package com.trinitesolutions.plugin.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Publisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired(required = false)
    private AMQPConfig amqpConfig;
    @Autowired
    private Map<PublishType, Union> unionMap;

    public void publish(IMsg msg) {
        this.publish(msg.getPublishMsg(), msg.getPublishType());
    }

    public void publish(String msg, PublishType type) {
        if (type == null || !type.canPublish()) {
           throw new RuntimeException("Cannot publish " + msg);
        }
        Union union = unionMap.get(type);
        if (union == null) {
            throw new RuntimeException("couldn't get exchange and queue for send : "+type.name());
        }
        rabbitTemplate.convertAndSend(union.getTopicExchange().getName(), union.getRoutingKey(), msg, m -> {
            m.getMessageProperties().getHeaders().put("user", amqpConfig.getBrokerUser());
            return m;
        });
    }
}
