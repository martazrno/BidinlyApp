package via.pro3.propertyserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import via.pro3.propertyserver.grpc.PropertyProto;
import via.pro3.propertyserver.model.Property;
import via.pro3.propertyserver.repositories.IPropertyRepository;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock
    private IPropertyRepository propertyRepository;

    @Mock
    private StreamObserver<PropertyProto.PropertyResponse> responseObserver;

    private PropertyServiceImpl propertyService;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyServiceImpl(propertyRepository);
    }

    @Test
    void createProperty_success_returnsPropertyResponse() {
        PropertyProto.CreatePropertyRequest request = baseRequest().build();
        Property saved = createSavedEntity();

        when(propertyRepository.save(any(Property.class))).thenReturn(saved);

        propertyService.createProperty(request, responseObserver);

        ArgumentCaptor<PropertyProto.PropertyResponse> responseCaptor =
            ArgumentCaptor.forClass(PropertyProto.PropertyResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        PropertyProto.PropertyResponse response = responseCaptor.getValue();
        assertEquals(saved.getId(), response.getId());
        assertEquals(saved.getTitle(), response.getTitle());
        assertEquals(saved.getAgentId(), response.getAgentId());

        ArgumentCaptor<Property> entityCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(entityCaptor.capture());
        assertEquals(saved.getTitle(), entityCaptor.getValue().getTitle());
        assertEquals(saved.getAgentId(), entityCaptor.getValue().getAgentId());
    }

    @Test
    void createProperty_blankTitle_returnsInvalidArgument() {
        PropertyProto.CreatePropertyRequest request = baseRequest()
            .setTitle("")
            .build();

        propertyService.createProperty(request, responseObserver);

        verifyInvalidArgument("Title is required");
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_zeroStartingPrice_returnsInvalidArgument() {
        PropertyProto.CreatePropertyRequest request = baseRequest()
            .setStartingPrice(0)
            .build();

        propertyService.createProperty(request, responseObserver);

        verifyInvalidArgument("Starting price must be positive");
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_negativeStartingPrice_returnsInvalidArgument() {
        PropertyProto.CreatePropertyRequest request = baseRequest()
            .setStartingPrice(-1000)
            .build();

        propertyService.createProperty(request, responseObserver);

        verifyInvalidArgument("Starting price must be positive");
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_zeroBedrooms_returnsInvalidArgument() {
        PropertyProto.CreatePropertyRequest request = baseRequest()
            .setBedrooms(0)
            .build();

        propertyService.createProperty(request, responseObserver);

        verifyInvalidArgument("Bedrooms must be greater than zero");
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_zeroBathrooms_returnsInvalidArgument() {
        PropertyProto.CreatePropertyRequest request = baseRequest()
            .setBathrooms(0)
            .build();

        propertyService.createProperty(request, responseObserver);

        verifyInvalidArgument("Bathrooms must be greater than zero");
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_zeroSize_returnsInvalidArgument() {
        PropertyProto.CreatePropertyRequest request = baseRequest()
            .setSizeInSquareMeters(0)
            .build();

        propertyService.createProperty(request, responseObserver);

        verifyInvalidArgument("Size in square meters must be positive");
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void createProperty_repositoryThrows_returnsInternal() {
        PropertyProto.CreatePropertyRequest request = baseRequest().build();
        when(propertyRepository.save(any(Property.class)))
            .thenThrow(new RuntimeException("Database unavailable"));

        propertyService.createProperty(request, responseObserver);

        verifyInternalError("Database unavailable");
    }

    @Test
    void createProperty_defaultStatusApplied_whenMissing() {
        PropertyProto.CreatePropertyRequest request = baseRequest()
            .clearStatus()
            .build();

        Property saved = createSavedEntity();
        saved.setStatus("Available");
        when(propertyRepository.save(any(Property.class))).thenReturn(saved);

        propertyService.createProperty(request, responseObserver);

        ArgumentCaptor<Property> entityCaptor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(entityCaptor.capture());
        assertEquals("Available", entityCaptor.getValue().getStatus());
    }

    @Test
    void createProperty_optionalFieldsNull_stillCreatesProperty() {
        PropertyProto.CreatePropertyRequest request = PropertyProto.CreatePropertyRequest.newBuilder()
            .setAgentId(1)
            .setTitle("Minimal Property")
            .setStartingPrice(100000)
            .setBedrooms(1)
            .setBathrooms(1)
            .setSizeInSquareMeters(500)
            .build();

        Property saved = new Property();
        saved.setId(5);
        saved.setAgentId(1);
        saved.setTitle("Minimal Property");
        saved.setStartingPrice(BigDecimal.valueOf(100000));
        saved.setBedrooms(1);
        saved.setBathrooms(1);
        saved.setSizeInSquareMeters(500.0);
        saved.setStatus("Available");

        when(propertyRepository.save(any(Property.class))).thenReturn(saved);

        propertyService.createProperty(request, responseObserver);

        ArgumentCaptor<PropertyProto.PropertyResponse> responseCaptor =
            ArgumentCaptor.forClass(PropertyProto.PropertyResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        assertEquals("", responseCaptor.getValue().getAddress());
        assertEquals("", responseCaptor.getValue().getDescription());
    }

    @Test
    void getProperty_notFound_returnsGrpcError() {
        PropertyProto.GetPropertyRequest request = PropertyProto.GetPropertyRequest.newBuilder()
            .setId(99)
            .build();

        when(propertyRepository.getSingle(99)).thenReturn(Optional.empty());

        propertyService.getProperty(request, responseObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());

        StatusRuntimeException statusException = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.NOT_FOUND.getCode(), statusException.getStatus().getCode());
        assertEquals("Property with id 99 not found", statusException.getStatus().getDescription());
    }

    private PropertyProto.CreatePropertyRequest.Builder baseRequest() {
        return PropertyProto.CreatePropertyRequest.newBuilder()
            .setAgentId(1)
            .setTitle("Luxury Downtown Apartment")
            .setAddress("123 Main Street")
            .setStartingPrice(450000.00)
            .setBedrooms(2)
            .setBathrooms(2)
            .setSizeInSquareMeters(1200)
            .setDescription("Beautiful apartment with stunning city views")
            .setStatus("Available");
    }

    private Property createSavedEntity() {
        Property property = new Property();
        property.setId(1);
        property.setAgentId(1);
        property.setTitle("Luxury Downtown Apartment");
        property.setAddress("123 Main Street");
        property.setStartingPrice(BigDecimal.valueOf(450000.00));
        property.setBedrooms(2);
        property.setBathrooms(2);
        property.setSizeInSquareMeters(1200.0);
        property.setDescription("Beautiful apartment with stunning city views");
        property.setStatus("Available");
        return property;
    }

    private void verifyInvalidArgument(String expectedMessage) {
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException statusException = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.INVALID_ARGUMENT.getCode(), statusException.getStatus().getCode());
        assertEquals(expectedMessage, statusException.getStatus().getDescription());
    }

    private void verifyInternalError(String expectedMessageFragment) {
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException statusException = (StatusRuntimeException) errorCaptor.getValue();
        assertEquals(Status.INTERNAL.getCode(), statusException.getStatus().getCode());
        assertEquals("Error creating property: " + expectedMessageFragment,
            statusException.getStatus().getDescription());
    }
}
