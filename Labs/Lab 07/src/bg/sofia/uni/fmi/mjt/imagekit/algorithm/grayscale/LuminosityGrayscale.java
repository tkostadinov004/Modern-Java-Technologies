package bg.sofia.uni.fmi.mjt.imagekit.algorithm.grayscale;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class LuminosityGrayscale implements GrayscaleAlgorithm {
    @Override
    public BufferedImage process(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null!");
        }

        final float redGrayscale = 0.21f;
        final float greenGrayscale = 0.72f;
        final float blueGrayscale = 0.07f;

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color pixelColor = new Color(image.getRGB(j, i));
                int modifiedRGB = (int)(redGrayscale * pixelColor.getRed() +
                        greenGrayscale * pixelColor.getGreen() +
                        blueGrayscale * pixelColor.getBlue());

                Color modifiedColor = new Color(modifiedRGB, modifiedRGB, modifiedRGB);
                result.setRGB(j, i, modifiedColor.getRGB());
            }
        }

        return result;
    }
}
