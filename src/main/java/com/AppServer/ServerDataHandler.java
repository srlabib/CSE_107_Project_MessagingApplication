package com.AppServer;


import com.CommonClasses.ChatThread;
import com.CommonClasses.User;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerDataHandler {
    private Map<String, User> users = new ConcurrentHashMap<String, User>();
    private Map<String, ChatThread> charThreads = new ConcurrentHashMap<String, ChatThread>();

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

    ChatThread searchChatThread(String id) {
        return charThreads.get(id);
    }

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
}
