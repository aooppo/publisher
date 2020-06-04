package cc.voox.plugin.publisher;

import java.util.Map;

public interface PublishAdvice {
    void before(String msg, Class<?> type, Map<String, Object> props, String id);
    void post(String msg, Class<?> type, Map<String, Object> props, String id);
}
