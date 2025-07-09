package com.CommonClasses;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 4L;
    private final String sender; // username of the sender
    private final String content; // content of the message
    private final long timestamp; // timestamp of when the message was sent

    public Message(String sender, String content, long time) {
        this.sender = sender;
        this.content = content;
        this.timestamp = time;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
