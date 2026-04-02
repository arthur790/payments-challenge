package com.bancobase.payments.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public FanoutExchange paymentStatusExchange(@Value("${app.rabbitmq.payment-status-exchange}") String name) {
        return new FanoutExchange(name, true, false);
    }

    @Bean
    public Queue paymentStatusNotifyQueue(@Value("${app.rabbitmq.payment-status-queue-notify}") String name) {
        return new Queue(name, true);
    }

    @Bean
    public Queue paymentStatusAuditQueue(@Value("${app.rabbitmq.payment-status-queue-audit}") String name) {
        return new Queue(name, true);
    }

    @Bean
    public Binding notifyBinding(FanoutExchange paymentStatusExchange, Queue paymentStatusNotifyQueue) {
        return BindingBuilder.bind(paymentStatusNotifyQueue).to(paymentStatusExchange);
    }

    @Bean
    public Binding auditBinding(FanoutExchange paymentStatusExchange, Queue paymentStatusAuditQueue) {
        return BindingBuilder.bind(paymentStatusAuditQueue).to(paymentStatusExchange);
    }
}
