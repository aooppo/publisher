package com.trinitesolutions.plugin.publisher;

public enum PublishType {
    Customer,
    Item,
    Contacts,
    NULL(false);

    private boolean publish = true;

    PublishType() {
    }

    PublishType(boolean publish) {
        this.publish = publish;
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
            if (t.name().equals(name)) {
                return t;
            }
        }
        return NULL;
    }
}
