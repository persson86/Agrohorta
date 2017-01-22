package com.mobile.persson.agrohorta.utils;

import org.androidannotations.annotations.EBean;

/**
 * Created by persson on 22/01/17.
 */

@EBean(scope = EBean.Scope.Singleton)
public class StringHelper {
    public String formatInputPlant(String inputPlant) {
        return inputPlant = inputPlant.replace(" ", "_");
    }

    public String formatImageName(String plant) {
        return plant + ".png";
    }
}
