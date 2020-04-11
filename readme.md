# Install
- add dependencies into pom.xml
``` xml
        <dependency>
			<groupId>com.trinitesolutions.plugin</groupId>
			<artifactId>trinite-biz-publisher</artifactId>
			<version>1.0</version>
			<exclusions>
				<exclusion>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-logging</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
```
- add config into app-context-core.xml file		
``` xml
	<bean id="amqpConfig" class="com.trinitesolutions.plugin.publisher.AMQPConfig">
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
 
 - publish msg to broker
``` java
@Service
class TestService {
    
    @Autowired 
    private Publisher publisher;
    public void addCustomer(Customer customer) {
       publisher.publish(customer);
    }

    public void test() {
        publisher.publish("this is customer", PublishType.Customer);
    }
}
```
