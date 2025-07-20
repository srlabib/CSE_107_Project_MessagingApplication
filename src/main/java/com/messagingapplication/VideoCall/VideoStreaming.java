package com.messagingapplication.VideoCall;

import com.SharedClasses.CallRequest;
import com.messagingapplication.VideoCallUIController;
import javafx.application.Platform;
import javafx.scene.image.WritableImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class VideoStreaming extends Thread {
    private VideoCallUIController videoCallUIController;
    private VideoCapture capture;
    private boolean cameraActive = false;
    private ObjectOutputStream oos;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public VideoStreaming(ObjectOutputStream oos, VideoCallUIController videoCallUIController) {
        this.videoCallUIController = videoCallUIController;
        this.oos = oos;
    }

    @Override
    public void run() {
        System.out.println("Starting video streaming...");
        capture = new VideoCapture();
        capture.open(0);

        if (capture.isOpened()) {
            cameraActive = true;

            Mat original = new Mat();
            while (cameraActive) {
                if (capture.read(original)) {
                    Mat frameToDisplay = new Mat();
                    Mat frameToSend = new Mat();
                    Imgproc.resize(original, frameToDisplay, new Size(160, 120));
                    Imgproc.resize(original, frameToSend, new Size(640, 480));
                    WritableImage imageToShow = mat2Image(frameToDisplay);
                    byte [] imageToSend = null;
                    try {
                        imageToSend = matToJpeg(frameToSend);
                    } catch (IOException e) {
                        System.out.println("Error converting Mat to JPEG: " + e.getMessage());
                        e.printStackTrace();
                    }
                    videoCallUIController.displayLocalVideo(imageToShow);
                    synchronized (oos){
                        try {
                            oos.writeObject(imageToSend);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(33); // approx 30 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            capture.release();
        } else {
            System.err.println("Cannot open camera!");
        }
    }



    private WritableImage mat2Image(Mat frame) {
        BufferedImage bufferedImage = matToBufferedImage(frame);
        WritableImage image = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                image.getPixelWriter().setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return image;
    }

    private BufferedImage matToBufferedImage(Mat original) {
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        System.out.println(width*height* channels);
        original.get(0, 0, sourcePixels);

        BufferedImage image;
        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
        return image;
    }

    private byte[] matToJpeg(Mat mat) throws IOException {
        BufferedImage image = matToBufferedImage(mat);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }


    public void end() {
        cameraActive = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }


}
