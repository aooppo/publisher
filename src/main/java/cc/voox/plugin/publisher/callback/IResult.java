package cc.voox.plugin.publisher.callback;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public interface IResult extends RabbitTemplate.ReturnCallback {
    default void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
    }
}
