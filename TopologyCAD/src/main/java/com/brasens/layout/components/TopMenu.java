package com.brasens.layout.components;

import com.brasens.layout.ApplicationWindow;
import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class TopMenu extends AnchorPane {

    private ApplicationWindow applicationWindow;

    boolean navBarIsOpen = true;

    public TopMenu(){

    }

    public TopMenu(ApplicationWindow applicationWindow){
        this.applicationWindow = applicationWindow;


        HBox centerButtonHBox = new HBox();
        centerButtonHBox.setAlignment(Pos.CENTER);

        //getChildren().add(headerHBox);
    }

}
