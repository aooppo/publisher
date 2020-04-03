package com.trinitesolutions.plugin.publisher;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

public class Union {
    private TopicExchange topicExchange;
    private Queue queue;
    private String routingKey;

    private Union(TopicExchange topicExchange, Queue queue, String routingKey) {
        this.topicExchange = topicExchange;
        this.queue = queue;
        this.routingKey = routingKey;
    }

    public TopicExchange getTopicExchange() {
        return topicExchange;
    }

    public void setTopicExchange(TopicExchange topicExchange) {
        this.topicExchange = topicExchange;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    protected static Union build(TopicExchange topicExchange, Queue queue) {
        return new Union(topicExchange, queue, queue.getName());
    }

    protected static Union build(TopicExchange topicExchange, Queue queue, String routingKey) {
        return new Union(topicExchange, queue, routingKey);
    }
}
