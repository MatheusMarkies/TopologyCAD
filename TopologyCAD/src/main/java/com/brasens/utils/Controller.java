package com.brasens.utils;

import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
@Getter
@Setter
public abstract class Controller{

    protected boolean releasedUpdate = false;

    public int delay = 100;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public Controller() {
        executorService.scheduleAtFixedRate(() -> {
            Platform.runLater(this::update);
        }, 0, delay, TimeUnit.MILLISECONDS);
    }

    public abstract void init();
    public abstract void close();

    public abstract void update();

    public void setUpdate(boolean releasedUpdate) {
        this.releasedUpdate = releasedUpdate;
    }
    public Boolean isUpdatable() {
        return releasedUpdate;
    }

}
