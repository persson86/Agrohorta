package com.mobile.persson.agrohorta.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobile.persson.agrohorta.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private GoogleApiClient mGoogleApiClient;

    private ProgressDialog mProgressDialog;
    private final static String TAG = "LFSP_DEBUG";
    FirebaseStorage mStorage;
    StorageReference mStorageRef;

    private String mDeviceLanguage;

    @Bean
    ConfigApp configApp;
    @ViewById
    Toolbar toolbar;
    @ViewById
    TextView tvToolbarTitle;
    @ViewById
    de.hdodenhof.circleimageview.CircleImageView ivProfile;

    @AfterViews
    void initialize() {

        startDialog();
        initializeApp();
        setAuthStateListener();
        googleAuthConfig();
        loadToolbar();

        FacebookSdk.sdkInitialize(getApplicationContext());

        teste();

        mProgressDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            UserInfo user = FirebaseAuth.getInstance().getCurrentUser();
            loadImageProfile(user.getPhotoUrl());
            Toast.makeText(this, user.getDisplayName(), Toast.LENGTH_SHORT).show();
        } else
            ivProfile.setImageResource(R.drawable.ic_account_circle_white_48dp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        tvToolbarTitle.setText("Catálogo de plantas");
    }

    private void initializeApp() {
        mDeviceLanguage = configApp.getLanguageDevice();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReferenceFromUrl("gs://agro-horta.appspot.com");
    }

    private void googleAuthConfig() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void setAuthStateListener() {
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if (user.getDisplayName() != null)
                        Toast.makeText(getApplicationContext(), user.getDisplayName(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void startDialog() {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle("buscando data");
        mProgressDialog.setMessage("aguarde");
        mProgressDialog.show();
    }

    private void loadImageProfile(Uri url) {
        Glide.with(ivProfile.getContext())
                .load(url)
                .into(ivProfile);
    }

    private void teste() {
        StorageReference imagesRef = mStorageRef.child("images");
        imagesRef.child("onion.png");
    }

    @Click
    void ivProfile() {
        LoginActivity_.intent(getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }
}
