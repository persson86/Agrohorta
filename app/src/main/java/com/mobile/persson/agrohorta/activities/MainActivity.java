package com.mobile.persson.agrohorta.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

//import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    private ProgressDialog mProgressDialog;
    private List<PlantModel> mPlantList;
    private List<Bitmap> mImageList = new ArrayList<>();
    private List<Bitmap> bmpList = new ArrayList<>();

    private String mNodeDatabase;
    private String mNodeLanguage;
    private String mNodePlantList;

    private boolean isOk = false;

    private final static String TAG = "LFSP_DEBUG";
    private final long ONE_MEGABYTE = 1024 * 1024;

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
    }

    private void setFirebaseNodes() {
        mNodeDatabase = getString(R.string.node_database);
        mNodePlantList = getString(R.string.node_plant_list);
        mNodeLanguage = getString(R.string.node_language) + configApp.getLanguageDevice();
    }

    private void setFirebaseReferences() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_storage_url));
    }

    private void loadToolbar() {
        setSupportActionBar(toolbar);
        tvToolbarTitle.setText(getString(R.string.list_of_plants));
    }

    @Background
    protected void getPlantList() {
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

    @Background
    public void getImagesFromFirebase() {
        StorageReference imageRef = mStorageRef.child(getString(R.string.folder_images));

        for (final PlantModel plant : mPlantList) {
            imageRef = imageRef.child(plant.getPlantImage());
            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    saveImageToDevice(bytes, plant);

/*
                    int i = 0;
                    mImageList.add(BitmapFactory.decodeResource(getResources(),
                            getResources().getIdentifier("itm" + i, "drawable", getPackageName())));
*/

/*                    ImageView image = (ImageView) findViewById(R.id.ivIcon);

                    image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),
                            image.getHeight(), false));*/
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //TODO Handle any errors
                    int i = 0;
                }
            });
        }

        getImageFromDevice();

    }

    private void saveImageToDevice(byte[] bytes, PlantModel plant) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        new ImageHelper(getApplicationContext()).
                setFileName(plant.getPlantImage()).
                setDirectoryName(getString(R.string.folder_images)).
                save(bitmap);
    }

    @Background
    public void getImageFromDevice() {

        int index = 0;

        for (PlantModel plant : mPlantList) {
            Bitmap bitmap = new ImageHelper(getApplicationContext()).
                    setFileName(plant.getPlantImage()).
                    setDirectoryName(getString(R.string.folder_images)).
                    load();


            bmpList.add(index, bitmap);

            if (index == 0) {
                ivTeste.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(),
                        bitmap.getHeight(), false));
            } else {
                ivTeste2.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(),
                        bitmap.getHeight(), false));
            }

            index += 1;


        }

        int i = 0;
    }

    @UiThread
    private void showImages() {
        int i = 0;
        for (Bitmap b : bmpList) {
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

    @Click
    void ivProfile() {
        LoginActivity_.intent(getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }
}
