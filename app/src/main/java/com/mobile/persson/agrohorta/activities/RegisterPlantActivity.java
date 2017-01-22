package com.mobile.persson.agrohorta.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobile.persson.agrohorta.R;
import com.mobile.persson.agrohorta.models.PlantModel;
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
    @ViewById(R.id.bt_register_plant)
    Button btRegisterPlant;

    @AfterViews
    void initialize() {
/*        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("gs://agro-horta.appspot.com");

        // Create a child reference
        // imagesRef now points to "images"
        StorageReference imagesRef = storageRef.child("images");*/

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Click
    void btRegisterPlant() {
        logicaFirebase();
    }

    //TODO change method name
    private void logicaFirebase() {
        String deviceLanguage = configApp.getLanguageDevice();

        String plantEn = stringHelper.formatInputPlant(etPlantEn.getText().toString());

        String nodeLanguage = "language_" + deviceLanguage;
        String nodePlantList = "plant_list";

        String image = stringHelper.formatImageName(plantEn);

        //mDatabase.child("database").child(deviceLanguage).child(nodePlantList).child(plantEn).setValue(image);

        String key = mDatabase.child("database").child(nodeLanguage).child(nodePlantList).child(plantEn).push().getKey();

        PlantModel plant = new PlantModel();
        plant.setImage(image);
        plant.setPlant(plantEn);

        Map<String, String> mapPlant = new HashMap<>();
        mapPlant.put("name", plantEn);
        mapPlant.put("image", image);


        String path = "/database/" + nodeLanguage + "/" + nodePlantList + "/" + plantEn;

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(path, mapPlant);

        mDatabase.updateChildren(childUpdates);


    }
}
