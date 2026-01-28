package com.example.medicinereminder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicineAdapter adapter;
    private List<Medicine> medicineList;
    private TextView tvEmptyState;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ValueEventListener medListener;

    private String currentFilter = "active";
    private final String databaseUrl = "https://medicinereminder123-default-rtdb.firebaseio.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference();

        recyclerView = findViewById(R.id.recyclerViewMedicines);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicineList = new ArrayList<>();

        adapter = new MedicineAdapter(medicineList, new MedicineAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteFromRealtime(medicineList.get(position).getId());
            }

            @Override
            public void onDoneClick(int position) {
                moveToHistoryRealtime(medicineList.get(position).getId());
            }
        });

        recyclerView.setAdapter(adapter);

        // Guest Mode: Open Add screen without login
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddMedicineActivity.class));
        });

        // NAVIGATION FIX
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                currentFilter = "active";
                listenToRealtimeMedicines();
                return true;
            } else if (id == R.id.nav_history) {
                currentFilter = "history";
                listenToRealtimeMedicines();
                return true;
            } else if (id == R.id.nav_members) {
                startActivity(new Intent(MainActivity.this, MembersActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                // This WILL open the ProfileActivity now
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenToRealtimeMedicines();
    }

    private void listenToRealtimeMedicines() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            medicineList.clear();
            adapter.notifyDataSetChanged();
            checkEmptyState();
            return;
        }

        DatabaseReference userMedsRef = mDatabase.child("medicines").child(user.getUid());
        if (medListener != null) userMedsRef.removeEventListener(medListener);

        medListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicineList.clear();
                for (DataSnapshot medSnapshot : snapshot.getChildren()) {
                    String name = medSnapshot.child("name").getValue(String.class);
                    String dosage = medSnapshot.child("dosage").getValue(String.class);
                    String status = medSnapshot.child("status").getValue(String.class);
                    String startDate = medSnapshot.child("startDate").getValue(String.class);
                    String endDate = medSnapshot.child("endDate").getValue(String.class);

                    if (status == null) status = "active";

                    if (status.equals(currentFilter)) {
                        Medicine med = new Medicine(name, dosage, status, startDate, endDate);
                        med.setId(medSnapshot.getKey());
                        medicineList.add(med);
                    }
                }
                adapter.notifyDataSetChanged();
                checkEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB_ERROR", error.getMessage());
            }
        };
        userMedsRef.addValueEventListener(medListener);
    }

    private void moveToHistoryRealtime(String medId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || medId == null) return;
        mDatabase.child("medicines").child(user.getUid()).child(medId).child("status").setValue("history");
    }

    private void deleteFromRealtime(String medId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || medId == null) return;
        mDatabase.child("medicines").child(user.getUid()).child(medId).removeValue();
    }

    private void checkEmptyState() {
        if (medicineList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(currentFilter.equals("active") ? "No medicines added." : "History is empty.");
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}