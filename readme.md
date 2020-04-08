# Install
- add dependencies into pom.xml

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

- add config into app-context-core.xml file		

        <bean id="amqpConfig" class="com.trinitesolutions.plugin.publisher.AMQPConfig">
            <property name="host" value="localhost"/>
            <property name="port" value="5672"/>
            <property name="username" value="root" />
            <property name="password" value="123456"/>
            <property name="virtualHost" value="/mytest" />
            <property name="prefixQueue" value="crm.api.test.queue"/>
            <property name="prefixExchange" value="crm.api.test.exchange"/>
            <property name="brokerUser" value="user"/> <!-- middleware username, must register, then input -->
            <property name="messageTTL" value="20000"/>
        </bean>


# Usage
    
 - implements interface **IMsg** in entity class
 ```
public interface IMsg {
  String getPublishMsg();
}
```
 - publish msg to broker
```
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
