package com.brasens.layout;

import com.brasens.NetworkManager;
import com.brasens.layout.components.TopMenu;
import com.brasens.layout.components.navbar.NavBar;
import com.brasens.layout.view.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewManager {
    private NavBar navBar;
    private TopMenu topMenu;


    private DashboardView dashboardView;

    public ViewManager(ApplicationWindow applicationWindow, NetworkManager networkManager) {
        dashboardView = new DashboardView(applicationWindow, networkManager);
    }

    public void setup(ApplicationWindow applicationWindow){
        navBar = new NavBar(applicationWindow);
        topMenu = new TopMenu(applicationWindow);
    }
}
