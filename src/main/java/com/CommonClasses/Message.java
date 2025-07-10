package com.CommonClasses;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 4L;
    private final String sender; // username of the sender
    private final String threadID; // which chat thread the message belongs to
    private final String content; // content of the message
    private final LocalDateTime timestamp; // timestamp of when the message was sent

    public Message(String sender,String thradID, String content, LocalDateTime time) {
        this.sender = sender;
        this.threadID = thradID;
        this.content = content;
        this.timestamp = time;
    }

    public String getSender() {
        return sender;
    }

    public String getThreadID() {
        return threadID;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
