package cc.voox.plugin.publisher.callback;

import cc.voox.plugin.publisher.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


public interface IConfirm extends RabbitTemplate.ConfirmCallback {
    default void confirm(org.springframework.amqp.rabbit.support.CorrelationData cd, boolean ack, String cause) {
        if (!ack) {
            if (cd != null && cd instanceof CorrelationData) {
                CorrelationData mcd = (CorrelationData) cd;
                handleFail(mcd, cause);
            }
        } else {

            if (cd != null && cd instanceof CorrelationData) {
                CorrelationData mcd = (CorrelationData) cd;
                postAfterAck(mcd);
            }
        }
    }

    default void postAfterAck(CorrelationData mcd) {
    }

    default void handleFail(CorrelationData mcd, String cause) {
    }
}
