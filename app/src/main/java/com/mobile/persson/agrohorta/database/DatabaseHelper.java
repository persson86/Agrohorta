package com.mobile.persson.agrohorta.database;

import android.content.Context;

import com.mobile.persson.agrohorta.BuildConfig;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import io.realm.Realm;
import io.realm.RealmConfiguration;

@EBean
public class DatabaseHelper {

    @RootContext
    Context context;

    private static final String DATABASE_NAME = "Agrohorta.db";
    private static RealmConfiguration configuration;

    @AfterInject
    public void afterInject() {
        configuration = getConfiguration(context);
    }

    public Realm getRealm() {
        return Realm.getInstance(getConfiguration(context));
    }

    private RealmConfiguration getConfiguration(Context context) {
        if (configuration == null) {
            configuration = new RealmConfiguration.Builder(context)
                    .name(DATABASE_NAME)
                    .schemaVersion(BuildConfig.VERSION_CODE)
                    .deleteRealmIfMigrationNeeded()
                    .build();
        }
        return configuration;
    }
}