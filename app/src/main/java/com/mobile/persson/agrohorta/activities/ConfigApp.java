package com.mobile.persson.agrohorta.activities;

import org.androidannotations.annotations.EBean;

import java.util.Locale;

@EBean(scope = EBean.Scope.Singleton)
public class ConfigApp {

    public String getLanguageDevice() {
        return Locale.getDefault().getLanguage();
    }

}
