package com.AppServer;


import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import com.SharedClasses.User;
import javafx.scene.layout.VBox;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerDataHandler {
    private Map<String, User> users = new ConcurrentHashMap<String, User>();
    private Map<String, ChatThread> charThreads = new ConcurrentHashMap<String, ChatThread>();
    private Map<String,ClientThread> activeUsers = new ConcurrentHashMap<>();

    private static ServerDataHandler INSTANCE;
    private final String usersFilePath = "Assets/Userdata/users.bin";
    private final String chatThreadsFilePath = "Assets/Userdata/chatThreads.bin";
    private <T>
    T loadData(String filename, Class<? extends T> classtype) {
       File file = new File(filename);

       try {
           if (file.exists()) {
               try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                   return (T) ois.readObject();
               }
           } else {
               T t = classtype.getDeclaredConstructor().newInstance();
               try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                   oos.writeObject(t);
               }
               return t;
           }
       } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
           e.printStackTrace();
           return null;
       }
   }
    private ServerDataHandler() {
        // Private constructor to prevent instantiation
        // Load users from the file
        users = loadData(usersFilePath,ConcurrentHashMap.class);
        charThreads = loadData(chatThreadsFilePath, ConcurrentHashMap.class);
    }

    User seachUser(String username) {
        return users.get(username);
    }
    User createUser(String username, String password, String name) {
        if (seachUser(username) != null) {
            throw new IllegalArgumentException("User already exists with username: " + username);
        }
        User user = new User(username, password,name);
        users.put(username, user);
        saveUsers();
        return user;
    }

//    ChatThread searchChatThread(String id) {
//        return charThreads.get(id);
//    }

    ChatThread createChatThread(String id, String name, String[] participants) {
        ChatThread chatThread = new ChatThread(id, participants);
        charThreads.put(id, chatThread);
        return chatThread;
    }

    public synchronized void saveUsers(){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(usersFilePath))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveChatThreads() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chatThreadsFilePath))) {
            oos.writeObject(charThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ServerDataHandler getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ServerDataHandler();
        }
        return INSTANCE;
    }

    public synchronized void addNewMessage(Message message, String username){
        // Generate the chat thread ID based on the current user and the message sender
        String chatThreadId = message.getThreadID();
        // Search for the chat thread in the existing threads
        ChatThread chatThread = charThreads.get(chatThreadId);

        if (chatThread == null) {
            // If the chat thread does not exist, create a new one
            chatThread = new ChatThread(chatThreadId, new String[]{username,message.getSender()});
            charThreads.put(chatThreadId, chatThread);
            chatThread.pushMessage(message);
            for(String participant : chatThread.getParticipants()) {
                ClientThread clientThread = activeUsers.get(participant);
                if (clientThread != null) {
                    clientThread.sendNewChatThread(chatThread);
                }
            }
        }
        else{
            chatThread.pushMessage(message);
            for(String participant : chatThread.getParticipants()) {
                ClientThread clientThread = activeUsers.get(participant);
                if (clientThread != null) {
                    clientThread.sendMessage(message);
                }
            }
        }
        // Push the message to the chat thread


        // Send the message to all active participants
        for(String participant : chatThread.getParticipants()) {
            ClientThread clientThread = activeUsers.get(participant);
            if (clientThread != null) {
                clientThread.sendMessage(message);
            }
        }
    }

    public void addActiveUser(String username, ClientThread clientThread) {
        activeUsers.put(username, clientThread);
    }

    public void removeActiveUser(String username) {
        if (username == null || !activeUsers.containsKey(username)) {
            throw new IllegalArgumentException("User not found or username is null");
        }
        activeUsers.remove(username);
    }
}
