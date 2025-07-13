package com.SharedClasses;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ChatThread implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    private LocalDateTime lastUpdated = LocalDateTime.now(); // timestamp of the last update to the chat thread
    private final String id; // unique identifier for the chat thread
    private final String[] participants; // usernames of participants in the chat thread
    private ArrayList<Message> messageList = new ArrayList<>();// array of messages in the chat thread



    public String getRemoteUserName(String localUser){
        if(participants[0].equals(localUser)) return participants[1];
        return participants[0];
    }
    public ChatThread(String id, String[] participants) {
        this.id = id;
        this.participants = participants;
    }

    // a unique id is generated to identify the chat thread (to be used as a key in a map)
    // the id is generated based on the usernames of the participants
    // username1+username2 in sorted order

    public static String generateID(String username1, String username2) {
        // Generate a unique ID for the chat thread based on the usernames
        return username1.compareTo(username2) < 0 ? username1 + "_" + username2 : username2 + "_" + username1;
    }

    public void pushMessage(Message message){
        messageList.add(message);
        lastUpdated = LocalDateTime.now(); // Update the last updated timestamp
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }



    public String getId() {
        return id;
    }

    public String[] getParticipants() {
        return participants;
    }

    public ArrayList<Message> getMessageList() {
        return messageList;
    }
}
