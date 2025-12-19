package com.brasens.layout.components.navbar;

import com.brasens.layout.ApplicationWindow;
import com.brasens.utils.Page;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NavigationButton extends AnchorPane {
    private final Page page;
    private final ApplicationWindow applicationWindow;

    public NavigationButton(Page page, ApplicationWindow applicationWindow) {
        this.page = page;
        this.applicationWindow = applicationWindow;
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onClick();
            }
        });
    }

    private void onClick() {
        applicationWindow.changePage(this.getPage());
    }
}
