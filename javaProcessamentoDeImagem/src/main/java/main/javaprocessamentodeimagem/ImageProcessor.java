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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.File;

public class ImageProcessor extends Application {

    private ImageView visualizadorOriginal;
    private ImageView visualizadorProcessado;

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
        primaryStage.setTitle("Processador de Imagens");

        visualizadorOriginal = new ImageView();
        visualizadorProcessado = new ImageView();

        Button botaoAbrir = new Button("Abrir Imagem");
        botaoAbrir.setOnAction(e -> abrirImagem());

        Button botaoProcessar = new Button("Processar Imagem");
        botaoProcessar.setOnAction(e -> processarImagem());

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(botaoAbrir, botaoProcessar);

        HBox caixaImagens = new HBox(10);
        caixaImagens.getChildren().addAll(visualizadorOriginal, visualizadorProcessado);

        Scene cena = new Scene(new HBox(hbox, caixaImagens), 600, 400);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    // Método para abrir uma imagem
    private void abrirImagem() {
        FileChooser seletorArquivo = new FileChooser();
        seletorArquivo.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Imagem", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File arquivoSelecionado = seletorArquivo.showOpenDialog(null);

        if (arquivoSelecionado != null) {
            try {
                Mat imagemOriginal = Imgcodecs.imread(arquivoSelecionado.getAbsolutePath());
                if (!imagemOriginal.empty()) {
                    System.out.println("Imagem aberta com sucesso.");
                    exibirImagem(imagemOriginal, visualizadorOriginal);
                } else {
                    System.err.println("Erro ao abrir a imagem: A imagem está vazia.");
                }
            } catch (Exception e) {
                System.err.println("Erro ao abrir a imagem: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Método para processar a imagem
    private void processarImagem() {
        if (visualizadorOriginal.getImage() != null) {
            Image imagemOriginal = visualizadorOriginal.getImage();
            Mat matrizOriginal = bufferedImageParaMat(SwingFXUtils.fromFXImage(imagemOriginal, null));
            Mat matrizProcessada = aplicarDesfoqueGaussiano(matrizOriginal);

            Image imagemProcessada = matParaImagem(matrizProcessada);
            visualizadorProcessado.setImage(imagemProcessada);

            // Exibir imagens original e processada em uma nova janela
            exibirLadoALado(matrizOriginal, matrizProcessada);
        }
    }

    // Método para exibir uma imagem na interface gráfica
    private void exibirImagem(Mat matrizImagem, ImageView visualizador) {
        Image imagem = matParaImagem(matrizImagem);
        visualizador.setImage(imagem);
    }

    // Método para exibir imagens original e processada lado a lado
    private void exibirLadoALado(Mat matrizOriginal, Mat matrizProcessada) {
        Stage janelaPopup = new Stage();

        ImageView visualizadorOriginal = new ImageView(matParaImagem(matrizOriginal));
        ImageView visualizadorProcessado = new ImageView(matParaImagem(matrizProcessada));

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(visualizadorOriginal, visualizadorProcessado);

        Scene cenaPopup = new Scene(hbox);
        janelaPopup.setScene(cenaPopup);
        janelaPopup.show();
    }

    // Método para aplicar um desfoque gaussiano à imagem
    private Mat aplicarDesfoqueGaussiano(Mat imagemEntrada) {
        Mat imagemSaida = new Mat();

        // Calcular o tamanho do kernel com base na largura da imagem
        int tamanhoKernel = (int) (imagemEntrada.width() * 0.05);

        // Certificar-se de que o tamanho do kernel seja ímpar
        tamanhoKernel = (tamanhoKernel % 2 == 0) ? tamanhoKernel + 1 : tamanhoKernel;

        // Aplicar o filtro Gaussiano
        Imgproc.GaussianBlur(imagemEntrada, imagemSaida, new org.opencv.core.Size(tamanhoKernel, tamanhoKernel), 0, 0);

        return imagemSaida;
    }

    // Método para converter uma imagem em buffer para uma matriz
    private Mat bufferedImageParaMat(BufferedImage imagemBuffer) {
        Mat matriz;
        DataBuffer dataBuffer = imagemBuffer.getRaster().getDataBuffer();

        if (dataBuffer instanceof DataBufferByte) {
            byte[] dados = ((DataBufferByte) dataBuffer).getData();
            matriz = new Mat(imagemBuffer.getHeight(), imagemBuffer.getWidth(), CvType.CV_8UC3);
            matriz.put(0, 0, dados);
        } else if (dataBuffer instanceof DataBufferInt) {
            int[] dados = ((DataBufferInt) dataBuffer).getData();
            byte[] bytes = new byte[imagemBuffer.getWidth() * imagemBuffer.getHeight() * 4];

            int indicePixel = 0;
            for (int y = 0; y < imagemBuffer.getHeight(); y++) {
                for (int x = 0; x < imagemBuffer.getWidth(); x++) {
                    int pixel = dados[indicePixel++];
                    bytes[(y * imagemBuffer.getWidth() + x) * 4 + 2] = (byte) ((pixel >> 16) & 0xFF); // Componente Vermelho
                    bytes[(y * imagemBuffer.getWidth() + x) * 4 + 1] = (byte) ((pixel >> 8) & 0xFF);  // Componente Verde
                    bytes[(y * imagemBuffer.getWidth() + x) * 4] = (byte) (pixel & 0xFF);             // Componente Azul
                    bytes[(y * imagemBuffer.getWidth() + x) * 4 + 3] = (byte) ((pixel >> 24) & 0xFF); // Componente Alfa
                }
            }

            matriz = new Mat(imagemBuffer.getHeight(), imagemBuffer.getWidth(), CvType.CV_8UC4);
            matriz.put(0, 0, bytes);
            Imgproc.cvtColor(matriz, matriz, Imgproc.COLOR_BGRA2BGR);
        } else {
            throw new IllegalArgumentException("Tipo de DataBuffer não suportado: " + dataBuffer.getClass());
        }

        return matriz;
    }

    // Método para converter uma matriz em uma imagem
    private Image matParaImagem(Mat matriz) {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".png", matriz, byteMat);
        byte[] byteArray = byteMat.toArray();
        try {
            return new Image(new ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
