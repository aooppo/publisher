package cc.voox.plugin.publisher;


public class CorrelationData extends org.springframework.amqp.rabbit.support.CorrelationData {
    private Class<?> clazz;
    private String brokerUser;
    private String oid;

    public CorrelationData(Class<?> clazz, String brokerUser, String oid) {
        super("@type@" + clazz + "@user@" + brokerUser + "@id@" + oid);
        this.clazz = clazz;
        this.brokerUser = brokerUser;
        this.oid = oid;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getBrokerUser() {
        return brokerUser;
    }

    public void setBrokerUser(String brokerUser) {
        this.brokerUser = brokerUser;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
