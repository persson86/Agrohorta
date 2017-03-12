package com.mobile.persson.agrohorta.activities;

import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobile.persson.agrohorta.R;
import com.mobile.persson.agrohorta.utils.StringHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Map;

@EActivity(R.layout.activity_register_plant)
public class RegisterPlantActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Bean
    ConfigApp configApp;
    @Bean
    StringHelper stringHelper;

    @ViewById(R.id.et_plant_en)
    EditText etPlantEn;
    @ViewById(R.id.et_plant_pt)
    EditText etPlantPt;
    @ViewById
    Button btRegisterPlant;

    @AfterViews
    void initialize() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Click
    void btRegisterPlant() {
        savePlant();
    }

    //TODO change method name
    private void savePlant() {
        String deviceLanguage = configApp.getLanguageDevice();
        String plantEn = etPlantEn.getText().toString();
        String plantImage = stringHelper.formatImageName(plantEn);
        String plantNodeEn = stringHelper.formatInputPlant(plantEn);

        String nodeDatabase = "database";
        String nodeLanguage = "language_" + deviceLanguage;
        String nodePlantList = "plant_list";
        String path = "/" + nodeDatabase + "/" + nodeLanguage + "/" + nodePlantList + "/" + plantNodeEn;

        Map<String, String> mapPlant = new HashMap<>();
        mapPlant.put("plantName", plantEn);
        mapPlant.put("plantImage", plantImage);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(path, mapPlant);

        mDatabase.updateChildren(childUpdates);
    }
}
