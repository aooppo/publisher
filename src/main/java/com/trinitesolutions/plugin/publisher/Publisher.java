package com.trinitesolutions.plugin.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
        this.publish(msg.getPublishMsg(), msg.getPublishType(), new HashMap<>());
    }

    public void publish(IMsg msg, boolean isNew) {
        Map<String, Object> props = new HashMap<>();
        props.put("isNew", isNew);
        this.publish(msg.getPublishMsg(), msg.getPublishType(), props);
    }


    public void publish(IMsg msg, String routingKey, Map<String, Object> props) {
        this.publish(msg.getPublishMsg(),routingKey, msg.getPublishType());
    }

    private void publish(String msg, String routingKey, PublishType type) {
        if (type == null || !type.canPublish()) {
            return;
        }
        Union union = unionMap.get(type);
        if (union == null) {
            throw new RuntimeException("couldn't get exchange and queue for send : "+type.name());
        }
        rabbitTemplate.convertAndSend(union.getTopicExchange().getName(), routingKey, msg, m -> {
            m.getMessageProperties().getHeaders().put("user", amqpConfig.getBrokerUser());
            return m;
        });
    }

    public void publish(String msg, PublishType type, Map<String, Object> props) {
        if (type == null || !type.canPublish()) {
           return;
        }
        Union union = unionMap.get(type);
        if (union == null) {
            throw new RuntimeException("couldn't get exchange and queue for send : "+type.name());
        }

        rabbitTemplate.convertAndSend(union.getTopicExchange().getName(), union.getRoutingKey(), msg, m -> {
            m.getMessageProperties().getHeaders().put("user", amqpConfig.getBrokerUser());
            if(props != null) {
                props.forEach((k,v)-> {
                    m.getMessageProperties().getHeaders().put(k, v);
                });
            }
            return m;
        });
    }
}
