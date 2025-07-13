package com.AppServer;

import com.SharedClasses.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class MainServer {
    public static void main(String[] args) throws IOException {

        // creating a server socket
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(2222);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Runnable adminThread = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Enter command: ");
                    String command = scanner.nextLine();
                    if(command.equals("clear chats")){
                        System.out.println("Clearing all chats...");
                        ServerDataHandler dataHandler = ServerDataHandler.getInstance();
                        Map<String, User> users = dataHandler.getUsers();
                        for (User user : users.values()) {
                            user.getChatThreads().clear();
                        }
                        dataHandler.getCharThreads().clear();
                        dataHandler.saveUsers();
                        dataHandler.saveChatThreads();
                        System.out.println("All chats cleared.");
                    }
                    else if(command.equals("clear All")){
                        System.out.println("Clearing all data...");
                        ServerDataHandler dataHandler = ServerDataHandler.getInstance();
                        dataHandler.getUsers().clear();
                        dataHandler.getCharThreads().clear();
                        dataHandler.saveUsers();
                        dataHandler.saveChatThreads();
                        System.out.println("All data cleared.");
                    }

                    else if(command.equals("show users")){
                        System.out.println("Active users:");
                        ServerDataHandler dataHandler = ServerDataHandler.getInstance();
                        for (User user : dataHandler.getUsers().values()) {
                            System.out.println(user);
                        }
                    }

                    else if(command.equals("exit")){
                        System.out.println("Exiting server...");
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.exit(0);
                    }
                    else {
                        System.out.println("Unknown command: " + command);
                    }
                }
            }
        };
        new Thread(adminThread).start();


        while(true){
            Socket socket = serverSocket.accept();

            new ClientThread(socket);
        }


    }
}
