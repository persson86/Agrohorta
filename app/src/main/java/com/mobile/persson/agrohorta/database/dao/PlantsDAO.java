package com.mobile.persson.agrohorta.database.dao;

import android.content.Context;

import com.mobile.persson.agrohorta.database.DatabaseHelper;
import com.mobile.persson.agrohorta.database.models.PlantModelRealm;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

@EBean(scope = EBean.Scope.Singleton)
public class PlantsDAO {
    @RootContext
    Context context;

    @Bean
    DatabaseHelper dbHelper;

    public void savePlants(List<PlantModelRealm> model) {
        Realm realm = dbHelper.getRealm();
        realm.beginTransaction();
        realm.where(PlantModelRealm.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.beginTransaction();
        realm.copyToRealm(model);
        realm.commitTransaction();
    }

    public List<PlantModelRealm> getPlants() {
        Realm realm = dbHelper.getRealm();
        RealmResults<PlantModelRealm> result = realm.where(PlantModelRealm.class).findAll();
        return result;
    }
}
