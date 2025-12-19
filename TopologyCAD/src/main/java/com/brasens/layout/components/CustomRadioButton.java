package com.brasens.layout.components;

import javafx.scene.control.RadioButton;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CustomRadioButton extends RadioButton {

    private String enumText = "";

    public CustomRadioButton(String text, String enumText) {
        super(text);
        this.enumText = enumText;
    }
}
