package via.pro3.paymentserver.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import via.pro3.paymentserver.grpc.PaymentServiceGrpc;
import via.pro3.paymentserver.grpc.PaymentProto;
import via.pro3.paymentserver.model.ValidCard;
import via.pro3.paymentserver.repositories.IValidCardRepository;
import via.pro3.paymentserver.publisher.PaymentPublisher;
import via.pro3.paymentserver.DTOs.PaymentValidatedMessage;

import java.util.Optional;

@GrpcService
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGrpcService.class);

    @Autowired
    private IValidCardRepository validCardRepository;
    
    @Autowired
    private PaymentPublisher paymentPublisher;

    @Override
    public void validateCard(PaymentProto.ValidateCardRequest request, StreamObserver<PaymentProto.ValidateCardResponse> responseObserver) {
        String cardNumber = request.getCardNumber() == null ? "" : request.getCardNumber().trim();
        String expirationDate = request.getExpirationDate() == null ? "" : request.getExpirationDate().trim();
        String cvc = request.getCvc() == null ? "" : request.getCvc().trim();
        String name = request.getName() == null ? "" : request.getName().trim();
        
        Optional<ValidCard> cardOpt = validCardRepository.findById(cardNumber);
        
        if (!cardOpt.isPresent()) {
            PaymentProto.ValidateCardResponse response = PaymentProto.ValidateCardResponse.newBuilder()
                    .setIsValid(false)
                    .setMessage("Card not found")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }
        
        ValidCard card = cardOpt.get();
        
        String dbExpirationDate = card.getExpirationDate() == null ? "" : card.getExpirationDate().trim();
        String dbCvc = card.getCvc() == null ? "" : card.getCvc().trim();
        String dbName = card.getName() == null ? "" : card.getName().trim();
        
        if (!dbExpirationDate.equals(expirationDate)) {
            PaymentProto.ValidateCardResponse response = PaymentProto.ValidateCardResponse.newBuilder()
                    .setIsValid(false)
                    .setMessage("Invalid expiration date")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }
        
        if (!dbCvc.equals(cvc)) {
            PaymentProto.ValidateCardResponse response = PaymentProto.ValidateCardResponse.newBuilder()
                    .setIsValid(false)
                    .setMessage("Invalid CVC")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }
        
        if (!dbName.equalsIgnoreCase(name)) {
            PaymentProto.ValidateCardResponse response = PaymentProto.ValidateCardResponse.newBuilder()
                    .setIsValid(false)
                    .setMessage("Invalid cardholder name")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }
        
        PaymentProto.ValidateCardResponse response = PaymentProto.ValidateCardResponse.newBuilder()
                .setIsValid(true)
                .setMessage("Card validated successfully")
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        
        if (request.hasPropertyId() && request.hasBidId() && 
            request.hasBuyerId() && request.hasAgentId()) {
            try {
                PaymentValidatedMessage message = new PaymentValidatedMessage(
                    request.getPropertyId(),
                    request.getBidId(),
                    request.getBuyerId(),
                    request.getAgentId(),
                    cardNumber
                );
                paymentPublisher.publishPaymentValidated(message);
                logger.info("Payment validated event published to RabbitMQ for PropertyId={}, BidId={}", 
                    request.getPropertyId(), request.getBidId());
            } catch (Exception e) {
                logger.error("Error publishing payment validated event to RabbitMQ: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("Payment validated but no sale info provided, skipping RabbitMQ publish");
        }
    }
}

