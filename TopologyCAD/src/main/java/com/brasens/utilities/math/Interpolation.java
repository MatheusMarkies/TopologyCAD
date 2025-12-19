package com.brasens.utilities.math;

import java.awt.*;

public class Interpolation {
    public static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }
    public static Color lerp(Color v0, Color v1, float t) {
        return new Color(
                lerp((float) v0.getRed(), (float)v1.getRed(), t),
                lerp((float)v0.getGreen(), (float)v1.getGreen(), t),
                lerp((float)v0.getBlue(), (float)v1.getBlue(), t)
        );
    }
    public static javafx.scene.paint.Color lerpColorFX(javafx.scene.paint.Color v0, javafx.scene.paint.Color v1, float t) {
        Color color = new Color(
                lerp((float) v0.getRed(), (float)v1.getRed(), t),
                lerp((float)v0.getGreen(), (float)v1.getGreen(), t),
                lerp((float)v0.getBlue(), (float)v1.getBlue(), t)
        );

    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();

    int a = color.getAlpha();

    double opacity = a / 255.0 ;
    return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }
}
