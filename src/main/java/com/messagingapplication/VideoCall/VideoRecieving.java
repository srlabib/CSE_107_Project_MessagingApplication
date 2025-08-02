package com.messagingapplication.VideoCall;

import com.SharedClasses.CallRequest;
import com.messagingapplication.Instances;
import com.messagingapplication.VideoCallUIController;
import javafx.application.Platform;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class VideoRecieving extends Thread{

    private ObjectInputStream ois;
    private VideoCallUIController videoCallUIController;
    private boolean active = true;

    public VideoRecieving(ObjectInputStream ois, VideoCallUIController videoCallUIController){
        this.ois = ois;
        this.videoCallUIController = videoCallUIController;
        this.setName("Video Receiving Thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("Starting video receiving...");
        while(active){
            try {
                Object obj = ois.readObject();
                byte [] image = (byte[]) obj;
                WritableImage imageToShow = jpegToImage(image);
                videoCallUIController.displayRemoteVideo(imageToShow);
            }
            // the call is declined or ended
            catch(EOFException e){
                System.out.println("Video call ended or declined.");
                Instances.videoCall.endCall();
                break;
            }
            catch (Exception e) {
                e.printStackTrace();
                break; // Exit the loop on error
            }
        }
    }



    private WritableImage jpegToImage(byte[] jpegData) throws IOException {
        BufferedImage bufferedImage = jpegToBufferedImage(jpegData);
        WritableImage image = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                image.getPixelWriter().setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return image;
    }

    private BufferedImage jpegToBufferedImage(byte[] jpegData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(jpegData);
        return ImageIO.read(bais);
    }

    public void end(){
        active = false;
        try {
            ois.close();
        } catch (IOException e) {
            System.err.println("Error closing ObjectInputStream: " + e.getMessage());
        }
    }
}
