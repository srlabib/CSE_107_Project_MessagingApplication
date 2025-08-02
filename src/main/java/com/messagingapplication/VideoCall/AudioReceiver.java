package com.messagingapplication.VideoCall;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AudioReceiver extends Thread {
    private final DatagramSocket datagramSocket;
    private volatile boolean active = true;
    private SourceDataLine speakers;

    public AudioReceiver(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("Starting audio receiving...");
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();
            System.out.println("Audio output initialized successfully");
        } catch (LineUnavailableException e) {
            System.err.println("Failed to initialize audio: " + e.getMessage());
            return;
        }

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        int packetCounter = 0;

        while (active && !Thread.currentThread().isInterrupted()) {
            try {
                datagramSocket.receive(packet);

                if (packet.getLength() > 0) {
                    int bytesWritten = speakers.write(packet.getData(), 0, packet.getLength());

                    packetCounter++;
                    if (packetCounter % 20 == 0) {
                        speakers.drain();
                    }
                }
            } catch (IOException e) {
                if (active) {
                    System.err.println("Error receiving audio: " + e.getMessage());
                }
                break;
            }
        }

        // Clean up resources when loop exits
        cleanupResources();
    }

    public void end() {
        active = false;
        this.interrupt(); // Interrupt the thread to break out of receive()
        cleanupResources();
    }

    private synchronized void cleanupResources() {
        if (speakers != null && speakers.isOpen()) {
            try {
                speakers.drain();
                speakers.stop();
                speakers.close();
                System.out.println("Audio speakers cleaned up");
            } catch (Exception e) {
                System.err.println("Error closing speakers: " + e.getMessage());
            }
            speakers = null;
        }
    }
}