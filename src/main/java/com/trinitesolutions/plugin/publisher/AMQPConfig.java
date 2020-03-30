package com.trinitesolutions.plugin.publisher;

public class AMQPConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
    private String exchange;
    private String prefixQueue;

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

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getPrefixQueue() {
        return prefixQueue;
    }

    public void setPrefixQueue(String prefixQueue) {
        this.prefixQueue = prefixQueue;
    }
}
