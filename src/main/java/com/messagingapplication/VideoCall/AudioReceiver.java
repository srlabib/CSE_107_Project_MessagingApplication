package com.messagingapplication.VideoCall;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AudioReceiver extends Thread {
    private final DatagramSocket datagramSocket;
    private boolean active = true;
    private SourceDataLine speakers;
    public AudioReceiver(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        System.out.println("Starting audio receiving...");
        AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        speakers.start();

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (active) {
            try {
                System.out.println("Waiting for audio packet...");
                datagramSocket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            speakers.write(packet.getData(), 0, packet.getLength());
            System.out.println("Received audio packet of size: " + packet.getLength() + " bytes");
        }
    }

    public void end() {
        active = false;
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
        if (speakers != null && speakers.isOpen()) {
            speakers.close();
        }
    }

}
