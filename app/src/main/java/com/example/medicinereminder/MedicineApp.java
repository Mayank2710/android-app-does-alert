package com.example.medicinereminder;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MedicineApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /* This is the "Secret Sauce": It allows the app to work offline
           and caches data so it doesn't "disappear" on restart.
        */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}