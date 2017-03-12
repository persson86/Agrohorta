package com.mobile.persson.agrohorta.activities;

import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.firebase.database.DatabaseError;
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

@EActivity(R.layout.activity_register_plant_relation)
public class RegisterPlantRelationActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;

    @Bean
    ConfigApp configApp;
    @Bean
    StringHelper stringHelper;

    @ViewById(R.id.et_plant_1)
    EditText etPlant1;
    @ViewById(R.id.et_plant_2)
    EditText etPlant2;
    @ViewById
    RadioButton rbCompanion;
    @ViewById
    RadioButton rbAntagonistic;
    @ViewById(R.id.bt_register_relation)
    Button btRegisterRelation;

    @AfterViews
    void initialize() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Click
    void btRegisterRelation() {
        String deviceLanguage = configApp.getLanguageDevice();
        final String plant1 = stringHelper.convertToInputFormatNode(etPlant1.getText().toString());
        final String plant2 = stringHelper.convertToInputFormatNode(etPlant2.getText().toString());

        String nodeDatabase = "database";
        String nodeLanguage = "language_" + deviceLanguage;
        String nodeCompanionList = "companion_list";
        String nodeAntagonisticList = "antagonistic_list";

        String path;
        final String relationType;
        if (rbCompanion.isChecked()) {
            path = "/" + nodeDatabase + "/" + nodeLanguage + "/" + nodeCompanionList;
            relationType = "C";
        } else {
            path = "/" + nodeDatabase + "/" + nodeLanguage + "/" + nodeAntagonisticList;
            relationType = "A";
        }

        Map<String, String> mapRelation = new HashMap<>();
        mapRelation.put("plant1", etPlant1.getText().toString());
        mapRelation.put("plant2", etPlant2.getText().toString());

        mDatabase.child(path).push().setValue(mapRelation, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                String key = databaseReference.getKey();
                if (key != null) {
                    //TODO salva key na plant_list para ambas plantas
                    String path = "/" + "database" + "/" + "language_en" + "/" + "plant_list" + "/" + plant1;

                    Map<String, String> mapRelation = new HashMap<>();
                    mapRelation.put("nodeRelation", key);
                    mapRelation.put("relationType", relationType);

                    mDatabase.child(path).push().setValue(mapRelation);

                    path = "/" + "database" + "/" + "language_en" + "/" + "plant_list" + "/" + plant2;

                    mapRelation.clear();
                    mapRelation = new HashMap<>();
                    mapRelation.put("nodeRelation", key);
                    mapRelation.put("relationType", relationType);

                    mDatabase.child(path).push().setValue(mapRelation);
                }
            }
        });
    }
}

