package cc.voox.plugin.publisher;

import cc.voox.plugin.publisher.callback.IConfirm;
import cc.voox.plugin.publisher.callback.IResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AMQPConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
    private String prefixExchange;
    private String prefixQueue;
    private String brokerUser;
    private long messageTTL;
    private String path;
    private Map<String, Set<Class<?>>> types;
    private IConfirm confirmCallback;
    private IResult returnCallback;

    public AMQPConfig() {
        System.out.println("init AMQPConfig");
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Set<Class<?>>> getTypes() {
        return types;
    }

    @PostConstruct()
    private void init() {
        ClassPathScanningCandidateComponentProvider cp = new ClassPathScanningCandidateComponentProvider(false);
        cp.addIncludeFilter(new AnnotationTypeFilter(Pub.class));

        Set<BeanDefinition> bd = cp.findCandidateComponents(getPath());
        ClassLoader loader = AMQPConfig.class.getClassLoader();

        Map<String, Set<Class<?>>> types = new HashMap<>();
        for(BeanDefinition b : bd) {
            String beanName = b.getBeanClassName();
            try {
                Class<?> c = loader.loadClass(beanName);
                Pub pub = c.getAnnotation(Pub.class);
                String value = pub.value();
                if (StringUtils.isEmpty(value)) {
                    value = c.getSimpleName();
                }
                Set<Class<?>> set = types.get(value);
                if (set == null) {
                    set = new HashSet<>();
                }
                set.add(c);
                types.put(value, set);
            } catch (ClassNotFoundException e) {
                System.out.println("not found class"+ beanName + " @ " + getPath());
            }
        }
        System.out.println("Pub types@ "+ types);
        this.types = types;
    }


    public long getMessageTTL() {
        return messageTTL;
    }

    public void setMessageTTL(long messageTTL) {
        this.messageTTL = messageTTL;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getPrefixExchange() {
        return prefixExchange;
    }

    public void setPrefixExchange(String prefixExchange) {
        this.prefixExchange = prefixExchange;
    }

    public String getPrefixQueue() {
        return prefixQueue;
    }

    public void setPrefixQueue(String prefixQueue) {
        this.prefixQueue = prefixQueue;
    }

    public String getBrokerUser() {
        return brokerUser;
    }

    public void setBrokerUser(String brokerUser) {
        this.brokerUser = brokerUser;
    }

    public IConfirm getConfirmCallback() {
        return confirmCallback;
    }

    public void setConfirmCallback(IConfirm confirmCallback) {
        this.confirmCallback = confirmCallback;
    }

    public IResult getReturnCallback() {
        return returnCallback;
    }

    public void setReturnCallback(IResult returnCallback) {
        this.returnCallback = returnCallback;
    }
}
