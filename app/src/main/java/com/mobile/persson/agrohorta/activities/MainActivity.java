package com.mobile.persson.agrohorta.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.mobile.persson.agrohorta.R;
import com.mobile.persson.agrohorta.firebase.FirebaseHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    //Google
    private GoogleApiClient googleApiClient;

    private ProgressDialog progressDialog;
    private final String TAG = getClass().toString();

    @ViewById
    Toolbar toolbar;
    @ViewById
    TextView tvToolbarTitle;
    @ViewById
    de.hdodenhof.circleimageview.CircleImageView ivProfile;

    @AfterViews
    void initialize() {
        startDialog();
        setAuthStateListener();
        googleAuthConfig();
        loadToolbar();

        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);

        progressDialog.dismiss();
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

    private void googleAuthConfig() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void setAuthStateListener() {
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
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
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("buscando data");
        progressDialog.setMessage("aguarde");
        progressDialog.show();
    }

    private void loadImageProfile(Uri url) {
        Glide.with(ivProfile.getContext())
                .load(url)
                .into(ivProfile);
    }

    @Click
    void ivProfile() {
        LoginActivity_.intent(getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }
}
