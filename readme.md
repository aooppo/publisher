# install
##### add dependencies into pom.xml

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
##### add config into  		

	<bean id="amqpConfig" class="com.trinitesolutions.plugin.publisher.AMQPConfig">
		<property name="host" value="localhost"/>
		<property name="port" value="5672"/>
		<property name="username" value="root" />
		<property name="password" value="123456"/>
		<property name="virtualHost" value="/mytest" />
		<property name="prefixQueue" value="events.test."/>
		<property name="exchange" value="events.test"/>
	</bean>


# Usage
    @Service
    class TestService {
    
	@Autowired private Publisher publisher;
	
	public void test() {
	    publisher.publish("this is customer", PublishType.Customer);
	}
}

