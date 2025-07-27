package com.messagingapplication.VideoCall;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.*;

public class AudioSender extends Thread{
    private final DatagramSocket datagramSocket;
    private final InetAddress receiverIP;
    private final int receiverPort;
    private boolean active = true;
    private TargetDataLine microphone;

    AudioSender(InetAddress ip, int recipientPort) {
        try {
            this.datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        this.receiverPort = recipientPort;
        this.receiverIP = ip;

        System.out.println("Other user IP: " + receiverIP.getHostAddress() + ", port: " + receiverPort);
    }

    @Override
    public void run() {

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // Open microphone

        try {
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        microphone.start();

        byte[] buffer = new byte[1024];
        while (active) {
            microphone.read(buffer, 0, buffer.length);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverIP, receiverPort);
            try {
//                System.out.println("Sending audio packet of size: " + packet.getLength() + " bytes to " + receiverIP + ":" + receiverPort);
                datagramSocket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            System.out.println("Sent audio packet of size: " + packet.getLength() + " bytes to " + receiverIP + ":" + receiverPort);
        }
    }

    public void end() {
        active = false;
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
        if(microphone != null && microphone.isOpen()) {
            microphone.close();
        }
    }
}
