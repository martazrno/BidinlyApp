package via.pro3.datatierserver.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String payment_validated_exchange = "payment.validated";
    public static final String sale_creation_queue = "sale.queue";
    
    @Bean
    public FanoutExchange paymentValidatedExchange() {return new FanoutExchange(payment_validated_exchange);}
    
    @Bean
    public Queue saleCreationQueue() {
        return QueueBuilder.durable(sale_creation_queue).build();
    }
    
    @Bean
    public Binding saleCreationBinding() {
        return BindingBuilder
            .bind(saleCreationQueue())
            .to(paymentValidatedExchange());
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}