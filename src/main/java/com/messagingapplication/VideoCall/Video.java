package com.messagingapplication.VideoCall;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Video extends Application {

    private VideoCapture capture;
    private boolean cameraActive = false;
    private ImageView imageView;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) {
        imageView = new ImageView();
        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 640, 480);

        primaryStage.setTitle("Webcam Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
        startCamera();
    }

    private void startCamera() {
        capture = new VideoCapture();
        capture.open(0);

        if (capture.isOpened()) {
            cameraActive = true;

            Thread frameGrabber = new Thread(() -> {
                Mat original = new Mat();
                while (cameraActive) {
                    if (capture.read(original)) {
                        Mat frame = new Mat();
                        Imgproc.resize(original, frame, new Size(640, 480));
                        WritableImage imageToShow = mat2Image(frame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));
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
            });
            frameGrabber.setDaemon(true);
            frameGrabber.start();
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

    @Override
    public void stop() {
        cameraActive = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
