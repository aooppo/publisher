package com.trinitesolutions.plugin.publisher;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@EnableRabbit
@Configuration
public class FactoryConfig {

    @Autowired(required = false)
    private AMQPConfig config;

    @Bean
    public CachingConnectionFactory factory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(config.getHost(), config.getPort());
        cf.setUsername(config.getUsername());
        cf.setPassword(config.getPassword());
        cf.setVirtualHost(config.getVirtualHost());
        return cf;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(factory());
        factory.setIdleEventInterval(60000L);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rt = new RabbitTemplate(factory());
        return rt;
    }

    @Bean
    public Map<PublishType, Queue> queueFactory() {
        Map<PublishType, Queue> map = new HashMap<>();
        String prefix = config.getPrefixQueue();
        for (PublishType type : PublishType.values()) {
            String name = prefix + type.name();
            Queue q = new Queue(name);
            map.put(type, q);

        }
        return map;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(rabbitTemplate());
    }

    @PostConstruct
    public void createQueuesAndBinding() {
        AmqpAdmin ad = amqpAdmin();
        Map<PublishType, Queue> queues = queueFactory();
        queues.values().forEach(q -> {
            ad.declareQueue(q);
            ad.declareBinding(BindingBuilder.bind(q).to(defaultExchange()).with(q.getName()));
        });

    }

    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(config.getExchange());
    }


}
