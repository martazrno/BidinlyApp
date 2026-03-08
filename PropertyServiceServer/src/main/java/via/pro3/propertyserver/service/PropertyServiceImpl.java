package via.pro3.propertyserver.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.math.BigDecimal;
import java.util.List;
import net.devh.boot.grpc.server.service.GrpcService;
import via.pro3.propertyserver.grpc.PropertyProto;
import via.pro3.propertyserver.grpc.PropertyServiceGrpc;
import via.pro3.propertyserver.model.Property;
import via.pro3.propertyserver.repositories.IPropertyRepository;

@GrpcService
public class PropertyServiceImpl extends PropertyServiceGrpc.PropertyServiceImplBase {

    private final IPropertyRepository propertyRepository;

    public PropertyServiceImpl(IPropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public void createProperty(PropertyProto.CreatePropertyRequest request,
                               StreamObserver<PropertyProto.PropertyResponse> responseObserver) {
        try {
             validateCreate(request);

            Property property = new Property();
            property.setAgentId(request.getAgentId());
            property.setTitle(request.getTitle());
            property.setAddress(request.hasAddress() ? request.getAddress() : null);
            property.setStartingPrice(BigDecimal.valueOf(request.getStartingPrice()));
            property.setBedrooms(request.getBedrooms());
            property.setBathrooms(request.getBathrooms());
            property.setSizeInSquareMeters((double) request.getSizeInSquareMeters());
            property.setDescription(request.hasDescription() ? request.getDescription() : null);
            property.setStatus(request.getStatus().isBlank() ? "Available" : request.getStatus());
            property.setCreationStatus(request.getCreationStatus().isBlank() ? "Pending" : request.getCreationStatus());
            property.setImageUrl(request.hasImageUrl() ? request.getImageUrl() : null);

            Property saved = propertyRepository.save(property);
            responseObserver.onNext(toProto(saved));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Error creating property: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getProperty(PropertyProto.GetPropertyRequest request,
                            StreamObserver<PropertyProto.PropertyResponse> responseObserver) {
        try {
            propertyRepository.getSingle(request.getId())
                .ifPresentOrElse(property -> {
                        responseObserver.onNext(toProto(property));
                        responseObserver.onCompleted();
                    },
                    () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Property with id " + request.getId() + " not found")
                        .asRuntimeException()));
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Error retrieving property: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getProperties(PropertyProto.GetPropertiesRequest request,
                              StreamObserver<PropertyProto.GetPropertiesResponse> responseObserver) {
        try {
            Integer agentId = request.hasAgentId() ? request.getAgentId() : null;
            String status = request.hasStatus() ? request.getStatus() : null;
            String creationStatus = request.hasCreationStatus() ? request.getCreationStatus() : null;

            List<PropertyProto.PropertyResponse> properties = propertyRepository.getMany().stream()
                .filter(property -> agentId == null || agentId.equals(property.getAgentId()))
                .filter(property -> status == null || status.isBlank() || status.equalsIgnoreCase(property.getStatus()))
                .filter(property -> creationStatus == null || creationStatus.isBlank() || creationStatus.equalsIgnoreCase(property.getCreationStatus()))
                .map(this::toProto)
                .toList();

            PropertyProto.GetPropertiesResponse response = PropertyProto.GetPropertiesResponse.newBuilder()
                .addAllProperties(properties)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Error retrieving properties: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateProperty(PropertyProto.UpdatePropertyRequest request,
                               StreamObserver<PropertyProto.PropertyResponse> responseObserver) {
        try {
            Property property = propertyRepository.getSingle(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Property with id " + request.getId() + " not found"));

            applyUpdates(property, request);

            Property saved = propertyRepository.save(property);
            responseObserver.onNext(toProto(saved));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            Status status = e.getMessage() != null && e.getMessage().contains("not found")
                ? Status.NOT_FOUND
                : Status.INVALID_ARGUMENT;
            responseObserver.onError(status.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Error updating property: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteProperty(PropertyProto.DeletePropertyRequest request,
                               StreamObserver<PropertyProto.DeletePropertyResponse> responseObserver) {
        try {
            if (!propertyRepository.existsById(request.getId())) {
                responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Property with id " + request.getId() + " not found")
                    .asRuntimeException());
                return;
            }

            propertyRepository.deleteById(request.getId());

            PropertyProto.DeletePropertyResponse response = PropertyProto.DeletePropertyResponse.newBuilder()
                .setSuccess(true)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Error deleting property: " + e.getMessage()).asRuntimeException());
        }
    }

    private void applyUpdates(Property property, PropertyProto.UpdatePropertyRequest request) {
        if (request.hasTitle()) {
            property.setTitle(request.getTitle());
        }
        if (request.hasAddress()) {
            property.setAddress(request.getAddress());
        }
        if (request.hasStartingPrice()) {
            if (request.getStartingPrice() <= 0) {
                throw new IllegalArgumentException("Starting price must be positive");
            }
            property.setStartingPrice(BigDecimal.valueOf(request.getStartingPrice()));
        }
        if (request.hasBedrooms()) {
            if (request.getBedrooms() <= 0) {
                throw new IllegalArgumentException("Bedrooms must be greater than zero");
            }
            property.setBedrooms(request.getBedrooms());
        }
        if (request.hasBathrooms()) {
            if (request.getBathrooms() <= 0) {
                throw new IllegalArgumentException("Bathrooms must be greater than zero");
            }
            property.setBathrooms(request.getBathrooms());
        }
        if (request.hasSizeInSquareMeters()) {
            if (request.getSizeInSquareMeters() <= 0) {
                throw new IllegalArgumentException("Size in square meters must be positive");
            }
            property.setSizeInSquareMeters((double) request.getSizeInSquareMeters());
        }
        if (request.hasDescription()) {
            property.setDescription(request.getDescription());
        }
        if (request.hasStatus()) {
            property.setStatus(request.getStatus());
        }
        if (request.hasCreationStatus()) {
            property.setCreationStatus(request.getCreationStatus());
        }
        if (request.hasImageUrl()) {
            property.setImageUrl(request.getImageUrl());
        }
    }

    private void validateCreate(PropertyProto.CreatePropertyRequest request) {
        if (request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getStartingPrice() <= 0) {
            throw new IllegalArgumentException("Starting price must be positive");
        }
        if (request.getBedrooms() <= 0) {
            throw new IllegalArgumentException("Bedrooms must be greater than zero");
        }
        if (request.getBathrooms() <= 0) {
            throw new IllegalArgumentException("Bathrooms must be greater than zero");
        }
        if (request.getSizeInSquareMeters() <= 0) {
            throw new IllegalArgumentException("Size in square meters must be positive");
        }
    }

    private PropertyProto.PropertyResponse toProto(Property property) {
        return PropertyProto.PropertyResponse.newBuilder()
            .setId(property.getId() != null ? property.getId() : 0)
            .setAgentId(property.getAgentId() != null ? property.getAgentId() : 0)
            .setTitle(property.getTitle() != null ? property.getTitle() : "")
            .setAddress(property.getAddress() != null ? property.getAddress() : "")
            .setStartingPrice(property.getStartingPrice() != null ? property.getStartingPrice().doubleValue() : 0.0)
            .setBedrooms(property.getBedrooms() != null ? property.getBedrooms() : 0)
            .setBathrooms(property.getBathrooms() != null ? property.getBathrooms() : 0)
            .setSizeInSquareMeters(property.getSizeInSquareMeters() != null ? property.getSizeInSquareMeters().intValue() : 0)
            .setDescription(property.getDescription() != null ? property.getDescription() : "")
            .setStatus(property.getStatus() != null ? property.getStatus() : "Available")
            .setCreationStatus(property.getCreationStatus() != null ? property.getCreationStatus() : "Pending")
            .setImageUrl(property.getImageUrl() != null ? property.getImageUrl() : "")
            .build();
    }
}
