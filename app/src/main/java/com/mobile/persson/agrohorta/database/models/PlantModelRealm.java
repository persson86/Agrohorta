package com.mobile.persson.agrohorta.database.models;

import io.realm.RealmObject;

public class PlantModelRealm extends RealmObject {
    private String plantName;
    private String plantImage;

    public PlantModelRealm() {
    }

    public PlantModelRealm(String name, String image) {
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
