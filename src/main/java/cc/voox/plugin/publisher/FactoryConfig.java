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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@EnableRabbit
@Configuration
public class FactoryConfig {
    
    public final String DLX_EXCHANGE = ".dlx.exchange";
    
    public final String DLX_QUEUE = ".dlx.queue";
    
    @Autowired(required = false)
    private AMQPConfig config;
    
    @PostConstruct
    public void init() {
        if (config.isShowDebugInfo()) {
            System.out.println("Init AMQP Start");
        }
        AmqpAdmin ad = amqpAdmin();
        String brokerUser = config.getBrokerUser();
        if (brokerUser == null) {
            throw new IllegalArgumentException("===>> AMQP Broker users must be set");
        }
        // 按分隔符 ; 或 , 分割字符串
        String[] users = brokerUser.split("[;,]");
        
        // 处理用户列表
        Arrays.stream(users).filter(Objects::nonNull) // 防止 null 元素
                .map(String::trim)        // 去掉空格
                .filter(user -> !user.isEmpty()) // 过滤空字符串
                .forEach(user -> {
                    try {
                        // 对 user 进行 URL 编码，确保与 TS 的 encodeURIComponent 结果一致
                        String encodedUser = URLEncoder.encode(user, StandardCharsets.UTF_8.toString());
                        if (config.isShowDebugInfo()) {
                            System.out.println("Encoded user: " + encodedUser);
                            System.out.println(
                                    "dlx ex:" + config.getPrefixExchange() + "." + encodedUser + DLX_EXCHANGE);
                            System.out.println("dlx queue:" + config.getPrefixQueue() + "." + encodedUser + DLX_QUEUE);
                        }
                        
                        //define dlx
                        TopicExchange exchange = (TopicExchange) ExchangeBuilder.topicExchange(
                                config.getPrefixExchange() + "." + encodedUser + DLX_EXCHANGE).durable(true).build();
                        //        Map<String, Object> args = new HashMap<>();
                        //        args.put("x-message-ttl", 20000);
                        Queue queue = QueueBuilder.durable(config.getPrefixQueue() + "." + encodedUser + DLX_QUEUE)
                                .withArgument("x-message-ttl", config.getMessageTTL()).build();
                        ad.declareExchange(exchange);
                        ad.declareQueue(queue);
                        ad.declareBinding(BindingBuilder.bind(queue).to(exchange)
                                .with(config.getPrefixQueue() + "." + encodedUser + ".dlx.#"));
                    } catch (Exception e) {
                        // 捕获并记录单个用户处理中的错误
                        System.err.println("Error processing user: " + user);
                        e.printStackTrace();
                    }
                });
        //normal incoming
        Map<String, List<Union>> us = unionMap();
        us.values().forEach(unionList -> {
            for (Union u : unionList) {
                ad.declareQueue(u.getQueue());
                ad.declareExchange(u.getTopicExchange());
                ad.declareBinding(BindingBuilder.bind(u.getQueue()).to(u.getTopicExchange()).with(u.getRoutingKey()));
            }
        });
        if (config.isShowDebugInfo()) {
            System.out.println("Init AMQP End.");
        }
    }
    
    
    @Bean
    public CachingConnectionFactory factory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(config.getHost(), config.getPort());
        cf.setUsername(config.getUsername());
        cf.setPassword(config.getPassword());
        cf.setVirtualHost(config.getVirtualHost());
        
        cf.setPublisherConfirms(true);
        cf.setPublisherReturns(true);
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
        rt.setMandatory(true);
        rt.setConfirmCallback(config.getConfirmCallback());
        rt.setReturnCallback(config.getReturnCallback());
        return rt;
    }
    
    @Bean
    public Map<String, List<Union>> unionMap() {
        if (config.isShowDebugInfo()) {
            System.out.println("unionMap() is being initialized.");
        }
        
        Map<String, List<Union>> map = new HashMap<>();
        String prefixQueue = config.getPrefixQueue();
        String prefixExchange = config.getPrefixExchange();
        Set<String> types = config.getTypes().keySet();
        Map<String, TypeInfo> typeInfos = config.getTypes();
        // 获取并处理 brokerUser
        String brokerUser = config.getBrokerUser();
        if (brokerUser == null) {
            throw new IllegalArgumentException("===>> AMQP Broker users must be set");
        }
        // 分割并处理每个 user
        String[] users = brokerUser.split("[;,]");
        List<String> userGroups = Arrays.stream(users).filter(Objects::nonNull).map(String::trim)
                .filter(user -> !user.isEmpty()).collect(Collectors.toList());
        for (String user : userGroups) {
            String encodedUser = "";
            try {
                encodedUser = URLEncoder.encode(user, "UTF-8"); // 使用字符集名称
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported", e);
            }
            for (String type : types) {
                TypeInfo typeInfo = typeInfos.get(type);
                if (typeInfo == null) {
                    continue;
                }
                Pub pub = typeInfo.getPub();
                String[] excludeUsers = pub.excludeUsers();
                if (excludeUsers != null && Arrays.asList(excludeUsers).contains(user)) {
                    continue;
                }
                String qn = prefixQueue + "." + encodedUser + "." + type;
                String en = prefixExchange + "." + encodedUser + "." + type;
                Queue q = QueueBuilder.durable(qn)
                        .withArgument("x-dead-letter-exchange", prefixExchange + "." + encodedUser + DLX_EXCHANGE)
                        .withArgument("x-dead-letter-routing-key", prefixQueue + "." + encodedUser + ".dlx." + type)
                        .build();
                List<Union> unions = map.get(type);
                if (unions == null) {
                    unions = new ArrayList<>();
                }
                unions.add(Union.build(new TopicExchange(en), q, user));
                map.put(type, unions);
            }
        }
        
        config.setUnionMap(map);
        return map;
    }
    
    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(rabbitTemplate());
    }
    
}
