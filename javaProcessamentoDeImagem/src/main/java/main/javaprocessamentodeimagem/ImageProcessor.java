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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.File;

public class ImageProcessor extends Application {

    private ImageView originalImageView;
    private ImageView processedImageView;

    public static void main(String[] args) {
        try {
            nu.pattern.OpenCV.loadLocally();
        } catch (Exception e) {
            System.err.println("Erro ao carregar a biblioteca OpenCV: " + e.getMessage());
            System.exit(1);
        }

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
            try {
                Mat originalImage = Imgcodecs.imread(selectedFile.getAbsolutePath());
                if (!originalImage.empty()) {
                    System.out.println("Imagem aberta com sucesso.");
                    displayImage(originalImage, originalImageView);
                } else {
                    System.err.println("Erro ao abrir a imagem: A imagem está vazia.");
                }
            } catch (Exception e) {
                System.err.println("Erro ao abrir a imagem: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void processImage() {
        if (originalImageView.getImage() != null) {
            Image originalImage = originalImageView.getImage();
            Mat originalMat = bufferedImageToMat(SwingFXUtils.fromFXImage(originalImage, null));
            Mat processedMat = applyGaussianBlur(originalMat);

            Image processedImage = matToImage(processedMat);
            processedImageView.setImage(processedImage);

            // Display original and processed images in a new window
            displaySideBySide(originalMat, processedMat);
        }
    }

    private void displayImage(Mat imageMat, ImageView imageView) {
        Image image = matToImage(imageMat);
        imageView.setImage(image);

        // Defina a escala da ImageView para ajustar a imagem dentro dela
        imageView.setFitWidth(300); // Defina a largura desejada (ajuste conforme necessário)
        imageView.setFitHeight(200); // Defina a altura desejada (ajuste conforme necessário)
    }

    private void displaySideBySide(Mat originalMat, Mat processedMat) {
        Stage popupStage = new Stage();

        ImageView originalView = new ImageView(matToImage(originalMat));
        ImageView processedView = new ImageView(matToImage(processedMat));

        // Defina a escala das ImageView para ajustar as imagens dentro delas
        originalView.setFitWidth(300); // Defina a largura desejada (ajuste conforme necessário)
        originalView.setFitHeight(200); // Defina a altura desejada (ajuste conforme necessário)

        processedView.setFitWidth(300); // Defina a largura desejada (ajuste conforme necessário)
        processedView.setFitHeight(200); // Defina a altura desejada (ajuste conforme necessário)

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(originalView, processedView);

        Scene popupScene = new Scene(hbox);
        popupStage.setScene(popupScene);
        popupStage.show();
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
