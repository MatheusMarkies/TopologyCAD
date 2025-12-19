package com.brasens.layout.components;

import com.brasens.CAD;
import com.brasens.layout.LayoutSizeManager;
import com.brasens.utilities.common.ImageUtils;
import com.brasens.utils.BadgeStyle;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Badge extends AnchorPane {

    private ImageView badgeImageView = new ImageView();
    private Label label = new Label();

    public Badge(String text, BadgeStyle badgeStyle){
        this.setStyle("-fx-padding: 5px");

        badgeImageView = new ImageView();
        badgeImageView.setFitHeight(12);
        badgeImageView.setFitWidth(12);
        badgeImageView.setPreserveRatio(true);

        label = new Label(text);

        this.setMaxWidth(text.length() * 10);
        this.setMaxHeight(30);

        HBox badgeLegendHBox = new HBox();
        badgeLegendHBox.setAlignment(Pos.CENTER);

        HBox.setMargin(badgeImageView, LayoutSizeManager.getResizedInsert(2.0,0.0,2.0,5.0));
        HBox.setMargin(label, LayoutSizeManager.getResizedInsert(2.0,0.0,2.0,0.0));

        badgeLegendHBox.getChildren().addAll(label, badgeImageView);

        AnchorPane.setBottomAnchor(badgeLegendHBox, 0.0);
        AnchorPane.setLeftAnchor(badgeLegendHBox, 0.0);
        AnchorPane.setRightAnchor(badgeLegendHBox, 0.0);
        AnchorPane.setTopAnchor(badgeLegendHBox, 0.0);

        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        setBadgeStyle(badgeStyle);

        this.getChildren().add(badgeLegendHBox);
    }

    private String backgroundColor;
    private String textColor;

    public void setImage(Image image, Color color){
        this.badgeImageView.setImage(ImageUtils.colorizeImage(image, color));
    }

    public void setBadgeFont(Font font){
        label.setFont(font);
    }

    public void setBadgeStyle(BadgeStyle color) {
        Image backgroundImage = null;

        switch (color) {
            case SUCCESS:
                backgroundColor = "rgba(0, 134, 78, 0.2)";
                textColor = "#005037";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/check-circle.png").toString()),
                        Color.web(textColor));
                break;
            case DANGER:
                backgroundColor = "rgba(255, 0, 0, 0.2)";
                textColor = "#990000";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/exclamation.png").toString()),
                        Color.web(textColor));
                break;
            case WARNING:
                backgroundColor = "rgba(255, 204, 0, 0.2)";
                textColor = "#996600";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/exclamation.png").toString()),
                        Color.web(textColor));
                break;
            case PRIMARY:
                backgroundColor = "rgba(0, 0, 255, 0.2)";
                textColor = "#000099";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/check-circle.png").toString()),
                        Color.web(textColor));
                break;
            case INFO:
                backgroundColor = "rgba(128, 128, 128, 0.2)";
                textColor = "#404040";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/info.png").toString()),
                        Color.web(textColor));
                break;
            case COMPLETED:
                backgroundColor = "rgba(0, 134, 78, 0.2)";
                textColor = "#005037";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/check-circle.png").toString()),
                        Color.web(textColor));
                break;
            case PROGRESS:
                backgroundColor = "rgba(255, 0, 0, 0.2)";
                textColor = "#996600";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/clock.png").toString()),
                        Color.web(textColor));
                break;
            case OPENING:
                backgroundColor = "rgba(255, 0, 0, 0.2)";
                textColor = "#990000";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/clock.png").toString()),
                        Color.web(textColor));
                break;
            case PLANNING:
                backgroundColor = "rgba(255, 0, 0, 0.2)";
                textColor = "#996600";
                backgroundImage = ImageUtils.colorizeImage(new Image(CAD.class.getResource("/mspm/icons/calendar-clock.png").toString()),
                        Color.web(textColor));
                break;
            default:
                backgroundColor = "#ffffff";
                textColor = "#000000";
                break;
        }

        this.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 6;");
        this.label.setTextFill(javafx.scene.paint.Color.web(textColor));
        this.badgeImageView.setImage(backgroundImage);
    }

}
