package com.mobile.persson.agrohorta.models;

/**
 * Created by persson on 13/12/16.
 */

public class PlantModel {
    private String name;

    public PlantModel() {
    }

    public PlantModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
