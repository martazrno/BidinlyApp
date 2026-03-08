package via.pro3.paymentserver.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import via.pro3.paymentserver.config.RabbitMQConfig;
import via.pro3.paymentserver.DTOs.PaymentValidatedMessage;

@Service
public class PaymentPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishPaymentValidated(PaymentValidatedMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.payment_validated_exchange, 
                "",  //fanout exchange doesnt need roting key
                message  //rabbitTemplate is handling JSON conversion
            );
            
            logger.info("Payment validated event published to RabbitMQ: PropertyId={}, BidId={}, BuyerId={}, AgentId={}", 
                message.getPropertyId(), message.getBidId(), message.getBuyerId(), message.getAgentId());
                
        } catch (Exception e) {
            logger.error("Error publishing payment validated event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish payment validated event", e);
        }
    }
}

