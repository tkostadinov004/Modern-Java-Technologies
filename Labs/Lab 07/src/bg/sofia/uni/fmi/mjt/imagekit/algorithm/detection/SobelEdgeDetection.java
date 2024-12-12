package bg.sofia.uni.fmi.mjt.imagekit.algorithm.detection;

import bg.sofia.uni.fmi.mjt.imagekit.algorithm.ImageAlgorithm;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class SobelEdgeDetection implements EdgeDetectionAlgorithm {
    private ImageAlgorithm grayscale;
    public SobelEdgeDetection(ImageAlgorithm grayscaleAlgorithm) {
        this.grayscale = grayscaleAlgorithm;
    }

    private static double convolution(int[][] pixelMatrix) {
        final int minusOne = -1;
        final int minusTwo = -2;
        final int two = 2;
        int gy = (pixelMatrix[0][0] * minusOne)
                + (pixelMatrix[0][1] * minusTwo)
                + (pixelMatrix[0][2] * minusOne)
                + (pixelMatrix[2][0])
                + (pixelMatrix[2][1] * two)
                + (pixelMatrix[2][2]);
        int gx = (pixelMatrix[0][0])
                + (pixelMatrix[0][2] * minusOne)
                + (pixelMatrix[1][0] * two)
                + (pixelMatrix[1][2] * minusTwo)
                + (pixelMatrix[2][0])
                + (pixelMatrix[2][2] * minusOne);
        return Math.sqrt((gx * gx) + (gy * gy));
    }

    @Override
    public BufferedImage process(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null!");
        }

        BufferedImage result = grayscale.process(image);
        final int matrixRows = 3;
        final int matrixCols = 3;
        int[][] pixelMatrix = new int[matrixRows][matrixCols];
        for (int i = 1; i < result.getWidth() - 1; i++) {
            for (int j = 1; j < result.getHeight() - 1; j++) {
                pixelMatrix[0][0] = new Color(image.getRGB(i - 1, j - 1)).getRed();
                pixelMatrix[0][1] = new Color(image.getRGB(i - 1, j)).getRed();
                pixelMatrix[0][2] = new Color(image.getRGB(i - 1, j + 1)).getRed();
                pixelMatrix[1][0] = new Color(image.getRGB(i, j - 1)).getRed();
                pixelMatrix[1][1] = new Color(image.getRGB(i, j)).getRed();
                pixelMatrix[1][2] = new Color(image.getRGB(i, j + 1)).getRed();
                pixelMatrix[2][0] = new Color(image.getRGB(i + 1, j - 1)).getRed();
                pixelMatrix[2][1] = new Color(image.getRGB(i + 1, j)).getRed();
                pixelMatrix[2][2] = new Color(image.getRGB(i + 1, j + 1)).getRed();

                int edge = (int)convolution(pixelMatrix);
                final int shift16 = 16;
                final int shift8 = 16;
                result.setRGB(i, j, (edge << shift16 | edge << shift8 | edge));
            }
        }

        return result;
    }
}
