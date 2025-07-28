package com.messagingapplication.VideoCall;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.*;

public class AudioSender extends Thread {
    private final DatagramSocket datagramSocket;
    private final InetAddress receiverIP;
    private final int receiverPort;
    private volatile boolean active = true;
    private TargetDataLine microphone;

    AudioSender(InetAddress ip, int recipientPort) {
        try {
            this.datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        this.receiverPort = recipientPort;
        this.receiverIP = ip;
        System.out.println("Sending audio to: " + receiverIP.getHostAddress() + ":" + receiverPort);
    }

    @Override
    public void run() {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            System.out.println("Microphone initialized successfully");
        } catch (LineUnavailableException e) {
            System.err.println("Failed to initialize microphone: " + e.getMessage());
            return;
        }

        byte[] buffer = new byte[1024];
        while (active && !Thread.currentThread().isInterrupted()) {
            try {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, receiverIP, receiverPort);
                    datagramSocket.send(packet);
                }
            } catch (IOException e) {
                if (active) {
                    System.err.println("Error sending audio: " + e.getMessage());
                }
                break;
            }
        }

        cleanupResources();
    }

    public void end() {
        active = false;
        this.interrupt();
        cleanupResources();
    }

    private synchronized void cleanupResources() {
        if (microphone != null && microphone.isOpen()) {
            try {
                microphone.stop();
                microphone.close();
                System.out.println("Microphone cleaned up");
            } catch (Exception e) {
                System.err.println("Error closing microphone: " + e.getMessage());
            }
            microphone = null;
        }

        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
            System.out.println("Audio sender socket closed");
        }
    }
}