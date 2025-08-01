package com.AppServer;


import com.SharedClasses.CallRequest;
import com.SharedClasses.ChatThread;
import com.SharedClasses.Message;
import com.SharedClasses.User;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
        ChatThread chatThread ;

        if (chatThreadId == null) {
            System.out.println("Chat thread ID is null, generating a new one.");
            // If the chat thread does not exist, create a new one
            chatThreadId = ChatThread.generateID(message.getSender(), message.getReciepent());
            chatThread = new ChatThread(chatThreadId, new String[]{message.getSender(),message.getReciepent()});
            charThreads.put(chatThreadId, chatThread);
            chatThread.pushMessage(message);

            for(String participant : chatThread.getParticipants()) {
                users.get(participant).addChatThread(chatThreadId);
                ClientThread clientThread = activeUsers.get(participant);
                if (clientThread != null) {
                    clientThread.sendNewChatThread(chatThread);
                }
                saveUsers();
            }
        }
        else{
            chatThread = charThreads.get(chatThreadId);
            chatThread.pushMessage(message);
            for(String participant : chatThread.getParticipants()) {
                ClientThread clientThread = activeUsers.get(participant);
                if (clientThread != null) {
                    clientThread.sendMessage(message);
                }
            }
        }
        saveChatThreads();
        // Push the message to the chat thread

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

    public ChatThread getChatThread(String id) {
        return charThreads.get(id);
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, ChatThread> getCharThreads() {
        return charThreads;
    }


    public void handleCallRequest(CallRequest callRequest) {
        String sender  = callRequest.getSender();
        String reciepent = callRequest.getRecipient();
        if(callRequest.isProcessed()){
            if(activeUsers.get(sender) != null) {
                ClientThread senderThread = activeUsers.get(sender);
                senderThread.sendCallRequest(callRequest);
                System.out.println("The response is sent back to sender : " + sender);
            } else {
                System.out.println("Sender is not online, cannot send response");
            }
        }else{
            if(activeUsers.get(reciepent) != null) {
                ClientThread reciepentThread = activeUsers.get(reciepent);
                reciepentThread.sendCallRequest(callRequest);
                System.out.println("Call request sent from " + sender + " to " + reciepent);
            } else {
                callRequest.setResponse("The user is not online");
                ClientThread senderThread = activeUsers.get(sender);
                if(senderThread!=null)
                    senderThread.sendCallRequest(callRequest);
            }
        }


    }
    public ArrayList<String> getActiveUsers(){
        return new ArrayList<String>(activeUsers.keySet());
    }
}
