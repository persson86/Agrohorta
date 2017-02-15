package com.mobile.persson.agrohorta.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobile.persson.agrohorta.R;
import com.mobile.persson.agrohorta.activities.ConfigApp;
import com.mobile.persson.agrohorta.models.PlantModel;

import org.androidannotations.annotations.Bean;

import java.util.ArrayList;
import java.util.List;

public class PlantListService extends IntentService {
    ArrayList<String> catalogoList = new ArrayList<>();
    ArrayList<String> compKeyList = new ArrayList<String>();
    ArrayList<String> antaKeyList = new ArrayList<String>();
    ArrayList<String> genericList = new ArrayList<String>();
    ArrayList<String> listInput = new ArrayList<>();

    String plantaInput;

    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String mNodeDatabase;
    private String mNodeLanguage;
    private String mNodePlantList;


    private Intent myIntent = new Intent(getString(R.string.service_plant_list));


    private static final String STEP_PROCESS = "STEP_PROCESS";
    private static final String GET_PLANT_LIST = "GET_PLANT_LIST";
    private static final String GET_IMAGES_FROM_STORAGE = "GET_IMAGES_FROM_STORAGE";
    private static final String GET_IMAGES_FROM_DEVICE = "GET_IMAGES_FROM_DEVICE";
    private static final String SAVE_IMAGES_INTO_DEVICE = "SAVE_IMAGES_INTO_DEVICE";
    private static final String GET_IMAGE_FROM_DEVICE = "GET_IMAGE_FROM_DEVICE";

    private List<PlantModel> mPlants;

    @Bean
    ConfigApp configApp;

    public PlantListService() {
        super(PlantListService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String stepProcess = intent.getStringExtra(STEP_PROCESS);
        switch (stepProcess) {
            case GET_PLANT_LIST:
                getPlantsList();
                break;
            case GET_IMAGES_FROM_STORAGE:
                break;
            case GET_IMAGES_FROM_DEVICE:
                break;
            case SAVE_IMAGES_INTO_DEVICE:
                break;
            case GET_IMAGE_FROM_DEVICE:
                break;
        }
    }

    public void getPlantsList() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_storage_url));
        mAuth = FirebaseAuth.getInstance();

        mNodeDatabase = getString(R.string.node_database);
        mNodePlantList = getString(R.string.node_plant_list);
        mNodeLanguage = getString(R.string.node_language) + configApp.getLanguageDevice();

        mPlants = new ArrayList<>();
        mDatabaseRef.child(mNodeDatabase).child(mNodeLanguage).child(mNodePlantList)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            PlantModel plant = data.getValue(PlantModel.class);
                            mPlants.add(plant);
                        }

                        callNextStep(GET_IMAGES_FROM_DEVICE);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(myIntent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //TODO tratar erros
                    }
                });
    }


    private void callNextStep(String stepProcess) {
        Bundle extras = new Bundle();
        extras.putString(STEP_PROCESS, stepProcess);
        myIntent.putExtras(extras);
    }
}