package com.mobile.persson.agrohorta.models;

public class PlantModel {
    private String plantName;
    private String plantImage;

    public PlantModel() {
    }

    public PlantModel(String name, String image) {
        this.plantName = name;
        this.plantImage = image;
    }

    public String getPlantName() {
        return plantName;
    }

    public String getPlantImage() {
        return plantImage;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public void setPlantImage(String plantImage) {
        this.plantImage = plantImage;
    }
}
