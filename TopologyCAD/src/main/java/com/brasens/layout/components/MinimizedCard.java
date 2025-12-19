package com.brasens.layout.components;

import com.brasens.layout.LayoutSizeManager;
import com.brasens.utils.BadgeStyle;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class MinimizedCard extends AnchorPane {
    private VBox cardStruct;
    private HBox cardTitleHeaderHBox;
    private Label cardTitleLabel;
    private Label cardValueLabel;

    private Badge cardBadge;

    private AnchorPane contentAnchorPane;

    public MinimizedCard(String title, String value, String badgeValue, BadgeStyle badgeStyle){
        getStyleClass().add("card");
        setPrefHeight(LayoutSizeManager.getResizedHeight(150.0));
        setPrefWidth(LayoutSizeManager.getResizedWidth(300.0));
        setMinHeight(AnchorPane.USE_PREF_SIZE);
        setMinWidth(AnchorPane.USE_PREF_SIZE);

        HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS);

        cardStruct = new VBox();
        cardStruct.setPrefHeight(LayoutSizeManager.getResizedHeight(200.0));
        cardStruct.setPrefWidth(LayoutSizeManager.getResizedWidth(100.0));
        cardStruct.setMinHeight(AnchorPane.USE_PREF_SIZE);
        cardStruct.setMinWidth(AnchorPane.USE_PREF_SIZE);
        AnchorPane.setBottomAnchor(cardStruct, 0.0);
        AnchorPane.setLeftAnchor(cardStruct, 0.0);
        AnchorPane.setRightAnchor(cardStruct, 0.0);
        AnchorPane.setTopAnchor(cardStruct, 0.0);

        cardTitleLabel = new Label(title);
        cardTitleLabel.setFont(new Font((int)(24.0 * LayoutSizeManager.getInverseScreenAreaRatio())));
        VBox.setVgrow(cardTitleLabel, javafx.scene.layout.Priority.ALWAYS);
        VBox.setMargin(cardTitleLabel, LayoutSizeManager.getResizedInsert(10.0, 0.0, 0.0, 5.0));

        HBox cardValueHBox = new HBox();

        cardValueLabel = new Label(value);
        cardValueLabel.setFont(new Font((int)(29.0 * LayoutSizeManager.getInverseScreenAreaRatio())));

        Region cardValueFiller = new Region(); HBox.setHgrow(cardValueFiller, Priority.ALWAYS);

        cardValueHBox.getChildren().addAll(cardValueFiller, cardValueLabel);

        VBox.setVgrow(cardValueHBox, javafx.scene.layout.Priority.ALWAYS);
        VBox.setMargin(cardValueHBox, LayoutSizeManager.getResizedInsert(10.0, 10.0, 0.0, 10.0));

        cardBadge = new Badge(badgeValue, badgeStyle);

        cardBadge.setMaxWidth(LayoutSizeManager.getResizedWidth(90));
        cardBadge.setPrefWidth(LayoutSizeManager.getResizedWidth(80));
        cardBadge.setMinWidth(LayoutSizeManager.getResizedWidth(80));

        VBox.setVgrow(cardBadge, javafx.scene.layout.Priority.ALWAYS);
        VBox.setMargin(cardBadge, LayoutSizeManager.getResizedInsert(5.0, 0.0, 20.0, 5.0));

        cardStruct.getChildren().addAll(
                cardTitleLabel,
                cardValueHBox,
                cardBadge
        );

        getChildren().add(cardStruct);

        setPadding(LayoutSizeManager.getResizedInsert(0.0, 0.0, 0.0, 10.0));
        HBox.setMargin(this, LayoutSizeManager.getResizedInsert(0.0, 0.0, 0.0, 10.0));
    }
}
