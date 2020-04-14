package cc.voox.plugin.publisher;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
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
import java.util.Set;

@EnableRabbit
@Configuration
public class FactoryConfig {
    public final String DLX_EXCHANGE = ".dlx.exchange";
    public final String DLX_QUEUE = ".dlx.queue";
    @Autowired(required = false)
    private AMQPConfig config;

    @PostConstruct
    public void init() {
        System.out.println("Init AMQP Start");
        AmqpAdmin ad = amqpAdmin();
        //define dlx
        TopicExchange exchange = (TopicExchange) ExchangeBuilder.topicExchange(config.getPrefixExchange() + DLX_EXCHANGE).durable(true).build();
//        Map<String, Object> args = new HashMap<>();
//        args.put("x-message-ttl", 20000);
        Queue queue = QueueBuilder.durable(config.getPrefixQueue() + DLX_QUEUE).withArgument("x-message-ttl", config.getMessageTTL()).build();
        ad.declareExchange(exchange);
        ad.declareQueue(queue);
        ad.declareBinding(BindingBuilder.bind(queue).to(exchange).with(config.getPrefixQueue() + ".dlx.#"));

        //normal incoming
        Map<String, Union> us = unionMap();
        us.values().forEach(u -> {
            ad.declareQueue(u.getQueue());
            ad.declareExchange(u.getTopicExchange());
            ad.declareBinding(BindingBuilder.bind(u.getQueue()).to(u.getTopicExchange()).with(u.getRoutingKey()));
        });
        System.out.println("Init AMQP End.");
    }

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
        rt.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.out.println("HelloSender消息发送失败" + cause + correlationData.toString());
            } else {
                System.out.println("HelloSender 消息发送成功 ");
            }
        });
        return rt;
    }

    @Bean
    public Map<String, Union> unionMap() {
        Map<String, Union> map = new HashMap<>();
        String prefixQueue = config.getPrefixQueue();
        String prefixExchange = config.getPrefixExchange();
        Set<String> types = config.getTypes().keySet();
        for (String type : types) {
            String qn = prefixQueue + "." + type;
            String en = prefixExchange + "." + type;
            Queue q = QueueBuilder.durable(qn)
                    .withArgument("x-dead-letter-exchange", prefixExchange + DLX_EXCHANGE)
                    .withArgument("x-dead-letter-routing-key", prefixQueue + ".dlx." + type)
                    .build();
            map.put(type, Union.build(new TopicExchange(en), q));
        }
        return map;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(rabbitTemplate());
    }

}
