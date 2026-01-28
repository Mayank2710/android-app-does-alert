package com.example.medicinereminder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MembersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private List<Member> memberList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private final String databaseUrl = "https://medicinereminder123-default-rtdb.firebaseio.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(databaseUrl).getReference();

        // UI Initialization
        recyclerView = findViewById(R.id.recyclerMembers);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddMember);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberList = new ArrayList<>();

        adapter = new MemberAdapter(memberList, position -> deleteMember(memberList.get(position).getId()));
        recyclerView.setAdapter(adapter);

        listenToMembers();
        fabAdd.setOnClickListener(v -> showAddMemberDialog());
    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_member, null);

        final EditText etName = view.findViewById(R.id.etMemberName);
        final EditText etRelation = view.findViewById(R.id.etMemberRelation);
        Button btnAdd = view.findViewById(R.id.btnAdd);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String relation = etRelation.getText().toString().trim();

            if (!name.isEmpty()) {
                saveMemberToRealtime(name, relation);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void saveMemberToRealtime(String name, String relation) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        DatabaseReference userMembersRef = mDatabase.child("members").child(user.getUid());
        String memberId = userMembersRef.push().getKey();

        Map<String, Object> memberMap = new HashMap<>();
        memberMap.put("name", name);
        memberMap.put("relation", relation);

        if (memberId != null) {
            userMembersRef.child(memberId).setValue(memberMap)
                    .addOnSuccessListener(v -> Toast.makeText(this, "Member added to family list", Toast.LENGTH_SHORT).show());
        }
    }

    private void listenToMembers() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        mDatabase.child("members").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Member m = new Member(data.child("name").getValue(String.class), data.child("relation").getValue(String.class));
                    m.setId(data.getKey());
                    memberList.add(m);
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void deleteMember(String id) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && id != null) {
            mDatabase.child("members").child(user.getUid()).child(id).removeValue();
        }
    }
}