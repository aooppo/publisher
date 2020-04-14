package cc.voox.plugin.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Publisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired(required = false)
    private AMQPConfig amqpConfig;
    @Autowired
    private Map<String, Union> unionMap;

    public void publish(IMsg msg) {
        Map<String, Object> props = new HashMap<>();
        props.put("isNew", true);
        this.publish(msg.getPublishMsg(), msg.getClass(), props);
    }

    public void publish(IMsg msg, boolean isNew) {
        Map<String, Object> props = new HashMap<>();
        props.put("isNew", isNew);
        this.publish(msg.getPublishMsg(), msg.getClass(), props);
    }


    public void publish(IMsg msg, String routingKey, Map<String, Object> props) {
        this.publish(msg.getPublishMsg(),routingKey, msg.getClass());
    }

    public Set<Class> getTypes() {
        return amqpConfig.getTypes().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private Union getUnionByType(Class<?> type) {
        if (this.amqpConfig != null && this.unionMap != null) {
            for(Map.Entry<String, Set<Class<?>>> entry: this.amqpConfig.getTypes().entrySet()) {
                Set<Class<?>> set = entry.getValue();
                if (set.contains(type)) {
                    return this.unionMap.get(entry.getKey());
                }
            }
        }
        return null;
    }

    private void publish(String msg, String routingKey, Class<?> type) {
        if (type == null || !getTypes().contains(type)) {
            return;
        }
        Union union = getUnionByType(type);
        if (union == null) {
            throw new RuntimeException("couldn't get exchange and queue for send : "+type);
        }
        rabbitTemplate.convertAndSend(union.getTopicExchange().getName(), routingKey, msg, m -> {
            m.getMessageProperties().getHeaders().put("user", amqpConfig.getBrokerUser());
            return m;
        });
    }

    private void publish(String msg, Class<?> type, Map<String, Object> props) {
        if (type == null || !getTypes().contains(type)) {
            return;
        }
        Union union = getUnionByType(type);
        if (union == null) {
            throw new RuntimeException("couldn't get exchange and queue for send : "+type);
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