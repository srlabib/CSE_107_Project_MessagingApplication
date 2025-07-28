package com.SharedClasses;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String username;
    private String password;
    private final String name;
    private HashSet<String>chatThreads;     // all id of chat Thread

    public User(String username, String password,String name) {
        this.username = username;
        this.password = password;
        this.name = name;
        chatThreads = new HashSet<String>();
    }

    public String getUsername() {
        return username;
    }

    public void verifyPassword(String password) {
        System.out.println(password+" "+this.password);
        if (!this.password.equals(password)) {
            throw new IllegalArgumentException("Incorrect Password");
        }
    }

    public HashSet<String> getChatThreads() {
        return chatThreads;
    }

    public void addChatThread(String chatThreadId) {
        chatThreads.add(chatThreadId);
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getName(){
        return  name;
    }

    /*
    idea:
    * when user send a message to server it will contain a chat thread id.
    * what if the user want to send message to a new person?
    * in that case the client will send a request with the person username to create a new chat thread

    * when the server is connected to the client
    * A client thread will be created
    * it will send all data to the client at first
    * there will be three subclass to the client each will be runnable
    * message sender, message receiver , update pusher
    * whenever there is a new update the waiting update pusher will be notified
    * and send all updates to the client from pending updates list

     */

}
