package com.mobile.persson.agrohorta.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.mobile.persson.agrohorta.database.dao.PlantsDAO;
import com.mobile.persson.agrohorta.database.models.PlantModel;
import com.mobile.persson.agrohorta.database.models.PlantModelRealm;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@EIntentService
public class PlantListService extends IntentService {
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private String mNodeDatabase;
    private String mNodeLanguage;
    private String mNodePlantList;

    private Intent myIntent = new Intent("service_plant_list");

    private static final String STEP_PROCESS = "STEP_PROCESS";
    private static final String GET_PLANT_LIST = "GET_PLANT_LIST";
    private static final String GET_IMAGES_FROM_STORAGE = "GET_IMAGES_FROM_STORAGE";
    private static final String GET_IMAGES_FROM_DEVICE = "GET_IMAGES_FROM_DEVICE";
    private static final String SAVE_IMAGES_INTO_DEVICE = "SAVE_IMAGES_INTO_DEVICE";
    private static final String GET_IMAGE_FROM_DEVICE = "GET_IMAGE_FROM_DEVICE";
    private final long ONE_MEGABYTE = 1024 * 1024;

    private List<PlantModelRealm> mPlants;
    private HashMap<byte[], PlantModel> mHashMap = new HashMap<>();

    @Bean
    ConfigApp configApp;
    @Bean
    PlantsDAO plantsDAO;

    public PlantListService() {
        super(PlantListService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        setFirebaseInstance();

        String stepProcess = intent.getStringExtra(STEP_PROCESS);
        switch (stepProcess) {
            case GET_PLANT_LIST:
                getPlantsList();
                break;
            case GET_IMAGES_FROM_STORAGE:
                getImagesFromStorage();
                break;
            case GET_IMAGES_FROM_DEVICE:
                break;
            case SAVE_IMAGES_INTO_DEVICE:
                break;
            case GET_IMAGE_FROM_DEVICE:
                break;
        }
    }

    private void setFirebaseInstance() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_storage_url));
        mAuth = FirebaseAuth.getInstance();

        mNodeDatabase = getString(R.string.node_database);
        mNodePlantList = getString(R.string.node_plant_list);
        mNodeLanguage = getString(R.string.node_language) + configApp.getLanguageDevice();
    }

    private void getPlantsList() {
        mPlants = new ArrayList<>();
        mDatabaseRef.child(mNodeDatabase).child(mNodeLanguage).child(mNodePlantList)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            PlantModel receivePlant = data.getValue(PlantModel.class);
                            PlantModelRealm plant = new PlantModelRealm();
                            plant.setPlantName(receivePlant.getPlantName());
                            plant.setPlantImage(receivePlant.getPlantImage());
                            mPlants.add(plant);
                        }

                        plantsDAO.savePlants(mPlants);
                        callNextStep(GET_IMAGES_FROM_STORAGE);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(myIntent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //TODO tratar erros
                    }
                });
    }

    private void getImagesFromStorage() {
        StorageReference imageRef = mStorageRef.child(getString(R.string.node_folder_images));

        final AtomicInteger index = new AtomicInteger();
        index.set(0);

        mPlants = plantsDAO.getPlants();

        for (final PlantModelRealm plant : mPlants) {
            if (plant.equals(""))
                continue;

            imageRef = imageRef.child(plant.getPlantImage());
            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    int count = index.get();
                    count = count + 1;
                    index.lazySet(count);

                    //saveImageToDevice(bytes, plant);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //TODO Handle any errors
                }
            });
        }

        while (mPlants.size() != index.get()) {
            continue;
        }

        callNextStep("DONE");
    }

    private void callNextStep(String stepProcess) {
        Bundle extras = new Bundle();
        extras.putString(STEP_PROCESS, stepProcess);
        myIntent.putExtras(extras);
    }
}
