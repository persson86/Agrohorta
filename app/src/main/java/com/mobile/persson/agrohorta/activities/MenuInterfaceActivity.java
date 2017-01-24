package com.mobile.persson.agrohorta.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.mobile.persson.agrohorta.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_menu_interface)
public class MenuInterfaceActivity extends AppCompatActivity {

    @ViewById
    Button btRegisterPlant;
    @ViewById
    Button btRegisterRelation;
    @ViewById
    Button btGoToApp;

    @Click
    void btRegisterPlant() {
        RegisterPlantActivity_.intent(getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }

    @Click
    void btRegisterRelation() {
        RegisterPlantRelationActivity_.intent(getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }

    @Click
    void btGoToApp() {
        MainActivity_.intent(getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }
}
