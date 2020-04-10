package com.trinitesolutions.plugin.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public enum PublishType {
    Customer,
    Item("GenItem"),
    Contacts,
    NULL(false);

    private boolean publish = true;
    private List<String> keyList = new ArrayList<>();
    PublishType() {
        keyList.add(this.name());
    }

    PublishType(String ... keys) {
        this();
        if (keys != null) {
           Stream.of(keys).forEach(k -> this.keyList.add(k));
        }
    }

    PublishType(boolean publish) {
        this.publish = publish;
    }

    public List<String> getKeyList() {
        return keyList;
    }

    public boolean canPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public static PublishType forName(String name) {
        if (name == null) return NULL;
        for (PublishType t : values()) {
            if ((!t.getKeyList().isEmpty()) && t.getKeyList().contains(name)) {
                return t;
            }
        }
        return NULL;
    }
}
