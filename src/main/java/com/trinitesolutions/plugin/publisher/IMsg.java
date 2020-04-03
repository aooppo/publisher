package com.trinitesolutions.plugin.publisher;

public interface IMsg {
    String getPublishMsg();

    default PublishType getPublishType() {
        return PublishType.forName(this.getClass().getSimpleName());
    }
}
