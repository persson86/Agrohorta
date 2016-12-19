package com.mobile.persson.agrohorta.firebase;

import android.app.Activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mobile.persson.agrohorta.R;
import com.mobile.persson.agrohorta.models.PlantModel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by persson on 13/12/16.
 */

@EBean
public class FirebaseHelper extends Activity {
    private DatabaseReference databaseReference;

    private List<PlantModel> catalogoList;
    private boolean inProcess = false;

    @Background
    public void getCatalogoFromFirebase() {
        inProcess = true;
        catalogoList = new ArrayList<>();

        String node = getString(R.string.firebase_node_catalogo);
        databaseReference.child(node).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    PlantModel p = new PlantModel();
                    p.setName(data.getKey());
                    catalogoList.add(p);
                }

                inProcess = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public List<PlantModel> getCatalogo() {
        getCatalogoFromFirebase();

        while (inProcess)
            continue;

        return catalogoList;
    }
}
