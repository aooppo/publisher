package cc.voox.plugin.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Publisher {
    private RabbitTemplate rabbitTemplate;
    private AMQPConfig amqpConfig;
    private Map<String, List<Union>> unionMap;
    @Autowired(required = false)
    PublishAdvice publishAdvice;

    @Autowired
    private void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    @Autowired
    private void setAmqpConfig(AMQPConfig amqpConfig) {
        this.amqpConfig = amqpConfig;
    }
    
    @Autowired(required = false)
    private void setUnionMap(Map<String, List<Union>> unionMap) {
        this.unionMap = unionMap;
    }

    public void publish(IMsg msg) {
        this.publish(msg, false);
    }

    /**
     *
     * @param msg payload
     * @param ignoreCheck Ignore whether checks exist
     */
    public void publish(IMsg msg, boolean ignoreCheck) {
        Map<String, Object> props = new HashMap<>();
        props.put("ignoreCheck", ignoreCheck);
        this.publish(msg.getPublishMsg(), msg.getClass(), props, msg.getMsgId());
    }


    public void publish(IMsg msg, String routingKey, Map<String, Object> props) {
        this.publish(msg.getPublishMsg(),routingKey, msg.getClass());
    }

    public Set<Class> getTypes() {
        return amqpConfig.getTypes().values().stream().map(TypeInfo::getClasses).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private List<Union> getUnionByType(Class<?> type) {
        Map<String, List<Union>> map = this.unionMap != null ? this.unionMap : this.amqpConfig.getUnionMap();
        if (this.amqpConfig != null && map != null) {
            for(Map.Entry<String, TypeInfo> entry: this.amqpConfig.getTypes().entrySet()) {
                TypeInfo typeInfo = entry.getValue();
                if (typeInfo != null) {
                    Set<Class<?>> set = typeInfo.getClasses();
                    if (set.contains(type)) {
                        return map.get(entry.getKey());
                    }
                }
            }
        }
        return null;
    }

    private void publish(String msg, String routingKey, Class<?> type) {
        if (type == null || !getTypes().contains(type)) {
            return;
        }
        List<Union> unions = getUnionByType(type);
        if (unions == null) {
            throw new RuntimeException("couldn't get exchange and queue for send : "+type);
        }
        for (Union union : unions) {
            rabbitTemplate.convertAndSend(union.getTopicExchange().getName(), routingKey, msg, m -> {
                m.getMessageProperties().getHeaders().put("user", union.getBrokerUser());
                return m;
            });
        }
   
    }

    private void publish(String msg, Class<?> type, Map<String, Object> props, String id) {
        if (type == null || !getTypes().contains(type)) {
            return;
        }
        List<Union> unions = getUnionByType(type);
        if (unions == null) {
            throw new RuntimeException("couldn't get exchange and queue for send : "+type);
        }
        try {
            if(publishAdvice != null) {
                publishAdvice.before(msg, type, props, id);
            }
            for (Union union : unions) {
                rabbitTemplate.convertAndSend(union.getTopicExchange().getName(), union.getRoutingKey(), msg, m -> {
                    m.getMessageProperties().getHeaders().put("user", union.getBrokerUser());
                    if(props != null) {
                        props.forEach((k,v)-> {
                            m.getMessageProperties().getHeaders().put(k, v);
                        });
                    }
                    return m;
                }, new CorrelationData(type, union.getBrokerUser(), id));
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if(publishAdvice != null) {
                publishAdvice.post(msg, type, props, id);
            }
        }
    }
}
