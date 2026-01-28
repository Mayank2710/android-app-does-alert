package com.example.medicinereminder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddMedicineActivity extends AppCompatActivity {

    private EditText etMedName, etStartDate, etEndDate;
    private EditText etMorningTime, etNoonTime, etNightTime;
    private CheckBox cbMorning, cbNoon, cbNight, cbBeforeFood, cbAfterFood;
    private Spinner spinnerDose;

    // Store selected times
    private int mHour = -1, mMin = -1;
    private int nHour = -1, nMin = -1;
    private int eHour = -1, eMin = -1;

    private final String databaseUrl = "https://medicinereminder123-default-rtdb.firebaseio.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        // UI Binding
        etMedName = findViewById(R.id.etMedName);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etMorningTime = findViewById(R.id.etMorningTime);
        etNoonTime = findViewById(R.id.etNoonTime);
        etNightTime = findViewById(R.id.etNightTime);
        cbMorning = findViewById(R.id.cbMorning);
        cbNoon = findViewById(R.id.cbNoon);
        cbNight = findViewById(R.id.cbNight);
        cbBeforeFood = findViewById(R.id.cbBeforeFood);
        cbAfterFood = findViewById(R.id.cbAfterFood);
        spinnerDose = findViewById(R.id.spinnerDose);
        Button btnSave = findViewById(R.id.btnSave);

        // Spinner Setup
        String[] doseOptions = {"1 Tablet", "2 Tablets", "0.5 Tablet", "1 Spoon"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doseOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDose.setAdapter(adapter);

        // Date Pickers
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        // Time Pickers
        etMorningTime.setOnClickListener(v -> showTimePicker(time -> {
            mHour = time[0]; mMin = time[1];
            etMorningTime.setText(String.format("%02d:%02d", mHour, mMin));
            cbMorning.setChecked(true);
        }));

        etNoonTime.setOnClickListener(v -> showTimePicker(time -> {
            nHour = time[0]; nMin = time[1];
            etNoonTime.setText(String.format("%02d:%02d", nHour, nMin));
            cbNoon.setChecked(true);
        }));

        etNightTime.setOnClickListener(v -> showTimePicker(time -> {
            eHour = time[0]; eMin = time[1];
            etNightTime.setText(String.format("%02d:%02d", eHour, eMin));
            cbNight.setChecked(true);
        }));

        btnSave.setOnClickListener(v -> saveMedicineData());
    }

    private void showTimePicker(TimeSelectListener listener) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, min) ->
                listener.onTimeSelected(new int[]{hour, min}),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    interface TimeSelectListener { void onTimeSelected(int[] time); }

    private void showDatePicker(EditText editText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) ->
                editText.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveMedicineData() {
        String name = etMedName.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getUid();

        // Handle Guest Mode path
        String pathId = (userId != null) ? userId : "guest_user";

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter medicine name", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference db = FirebaseDatabase.getInstance(databaseUrl).getReference();
        String medId = db.child("medicines").child(pathId).push().getKey();

        Map<String, Object> medData = new HashMap<>();
        medData.put("name", name);
        medData.put("dosage", spinnerDose.getSelectedItem().toString());
        medData.put("status", "active");
        medData.put("startDate", etStartDate.getText().toString());
        medData.put("endDate", etEndDate.getText().toString());

        if (medId != null) {
            db.child("medicines").child(pathId).child(medId).setValue(medData).addOnSuccessListener(v -> {
                int baseId = Math.abs(medId.hashCode());

                // Trigger First Alarms (Receiver will handle the next 2 repeats)
                if (cbMorning.isChecked() && mHour != -1) scheduleTripleAlarm(name, mHour, mMin, baseId + 100);
                if (cbNoon.isChecked() && nHour != -1) scheduleTripleAlarm(name, nHour, nMin, baseId + 200);
                if (cbNight.isChecked() && eHour != -1) scheduleTripleAlarm(name, eHour, eMin, baseId + 300);

                Toast.makeText(this, "Reminders Set (3x alerts per dose)", Toast.LENGTH_LONG).show();
                finish();
            });
        }
    }

    private void scheduleTripleAlarm(String name, int hr, int min, int id) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ReminderReceiver.class);

        // Pass necessary data for repeats
        i.putExtra("medName", name);
        i.putExtra("alarmId", id);
        i.putExtra("repeatCount", 1); // Starting the first of three

        PendingIntent pi = PendingIntent.getBroadcast(this, id, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hr);
        c.set(Calendar.MINUTE, min);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        // If selected time passed today, move to tomorrow
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        }
    }
}