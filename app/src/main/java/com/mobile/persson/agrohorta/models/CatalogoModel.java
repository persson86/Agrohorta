package com.mobile.persson.agrohorta.models;

/**
 * Created by persson on 13/12/16.
 */

public class CatalogoModel {
    private String key;
    private String tipo;

    public CatalogoModel() {
    }

    public CatalogoModel(String key, String tipo) {
        key = key;
        tipo = tipo;
    }

    public String getKey() {
        return key;
    }

    public String getTipo() {
        return tipo;
    }
}
