package com.mobile.persson.agrohorta.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.mobile.persson.agrohorta.adapters.ContentAdapter;
import com.mobile.persson.agrohorta.database.dao.PlantsDAO;
import com.mobile.persson.agrohorta.database.models.PlantModel;
import com.mobile.persson.agrohorta.database.models.PlantModelRealm;
import com.mobile.persson.agrohorta.services.PlantListService_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mProgressDialog;

    private String mNodeDatabase;
    private String mNodeLanguage;
    private String mNodePlants;

    private List<PlantModelRealm> mPlants;
    private long plantsCount;

    @Bean
    ConfigApp configApp;
    @Bean
    PlantsDAO plantsDAO;

    @ViewById
    Toolbar toolbar;
    @ViewById
    TextView tvToolbarTitle;
    @ViewById
    RecyclerView content;
    @ViewById
    ImageView ivPlantImage;
    @ViewById
    TextView tvPlantName;

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
        setPlants();
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
        mNodePlants = getString(R.string.node_plant_list);
        mNodeLanguage = getString(R.string.node_language) + configApp.getLanguageDevice();
    }

    private void setPlants() {
        mPlants = new ArrayList<>();
        mPlants = plantsDAO.getPlants();

        if (mPlants.isEmpty()) {
            getPlantsList();
        } else {
            getPlantsListCount();
            //setContentAdapter();
        }
    }

    @Background()
    public void getPlantsList() {
        mPlants = new ArrayList<>();
        mDatabaseRef.child(mNodeDatabase).child(mNodeLanguage).child(mNodePlants)
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
                        setContentAdapter();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //TODO tratar erros
                    }
                });
    }

    @Background()
    public void getPlantsListCount() {
        mPlants = new ArrayList<>();
        mDatabaseRef.child(mNodeDatabase).child(mNodeLanguage).child(mNodePlants)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        plantsCount = dataSnapshot.getChildrenCount();

                        mPlants = new ArrayList<>();
                        mPlants = plantsDAO.getPlants();
                        if (mPlants.size() != plantsCount){
                            getPlantsList();
                        }
                        else
                            setContentAdapter();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //TODO tratar erros
                    }
                });
    }

    private void setContentAdapter() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 3);
        content.setLayoutManager(layoutManager);
        content.setHasFixedSize(true);
        ContentAdapter adapter = new ContentAdapter(getApplicationContext(), mPlants);
        content.setAdapter(adapter);
        mProgressDialog.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
