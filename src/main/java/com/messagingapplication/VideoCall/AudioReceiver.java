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
                            return; // Exit thread rather than throwing exception
                        }

                        byte[] buffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        int packetCounter = 0;

                        while (active) {
                            try {
                                System.out.println("Waiting for audio packet...");
                                datagramSocket.receive(packet);

                                int bytesWritten = speakers.write(packet.getData(), 0, packet.getLength());
                                System.out.println("Received audio packet size: " + packet.getLength() +
                                                  " bytes, wrote " + bytesWritten + " bytes");

                                // Only drain occasionally to allow buffer to build up
                                packetCounter++;
                                if (packetCounter % 10 == 0) {
                                    speakers.drain();
                                }
                            } catch (IOException e) {
                                System.err.println("Error receiving audio: " + e.getMessage());
                                // Continue instead of terminating
                            }
                        }
                    }

                    public void end() {
                        active = false;

                        if (speakers != null && speakers.isOpen()) {
                            speakers.drain();
                            speakers.stop(); // Add stop before close
                            speakers.close();
                            System.out.println("Audio output closed");
                        }

                        if (datagramSocket != null && !datagramSocket.isClosed()) {
                            datagramSocket.close();
                            System.out.println("Audio socket closed");
                        }
                    }
                }