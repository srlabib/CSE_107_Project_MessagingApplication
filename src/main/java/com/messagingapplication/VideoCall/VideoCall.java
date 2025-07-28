package com.messagingapplication.VideoCall;

import com.SharedClasses.CallRequest;
import com.messagingapplication.IncommingCallUIController;
import com.messagingapplication.Instances;
import com.messagingapplication.VideoCallUIController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.InnerShadow;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;


public class VideoCall extends Thread {
    private ObjectOutputStream mainOos;
    private CallRequest callRequest;
    private String currentUser;
    private Stage stage;
    private boolean responseRecieved = false;
    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private VideoStreaming videoStreaming;
    private VideoRecieving videoRecieving;
    private AudioSender audioSender;
    private AudioReceiver audioReceiver;
    private DatagramSocket datagramSocket;

    public VideoCall(CallRequest callRequest, ObjectOutputStream oos){
        mainOos = oos;
        this.callRequest = callRequest;
        Instances.mainUIController.isVideoCallActive = true;
        Instances.videoCall = this;
        currentUser = Instances.clientDataHandler.getCurrentUsername();
        socket = null;
        serverSocket = null;
        Platform.runLater(()-> {
            stage = new Stage();
        });
        this.setDaemon(true);

    }

    @Override
    public void run() {
        if(callRequest.getSender().equals(currentUser)){
            startNewCall();
        }
        else{
            startIncomingCall();
        }
    }

    public void updateCallRequest(CallRequest callRequest){
        this.callRequest = callRequest;
        responseRecieved = true;
        synchronized (this) {
            this.notifyAll(); // Notify the waiting thread that the call request has been updated
        }
    }

    private void startNewCall(){
        AtomicReference<VideoCallUIController> controller = new AtomicReference<>();
        Platform.runLater(()->{
//            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Video Call");
            stage.setResizable(false);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/messagingapplication/VideoCall.fxml"));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            controller.set(loader.getController());
            controller.get().setParticipantName(callRequest.getRecipient());
            controller.get().setWaitingStatus(true);
            stage.setScene(new Scene(root));
            stage.show();
        });
        responseRecieved = false;
        synchronized (mainOos){
            try {
                mainOos.writeObject(callRequest);
            } catch (IOException e) {
                System.out.println("Server is not responding");
            }
        }

        synchronized (this){
            try {
                while(!responseRecieved){
                    System.out.println("Waiting for call to be accepted or rejected");
                    this.wait(); // Wait for the call to be accepted or rejected
                }
            } catch (InterruptedException e) {
                System.out.println("Video call thread interrupted: " + e.getMessage());
            }
        }

        if (callRequest.getResponse().equals("accepted")) {
            Platform.runLater(()-> {
                controller.get().setWaitingStatus(false);
            });
            ObjectOutputStream oos;
            ObjectInputStream ois;
            try {
                socket = new Socket(callRequest.getIP(),callRequest.getPort());
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                // sending a dummy packet to send the audio port
                System.out.println("Port received: " + callRequest.getAudioPort());
                datagramSocket = new DatagramSocket();
                System.out.println("My ip: " + datagramSocket.getLocalAddress().getHostAddress()+" Port: " + datagramSocket.getLocalPort());
                DatagramPacket dp = new DatagramPacket(new byte[1024], 1024, InetAddress.getByName(callRequest.getIP()), callRequest.getAudioPort());
                datagramSocket.send(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            // call main video streaming methods
            InetAddress ip = null;
            int port = callRequest.getAudioPort();
            try {
                ip = InetAddress.getByName(callRequest.getIP());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            videoStreaming = new VideoStreaming(oos,controller.get());
            videoRecieving = new VideoRecieving(ois,controller.get());
            audioSender = new AudioSender(ip, port);
            audioReceiver = new AudioReceiver(datagramSocket);

            videoStreaming.start();
            videoRecieving.start();
            audioSender.start();
            audioReceiver.start();

            try {
                videoStreaming.join();
                videoRecieving.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        else{
            Platform.runLater(()-> {
                controller.get().setFailedStatus(callRequest.getResponse());
            });
        }
    }

    private void startIncomingCall(){
        AtomicReference<IncommingCallUIController> controller = new AtomicReference<>();
        Platform.runLater(()->{
//            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Video Call");
            stage.setResizable(false);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/messagingapplication/VideoCallIncomming.fxml"));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            controller.set(loader.getController());
            controller.get().setCallerName(callRequest.getSender());
            // I May set image here
            stage.setScene(new Scene(root));
            stage.show();
        });
    }

    public void acceptCall() {
        System.out.println("Local IP Address: " + getLocalIpAddress());
        callRequest.setIP(getLocalIpAddress());
        try {

            serverSocket = new ServerSocket(0);
            callRequest.setPort(serverSocket.getLocalPort());
            datagramSocket = new DatagramSocket();
            callRequest.setAudioPort(datagramSocket.getLocalPort());
            System.out.println("DatagramSocket created on port: " + callRequest.getAudioPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        callRequest.setResponse("accepted");
        callRequest.setProcessed();

        // Create a separate UI thread
        Platform.runLater(() -> {
            // Close the incoming call UI
            stage.close();

            // Create new video call UI
            stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/messagingapplication/VideoCall.fxml"));
            try {
                Parent root = loader.load();
                VideoCallUIController controller = loader.getController();
                controller.setParticipantName(callRequest.getSender());
                controller.setWaitingStatus(true); // Show "Connecting..." status
                stage.setScene(new Scene(root));
                stage.show();

                // Start connection thread after UI is ready
                startConnectionThread(controller);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void startConnectionThread(VideoCallUIController controller) {
        Thread connectionThread = new Thread(() -> {
            try {
                // Send response to caller
                synchronized (mainOos) {
                    mainOos.writeObject(callRequest);
                }

                System.out.println("Waiting for connection on " + callRequest.getIP() + ":" + callRequest.getPort());
                socket = serverSocket.accept(); // This blocks until connection is established

                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                // receiving a dummy packet to get the audio port
                DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
                datagramSocket.receive(dp);
                System.out.println("Received audio port: " + dp.getPort()+" IP: " + dp.getAddress().getHostAddress());
                System.out.println("Connection established with " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                // Update UI to show connected state
                Platform.runLater(() -> controller.setWaitingStatus(false));

                // Start video streaming on separate threads
                videoStreaming = new VideoStreaming(oos, controller);
                videoRecieving = new VideoRecieving(ois, controller);
                audioSender = new AudioSender(dp.getAddress(), dp.getPort());
                audioReceiver = new AudioReceiver(datagramSocket);
                videoStreaming.start();
                videoRecieving.start();
                audioSender.start();
                audioReceiver.start();

            } catch (IOException e) {
                System.out.println("Error while waiting for connection: " + e.getMessage());
                Platform.runLater(() -> controller.setFailedStatus("Connection failed: " + e.getMessage()));
            }
        });
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void startVideoStreamingReceiving(AtomicReference<VideoCallUIController> controller) {

    }


    public void rejectCall(){
        callRequest.setResponse("The call has been rejected");
        responseRecieved = true;
        synchronized (mainOos){
            try {
                mainOos.writeObject(callRequest);
            } catch (IOException e) {
                System.out.println("Server is not responding");
            }
        }

        endCall();

    }

    public void endCall(){
        if(videoStreaming != null){
            videoStreaming.end();
        }
        if(videoRecieving != null){
            videoRecieving.end();
        }
        if(audioSender != null){
            audioSender.end();
        }
        if(audioReceiver != null){
            audioReceiver.end();
        }
        Platform.runLater(()->{
            stage.close();
            Instances.mainUIController.isVideoCallActive = false;
            //Close threads for video calling
            if(videoRecieving!= null){
                videoRecieving.end();
            }
            Instances.videoCall = null;
        });

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing sockets: " + e.getMessage());
        }
    }

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();  // This is your local LAN IP
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";  // fallback
    }


}
