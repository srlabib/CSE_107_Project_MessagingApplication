package com.messagingapplication.VideoCall;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AudioSender extends Thread{
    private final DatagramSocket datagramSocket;
    private final InetAddress receiverIP;
    private final int receiverPort;
    private boolean active = true;
    private TargetDataLine microphone;

    AudioSender(DatagramSocket datagramSocket, InetAddress ip, int recipientPort) {
        this.datagramSocket = datagramSocket;
        this.receiverPort = recipientPort;
        this.receiverIP = ip;
    }

    @Override
    public void run() {

        AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
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
                datagramSocket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
