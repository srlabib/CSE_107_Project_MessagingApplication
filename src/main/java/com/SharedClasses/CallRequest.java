package com.SharedClasses;

import java.io.Serializable;

public class CallRequest implements Serializable {
    private static final long serialVersionUID = 5L;

    private final String sender;
    private final String recipient;
    private String IP;
    private int port;
    private String response;
    private boolean processed;
    private int audioPort;

    public CallRequest(String sender, String recipient) {
        this.sender = sender;
        this.recipient = recipient;
        processed = false;
    }

    public String getResponse() {
        return response;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    public int getAudioPort() {
        return audioPort;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAudioPort(int audioPort) {
        this.audioPort = audioPort;
    }

    public void setResponse(String response) {
        this.response = response;
        processed = true;
    }

    public void setProcessed(){
        processed = true;
    }

    public boolean isProcessed() {
        return processed;
    }
}
