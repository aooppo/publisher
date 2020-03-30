package com.trinitesolutions.plugin.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Publisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired(required = false)
    private AMQPConfig amqpConfig;

    public void publish(String msg) {
        rabbitTemplate.convertAndSend(amqpConfig.getExchange(), "", msg);
    }

    public void publish(String msg, String exchange, String routingKey) {
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
    }

    public void publish(String msg, PublishType type) {
        rabbitTemplate.convertAndSend(amqpConfig.getExchange(), amqpConfig.getPrefixQueue() + type.name(), msg);
    }
}
