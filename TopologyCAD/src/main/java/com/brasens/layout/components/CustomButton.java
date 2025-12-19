package com.brasens.layout.components;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CustomButton extends StackPane {

    private ImageView buttonLabelImageView = new ImageView();
    private Label buttonLabel = new Label();
    private HBox contentBox = new HBox();

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>();
    private Timeline colorAnimation;

    private boolean isToggle = false;
    private boolean isActive = false;

    private Color defaultColor = Color.web("#2b2b2b");
    private Color hoverColor = Color.web("#3f3f3f");
    private Color activeColor = Color.web("#0078D7");

    private float animTimer = 200;

    public CustomButton(String text, Image image, String style, int imageSize){
        // 1. Configuração Visual (Icone e Texto)
        buttonLabelImageView = new ImageView(image);
        buttonLabelImageView.setFitHeight(imageSize);
        buttonLabelImageView.setFitWidth(imageSize);
        buttonLabelImageView.setPreserveRatio(true);
        buttonLabelImageView.setSmooth(true);

        buttonLabel = new Label(text);
        buttonLabel.setTextFill(Color.WHITE);
        buttonLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));

        contentBox.getChildren().addAll(buttonLabelImageView);
        if (!text.isEmpty()) {
            contentBox.getChildren().add(buttonLabel);
            contentBox.setSpacing(10);
        }
        contentBox.setAlignment(Pos.CENTER);

        this.getChildren().add(contentBox);
        this.setAlignment(Pos.CENTER);

        this.setMinWidth(40);
        this.setMinHeight(40);
        this.setCursor(Cursor.HAND);

        // Sombra suave
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0,0,0, 0.3));
        shadow.setRadius(5);
        shadow.setOffsetY(2);
        this.setEffect(shadow);

        // --- CORREÇÃO PRINCIPAL ---
        // Adiciona um ouvinte: Sempre que 'backgroundColor' mudar, aplica o CSS
        backgroundColor.addListener((obs, oldColor, newColor) -> applyBackgroundCSS(newColor));

        // Inicializa com a cor padrão
        backgroundColor.set(defaultColor);

        // Lógica de Toggle ao Clicar
        this.setOnMouseClicked(e -> {
            if (isToggle) {
                setActive(!isActive);
            }
        });
    }

    public void setAnimation(Color defaultColor, Color hoverColor, Color activeColor, float timer, boolean isToggle){
        this.defaultColor = defaultColor;
        this.hoverColor = hoverColor;
        this.activeColor = activeColor;
        this.animTimer = timer;
        this.isToggle = isToggle;

        // Atualiza cor inicial
        backgroundColor.set(isActive ? activeColor : defaultColor);

        setupVisualEffects();
    }

    public void setAnimation(Color defaultColor, Color hoverColor, float timer){
        setAnimation(defaultColor, hoverColor, hoverColor, timer, false);
    }

    // Método que converte a cor para CSS (Seguro contra Null/Preto)
    private void applyBackgroundCSS(Color c) {
        if (c == null) c = defaultColor;

        String css = String.format(
                "-fx-background-color: rgba(%d, %d, %d, %.2f); -fx-background-radius: 8px; -fx-border-radius: 8px;",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255),
                c.getOpacity()
        );
        this.setStyle(css);
    }

    public void setActive(boolean active) {
        this.isActive = active;
        animateToColor(isActive ? activeColor : defaultColor);
    }

    private void setupVisualEffects() {
        // Efeito de Zoom
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), this);
        scaleUp.setToX(1.05); scaleUp.setToY(1.05);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), this);
        scaleDown.setToX(1.0); scaleDown.setToY(1.0);

        this.setOnMouseEntered(event -> {
            scaleUp.playFromStart();
            Color target = isActive ? activeColor : hoverColor;
            animateToColor(target);
        });

        this.setOnMouseExited(event -> {
            scaleDown.playFromStart();
            Color target = isActive ? activeColor : defaultColor;
            animateToColor(target);
        });
    }

    private void animateToColor(Color targetColor) {
        if (colorAnimation != null) {
            colorAnimation.stop();
        }

        colorAnimation = new Timeline(
                new KeyFrame(Duration.millis(animTimer),
                        new KeyValue(backgroundColor, targetColor, Interpolator.EASE_OUT)
                )
        );

        colorAnimation.play();
    }
}