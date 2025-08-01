package com.messagingapplication;

import com.messagingapplication.VideoCall.VideoCall;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Instances {
    public static String ip = "192.168.0.101";
    public static MainUIController mainUIController;
    public static ClientDataHandler clientDataHandler;
    public static VideoCall videoCall;
    public static ObjectOutputStream oos;
    public static ObjectInputStream ois;
}
