package cc.voox.plugin.publisher;

import java.util.Set;

/**
 * <pre>
 *     TypeInfo
 * </pre>
 *
 * @author <a href="https://github.com/aooppo">TJ</a>
 * @since 2024-12-02
 */
public class TypeInfo {
    private Pub pub; // 存储 Pub 注解信息
    private Set<Class<?>> classes; // 存储对应的 Class 集合
    
    public TypeInfo(Pub pub, Set<Class<?>> classes) {
        this.pub = pub;
        this.classes = classes;
    }
    
    public Pub getPub() {
        return pub;
    }
    
    public void setPub(Pub pub) {
        this.pub = pub;
    }
    
    public Set<Class<?>> getClasses() {
        return classes;
    }
    
    public void setClasses(Set<Class<?>> classes) {
        this.classes = classes;
    }
    
    @Override
    public String toString() {
        return "TypeInfo{" +
                "pub=" + pub +
                ", classes=" + classes +
                '}';
    }
}
