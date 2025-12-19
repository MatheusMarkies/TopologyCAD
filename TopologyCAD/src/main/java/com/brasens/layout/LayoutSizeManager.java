package com.brasens.layout;

import javafx.geometry.Insets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter @Setter @AllArgsConstructor
public class LayoutSizeManager {
    public static int ReferenceScreenWidth = 1920, ReferenceScreenHeight = 1080;
    public int screenWidth, screenHeight;

    public static double PageDefaultSideOffset = 160.0;
    public static double AnchorPaneDefaultLeftAnchor = 20.0;

    public static int getScreenHeight(){
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        return gd.getDisplayMode().getHeight();
    }
    public static int getScreenWidth(){
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        return gd.getDisplayMode().getWidth();
    }
    public static double getScreenAreaRatio(){
        return (double)(ReferenceScreenWidth * ReferenceScreenHeight) /
                (double)(getScreenWidth() * getScreenHeight());
    }
    public static double getInverseScreenAreaRatio(){
        return (double)(1/getScreenAreaRatio());
    }
    public static double getPageSideOffset(){
        return PageDefaultSideOffset/ReferenceScreenWidth * getScreenWidth();
    }
    public static double getAnchorPaneDefaultLeftAnchor(){
        return AnchorPaneDefaultLeftAnchor/ReferenceScreenWidth * getScreenWidth();
    }

    public static double getResizedWidth(double o){
        return o/ReferenceScreenWidth * getScreenWidth();
    }
    public static double getResizedHeight(double o){
        return o/ReferenceScreenHeight * getScreenHeight();
    }
    public static Insets getResizedInsert(double top, double right, double left, double bottom){
        return new Insets(
                getResizedHeight(top),
                getResizedWidth(right),
                getResizedWidth(left),
                getResizedHeight(bottom)
        );
    }
}

