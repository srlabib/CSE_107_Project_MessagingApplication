package com.AppServer;


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataHandler {
    private Map<String, User> users = new ConcurrentHashMap<String, User>();



    private static DataHandler INSTANCE;
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
    private DataHandler() {
        // Private constructor to prevent instantiation

        // Load users from the file
        users = loadData("Assets/Userdata/users.bin",ConcurrentHashMap.class);

    }

    User seachUser(String username) {
        return users.get(username);
    }

   public static DataHandler getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new DataHandler();
        }
        return INSTANCE;
   }


}
