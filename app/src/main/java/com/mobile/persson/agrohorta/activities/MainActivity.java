package com.mobile.persson.agrohorta.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mobile.persson.agrohorta.models.PlantModel;
import com.mobile.persson.agrohorta.utils.ImageHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
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
    private List<PlantModel> mPlantList;
    private List<Bitmap> mImageList = new ArrayList<>();
    private List<Bitmap> bmpList = new ArrayList<>();

    private String mNodeDatabase;
    private String mNodeLanguage;
    private String mNodePlantList;

    private HashMap<byte[], PlantModel> hashMap = new HashMap<>();

    private final static String TAG = "LFSP_DEBUG";
    private final long ONE_MEGABYTE = 1024 * 1024;

    int count = 0;

    @Bean
    ConfigApp configApp;
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
        configFirebase();
        loadToolbar();
        //execBackgroundTasks();
        getPlantList();

 /*       while (bmpList.size() == 0){
        }*/

        //showImages();
        //teste();

        mProgressDialog.dismiss();
    }

    private void startDialog() {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle(getString(R.string.wait));
        mProgressDialog.setMessage(getString(R.string.getting_data));
        mProgressDialog.show();
    }

    private void configFirebase() {
        setFirebaseReferences();
        setFirebaseNodes();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
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
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
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

    private void loadToolbar() {
        setSupportActionBar(toolbar);
        tvToolbarTitle.setText(getString(R.string.list_of_plants));
    }

    @Background(serial = "test")
    public void execBackgroundTasks() {
        getPlantList();
        //getImagesFromFirebase();
        //getImageFromDevice();
        //saveImagesToDevice();
        //getImageFromDevice();
        //showImages();
    }

    @Background(serial = "test")
    public void getPlantList() {
        mPlantList = new ArrayList<>();
        mDatabaseRef.child(mNodeDatabase).child(mNodeLanguage).child(mNodePlantList)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            PlantModel plant = data.getValue(PlantModel.class);
                            mPlantList.add(plant);
                        }
                        getImagesFromFirebase();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //TODO tratar erros
                    }
                });
    }

    @Background(serial = "test")
    public void getImagesFromFirebase() {
        StorageReference imageRef = mStorageRef.child(getString(R.string.folder_images));


        for (final PlantModel plant : mPlantList) {
            imageRef = imageRef.child(plant.getPlantImage());
            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    //saveImageToDevice(bytes, plant);
                    hashMap.put(bytes, plant);
                    count += 1;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //TODO Handle any errors
                    int i = 0;
                    count += 1;
                }
            });
        }

        while (count != mPlantList.size()) {
            continue;
        }

        saveImagesToDevice();
        //getImageFromDevice();

    }

    @Background(serial = "test")
    public void saveImagesToDevice() {
        for (HashMap.Entry<byte[], PlantModel> map : hashMap.entrySet()) {
            saveImageToDevice(map.getKey(), map.getValue());
        }

        getImageFromDevice();
    }

    @Background(serial = "test")
    public void saveImageToDevice(byte[] bytes, PlantModel plant) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        new ImageHelper(getApplicationContext()).
                setFileName(plant.getPlantImage()).
                setDirectoryName(getString(R.string.folder_images)).
                save(bitmap);
/*        try {
            File newfile = savebitmap(bitmap, plant.getPlantImage());
        } catch (Exception e) {
        }*/

    }

    @Background(serial = "test")
    public void getImageFromDevice() {

        int index = 0;

        for (PlantModel plant : mPlantList) {
            Bitmap bitmap = new ImageHelper(getApplicationContext()).
                    setFileName(plant.getPlantImage()).
                    setDirectoryName(getString(R.string.folder_images)).
                    load();


            bmpList.add(index, bitmap);

            //if (index == 0) {
            //ivTeste.setImageResource(R.drawable.ic_account_circle_white_48dp);
/*                ivTeste.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(),
                        bitmap.getHeight(), false));*/
            //} else {
            //ivTeste2.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(),
            //bitmap.getHeight(), false));
            //}

            index += 1;


        }

        int i = 0;
        showImages();
    }

    @UiThread
    public void showImages() {

        int i = 0;
        for (Bitmap b : bmpList) {
            if (b == null) {
                continue;
            }

            if (i == 0) {
                ivTeste.setImageBitmap(Bitmap.createScaledBitmap(b, b.getWidth(),
                        b.getHeight(), false));
            } else {
                ivTeste2.setImageBitmap(Bitmap.createScaledBitmap(b, b.getWidth(),
                        b.getHeight(), false));
            }

            i += 1;
        }

    }


    private void teste() {
        StorageReference imagesRef = mStorageRef.child("images");
        imagesRef = imagesRef.child("onion.png");

        final long ONE_MEGABYTE = 1024 * 1024;
        imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                int i = 0;

                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ImageView image = (ImageView) findViewById(R.id.ivIcon);

                image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),
                        image.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                int i = 0;
            }
        });
    }

/*    @Click
    void ivProfile() {
        LoginActivity_.intent(getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }*/

    public static File savebitmap(Bitmap bmp, String name) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + name);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
