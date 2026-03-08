package via.pro3.propertyserver.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import via.pro3.propertyserver.config.RabbitMQConfig;
import via.pro3.propertyserver.DTOs.PaymentValidatedMessage;
import via.pro3.propertyserver.model.Property;
import via.pro3.propertyserver.repositories.IPropertyRepository;

import java.util.Optional;

@Component
public class PropertySubscriber {

    private static final Logger logger = LoggerFactory.getLogger(PropertySubscriber.class);

    @Autowired
    private IPropertyRepository propertyRepository;

    @RabbitListener(queues = RabbitMQConfig.property_update_queue)
    public void handlePaymentValidated(PaymentValidatedMessage message) {
        try {
            logger.info("Received payment validated event: PropertyId={}, BidId={}, BuyerId={}, AgentId={}",
                    message.getPropertyId(), message.getBidId(), message.getBuyerId(), message.getAgentId());

            Optional<Property> propertyOpt = propertyRepository.getSingle(message.getPropertyId());
            
            if (propertyOpt.isEmpty()) {
                logger.warn("Property with id {} not found, cannot update status", message.getPropertyId());
                return;
            }
            
            Property property = propertyOpt.get();
            property.setStatus("Sold");
            
            Property savedProperty = propertyRepository.save(property);
            
            logger.info("Property status updated to 'Sold' successfully. PropertyId={}, Title={}",
                    savedProperty.getId(), savedProperty.getTitle());

        } catch (Exception e) {
            logger.error("Error updating property status from payment validated event: {}", e.getMessage(), e);
            throw e; // RabbitMQ retry mechanism
        }
    }
}

