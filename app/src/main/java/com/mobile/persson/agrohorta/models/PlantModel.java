package com.mobile.persson.agrohorta.models;

/**
 * Created by persson on 13/12/16.
 */

public class PlantModel {
    private String plant;
    private String image;

    public PlantModel() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public PlantModel(String plant) {
        this.plant = plant;
        this.image = image;

    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }
}
