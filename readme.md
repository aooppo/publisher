# Install
- add dependencies into pom.xml
``` xml
<dependency>
    <groupId>cc.voox</groupId>
    <artifactId>publisher</artifactId>
    <version>0.2.0</version>
</dependency>
```
- add config into app-context-core.xml file		
``` xml
<bean id="amqpConfig" class="AMQPConfig">
    <property name="host" value="localhost"/>
    <property name="port" value="5672"/>
    <property name="username" value="root" />
    <property name="password" value="123456"/>
    <property name="virtualHost" value="/mytest" />
    <property name="prefixQueue" value="crm.api.test.queue"/>
    <property name="prefixExchange" value="crm.api.test.exchange"/>
    <property name="brokerUser" value="xs66@qq.cc"/>
    <property name="messageTTL" value="20000"/>
    <property name="path" value="com.trinitesolutions"/>
</bean>
```

# Usage
    
 - implements interface **IMsg** in entity class
 ``` java
public interface IMsg {
  String getPublishMsg();
}
```

 - use @Pub in entity class
 
```java
@Pub
class Item {

}
``` 
 
 - publish msg to broker
``` java
@Service
class TestService {
    
    @Autowired 
    private Publisher publisher;
    public void addCustomer(Customer customer) {
       publisher.publish(customer);
    }
}
```
