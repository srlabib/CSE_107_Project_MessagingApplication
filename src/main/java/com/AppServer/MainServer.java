package com.AppServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {

    public static void main(String[] args) throws IOException {

        // creating a server socket
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(2222);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        while(true){
            Socket socket = serverSocket.accept();

            new ClientThread(socket);
        }


    }
}
