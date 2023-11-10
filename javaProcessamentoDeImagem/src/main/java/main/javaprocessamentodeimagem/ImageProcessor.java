package main.javaprocessamentodeimagem;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.opencv.core.CvType;

public class ImageProcessor extends Application {

    private ImageView originalImageView;
    private ImageView processedImageView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Processor");

        originalImageView = new ImageView();
        processedImageView = new ImageView();

        Button openButton = new Button("Open Image");
        openButton.setOnAction(e -> openImage());

        Button processButton = new Button("Process Image");
        processButton.setOnAction(e -> processImage());

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(openButton, processButton);

        HBox imageBox = new HBox(10);
        imageBox.getChildren().addAll(originalImageView, processedImageView);

        Scene scene = new Scene(new HBox(hbox, imageBox), 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            Mat originalImage = Imgcodecs.imread(selectedFile.getAbsolutePath());
            displayImage(originalImage, originalImageView);
        }
    }

    private void processImage() {
        if (originalImageView.getImage() != null) {
            Image originalImage = originalImageView.getImage();
            Mat originalMat = bufferedImageToMat(SwingFXUtils.fromFXImage(originalImage, null));
            Mat processedMat = applyGaussianBlur(originalMat);

            Image processedImage = matToImage(processedMat);
            processedImageView.setImage(processedImage);
        }
    }

    private void displayImage(Mat imageMat, ImageView imageView) {
        Image image = matToImage(imageMat);
        imageView.setImage(image);
    }

    private Mat applyGaussianBlur(Mat inputImage) {
        Mat outputImage = new Mat();
        Imgproc.GaussianBlur(inputImage, outputImage, new org.opencv.core.Size(5, 5), 0, 0);
        return outputImage;
    }

    private Mat bufferedImageToMat(java.awt.image.BufferedImage bufferedImage) {
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        byte[] data = ((java.awt.image.DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private Image matToImage(Mat mat) {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".png", mat, byteMat);
        byte[] byteArray = byteMat.toArray();
        try {
            return new Image(new ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
