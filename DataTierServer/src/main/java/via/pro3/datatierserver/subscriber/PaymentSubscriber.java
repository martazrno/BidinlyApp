package via.pro3.datatierserver.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import via.pro3.datatierserver.config.RabbitMQConfig;
import via.pro3.datatierserver.DTOs.PaymentValidatedMessage;
import via.pro3.datatierserver.model.Sale;
import via.pro3.datatierserver.repositories.ISaleRepository;

import java.time.Instant;

@Component
public class PaymentSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(PaymentSubscriber.class);

    @Autowired
    private ISaleRepository saleRepository;

    @RabbitListener(queues = RabbitMQConfig.sale_creation_queue)
    public void handlePaymentValidated(PaymentValidatedMessage message) {
        try {
            logger.info("Received payment validated event: PropertyId={}, BidId={}, BuyerId={}, AgentId={}",
                    message.getPropertyId(), message.getBidId(), message.getBuyerId(), message.getAgentId());

            Sale sale = new Sale();
            sale.setTimeOfSale(Instant.ofEpochSecond(message.getTimestamp()));
            sale.setPropertyId(message.getPropertyId());
            sale.setBidId(message.getBidId());
            sale.setBuyerId(message.getBuyerId());
            sale.setAgentId(message.getAgentId());

            Sale savedSale = saleRepository.save(sale);

            logger.info("Sale created successfully with ID: {} for PropertyId={}, BidId={}",
                    savedSale.getId(), message.getPropertyId(), message.getBidId());

        } catch (Exception e) {
            logger.error("Error creating sale from payment validated event: {}", e.getMessage(), e);
            throw e; //rabbitMQ retry mechanism
        }
    }
}