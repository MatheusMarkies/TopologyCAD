package com.brasens.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Update {
    String version = "";
    String URL = "";

    @Override
    public String toString() {
        return "Update{" +
                "version='" + version + '\'' +
                ", URL='" + URL + '\'' +
                '}';
    }
}
