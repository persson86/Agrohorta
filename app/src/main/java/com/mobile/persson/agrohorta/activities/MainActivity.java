package com.mobile.persson.agrohorta.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobile.persson.agrohorta.R;
import com.mobile.persson.agrohorta.database.dao.PlantsDAO;
import com.mobile.persson.agrohorta.database.models.PlantModel;
import com.mobile.persson.agrohorta.database.models.PlantModelRealm;
import com.mobile.persson.agrohorta.services.PlantListService_;
import com.mobile.persson.agrohorta.utils.ImageHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mProgressDialog;

    private String mNodeDatabase;
    private String mNodeLanguage;
    private String mNodePlantList;

    private List<PlantModelRealm> mPlants;

    @Bean
    ConfigApp configApp;
    @Bean
    PlantsDAO plantsDAO;

    @ViewById
    Toolbar toolbar;
    @ViewById
    TextView tvToolbarTitle;
    @ViewById
    ImageView ivTeste;
    @ViewById
    ImageView ivTeste2;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @AfterViews
    void initialize() {
        startDialog();
        loadToolbar();
        configFirebase();
        mPlants = new ArrayList<>();
        mPlants = plantsDAO.getPlants();
        if (mPlants.isEmpty())
            getPlantList();
        else
            showImages();

        //callIntentService("GET_PLANT_LIST");
    }

    private void startDialog() {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle(getString(R.string.wait));
        mProgressDialog.setMessage(getString(R.string.getting_data));
        mProgressDialog.show();
    }

    private void loadToolbar() {
        setSupportActionBar(toolbar);
        tvToolbarTitle.setText(getString(R.string.list_of_plants));
    }

    @Background
    public void callIntentService(String step) {
        Intent it = new Intent(getApplicationContext(), PlantListService_.class);
        it.putExtra("STEP_PROCESS", step);
        startService(it);
    }

    private void configFirebase() {
        setFirebaseReferences();
        setFirebaseNodes();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                } else {
                }
            }
        };
    }

    private void setFirebaseReferences() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_storage_url));
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void setFirebaseNodes() {
        mNodeDatabase = getString(R.string.node_database);
        mNodePlantList = getString(R.string.node_plant_list);
        mNodeLanguage = getString(R.string.node_language) + configApp.getLanguageDevice();
    }

    @Background()
    public void getPlantList() {
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
                        showImages();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //TODO tratar erros
                    }
                });
    }

    @UiThread
    public void showImages() {

        StorageReference imageRef = mStorageRef.child(getString(R.string.node_folder_images));

        int i = 0;
        for (PlantModelRealm p : mPlants) {

            ImageView iv = new ImageView(getApplicationContext());

            if (i == 0) {
                // Load the image using Glide
                Glide.with(this /* context */)
                        .using(new FirebaseImageLoader())
                        .load(imageRef.child(p.getPlantImage()))
                        .into(ivTeste);
            } else {
                Glide.with(this /* context */)
                        .using(new FirebaseImageLoader())
                        .load(imageRef.child(p.getPlantImage()))
                        .into(ivTeste2);
            }

            i++;

        }

        mProgressDialog.dismiss();

    }

/*    @Click
    void ivProfile() {
        LoginActivity_.intent(getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }*/

    @Override
    public void onStart() {
        super.onStart();
//        mAuth.addAuthStateListener(mAuthListener);
        LocalBroadcastManager.getInstance(this).registerReceiver(MyReceiver, new IntentFilter("service_plant_list"));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(MyReceiver);
    }

    //region BroadCastReceiver
    private BroadcastReceiver MyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String stepProcess = intent.getStringExtra("STEP_PROCESS");

            switch (stepProcess) {
                case "GET_IMAGES_FROM_STORAGE":
                    callIntentService("GET_IMAGES_FROM_STORAGE");
                    break;
                case "DONE":
                    //mPlantList = plantsDAO.getPlants();
                    mProgressDialog.dismiss();
                    int i = 0;
                    break;
            }
        }
    };
    //endregion
}
