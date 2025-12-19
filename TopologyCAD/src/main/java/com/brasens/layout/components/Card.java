package com.brasens.layout.components;

import com.brasens.CAD;
import com.brasens.layout.LayoutSizeManager;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Card extends AnchorPane {

    private VBox cardStruct = new VBox();
    private Label cardHeaderLabel = new Label();
    private HBox cardHeaderHBox = new HBox();
    private HBox cardTitleHBox = new HBox();
    private Label cardBottomLabel = new Label();
    private Label cardTitleLabel = new Label();
    private AnchorPane contentAnchorPane = new AnchorPane();

    private String tooltipText = "";

    public Card(String header, String title, String bottom,String tooltipText){
        getStyleClass().add("card");
        setPrefHeight(LayoutSizeManager.getResizedHeight(450.0));
        setPrefWidth(LayoutSizeManager.getResizedWidth(300.0));
        setMinHeight(AnchorPane.USE_PREF_SIZE);
        setMinWidth(AnchorPane.USE_PREF_SIZE);

        HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS);

        cardStruct = new VBox();
        cardStruct.setPrefHeight(LayoutSizeManager.getResizedHeight(450.0));
        cardStruct.setPrefWidth(LayoutSizeManager.getResizedWidth(300.0));
        cardStruct.setMinHeight(AnchorPane.USE_PREF_SIZE);
        cardStruct.setMinWidth(AnchorPane.USE_PREF_SIZE);

        AnchorPane.setBottomAnchor(cardStruct, 0.0);
        AnchorPane.setLeftAnchor(cardStruct, 0.0);
        AnchorPane.setRightAnchor(cardStruct, 0.0);
        AnchorPane.setTopAnchor(cardStruct, 0.0);

        cardHeaderHBox = new HBox();

        cardHeaderLabel = new Label(header);
        cardHeaderLabel.setFont(new Font((int)(24.0 * LayoutSizeManager.getInverseScreenAreaRatio())));

        //VBox.setVgrow(cardHeaderHBox, javafx.scene.layout.Priority.ALWAYS);
        VBox.setMargin(cardHeaderHBox, LayoutSizeManager.getResizedInsert(0.0, 0.0, 0.0, 5.0));
        cardHeaderHBox.setAlignment(Pos.CENTER_LEFT);

        cardHeaderHBox.getChildren().addAll(cardHeaderLabel);

        cardTitleLabel = new Label(title);
        cardTitleLabel.setFont(new Font((int)(24.0 * LayoutSizeManager.getInverseScreenAreaRatio())));
        //VBox.setVgrow(cardTitleLabel, javafx.scene.layout.Priority.ALWAYS);
        VBox.setMargin(cardTitleLabel, LayoutSizeManager.getResizedInsert(10.0, 0.0, 0.0, 5.0));

        cardTitleHBox = new HBox();

        cardTitleLabel = new Label(title);
        cardTitleLabel.setFont(new Font((int)(24.0 * LayoutSizeManager.getInverseScreenAreaRatio())));

        AnchorPane questionImageAnchor = new AnchorPane();

        ImageView questionImage = new ImageView(new Image(CAD.class.getResource("/mspm/icons/exclamation.png").toString()));
        questionImage.setFitHeight(LayoutSizeManager.getResizedHeight(12));
        questionImage.setFitWidth(LayoutSizeManager.getResizedWidth(12));
        questionImage.setPreserveRatio(true);

        questionImageAnchor.getChildren().addAll(questionImage);

        questionImage.setCursor(Cursor.HAND);
        Tooltip.install(questionImage, new CustomToolTip(tooltipText));

        VBox.setMargin(cardTitleHBox, LayoutSizeManager.getResizedInsert(0.0, 15.0, 15.0, 5.0));
        cardTitleHBox.setAlignment(Pos.CENTER_LEFT);

        cardTitleHBox.getChildren().addAll(cardTitleLabel, questionImage);

        Separator separator = new Separator();
        separator.setMaxHeight(5);
        VBox.setMargin(separator, LayoutSizeManager.getResizedInsert(0.0,10.0, 0.0,0.0));

        contentAnchorPane = new AnchorPane();
        HBox contentHBox = new HBox();

        VBox.setVgrow(contentHBox, javafx.scene.layout.Priority.ALWAYS);
        VBox.setMargin(contentHBox, LayoutSizeManager.getResizedInsert(0.0, 0.0, 0.0, 5.0));
        contentHBox.setAlignment(Pos.CENTER);

        HBox.setHgrow(contentAnchorPane, javafx.scene.layout.Priority.ALWAYS);
        contentHBox.getChildren().addAll(contentAnchorPane);

        HBox cardBottomHBox = new HBox();

        cardBottomLabel = new Label(bottom);

        VBox.setMargin(cardBottomHBox, LayoutSizeManager.getResizedInsert(0.0, 0.0, 0.0, 5.0));
        cardBottomHBox.setAlignment(Pos.CENTER_LEFT);

        cardBottomHBox.getChildren().addAll(cardBottomLabel);

        cardStruct.getChildren().addAll(
                cardHeaderHBox,
                cardTitleHBox,
                separator,
                contentHBox,
                cardBottomHBox
        );

        getChildren().add(cardStruct);

        setPadding(LayoutSizeManager.getResizedInsert(0.0, 0.0, 0.0, 10.0));
        HBox.setMargin(this, LayoutSizeManager.getResizedInsert(0.0, 0.0, 0.0, 10.0));
    }
}
