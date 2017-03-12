package com.mobile.persson.agrohorta.utils;

import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class StringHelper {
    public String convertToInputFormatNode(String inputPlant) {
        return inputPlant.replace(" ", "_");
    }

    public String convertToInputFormatImage(String plant) {
        return plant + ".png";
    }
}
