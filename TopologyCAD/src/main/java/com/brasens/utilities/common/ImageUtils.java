package com.brasens.utilities.common;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageUtils {
    public static Image colorizeImage(Image originalImage, Color oldColor, Color targetColor) {
        WritableImage coloredImage = new WritableImage((int) originalImage.getWidth(), (int) originalImage.getHeight());

        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = coloredImage.getPixelWriter();

        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                Color originalColor = pixelReader.getColor(x, y);
                if (originalColor.equals(oldColor)) {
                    pixelWriter.setColor(x, y, targetColor);
                } else {
                    pixelWriter.setColor(x, y, originalColor);
                }
            }
        }

        return coloredImage;
    }

    public static Image colorizeImage(Image originalImage, Color targetColor) {
        WritableImage coloredImage = new WritableImage((int) originalImage.getWidth(), (int) originalImage.getHeight());

        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = coloredImage.getPixelWriter();

        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                Color originalColor = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, new Color(targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue(), originalColor.getOpacity()));
            }
        }

        return coloredImage;
    }
}
