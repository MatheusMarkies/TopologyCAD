package com.brasens;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.TimeZone;

public class Config {
    public static final String APP_VERSION = "SNAPSHOT 1.6.4";

    public static final String BACKEND_HOST = "";

    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("America/Sao_Paulo");

    public static final String NEW_SERVICE_ORDER_ICON = "/mspm/icons/add.png";
    public static final String SERVICES_ORDER_ICON = "/mspm/icons/calendar.png";
    public static final String DASHBOARD_ICON = "/mspm/icons/home.png";
    public static final String ASSETS_ICON = "/mspm/icons/coins.png";
    public static final String USER_NAVBAR_ICON = "/mspm/icons/users-alt.png";
    public static final String OPEN_NAVBAR_ICON = "/mspm/icons/menu-burger.png";
    public static final String CHANGE_THEME_ICON = "/mspm/icons/bell.png";
    public static final String NOTIFICATION_ICON = "/mspm/icons/bell.png";
    public static final String HELP_ICON = "/mspm/icons/bell.png";

    public static final String CHART_DATA_PATTERN = "dd/MM HH:mm:ss";
    public static Image getIcon(String icon){
        return new Image(Config.class.getResource(icon).toString());
    }

    public static String getFormattedDate(ZonedDateTime zonedDateTime){
        ZonedDateTime destinationDateTime = zonedDateTime.withZoneSameInstant(DEFAULT_TIMEZONE.toZoneId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CHART_DATA_PATTERN);
        return destinationDateTime.format(formatter);
    }

    public static ZonedDateTime getZonedDateTime(ZonedDateTime zonedDateTime){
        ZonedDateTime destinationDateTime = zonedDateTime.withZoneSameInstant(DEFAULT_TIMEZONE.toZoneId());
        return destinationDateTime;
    }

    public static String getColorPalleteProperties(String key) {
        Properties properties = new Properties();
        try (
                InputStream inputStream = CAD.class.getClassLoader().getResourceAsStream("mspm/color_pallate.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                System.err.println("File not found: color_pallate.properties");
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        return properties.getProperty(key);
    }

    public static String getTooltipTextProperties(String key) {
        Properties properties = new Properties();
        try (
                InputStream inputStream = CAD.class.getClassLoader().getResourceAsStream("mspm/tooltips_texts.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                System.err.println("File not found: tooltips_texts.properties");
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }

}
