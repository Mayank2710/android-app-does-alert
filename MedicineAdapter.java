package com.example.medicinereminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final List<Medicine> medicineList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onDoneClick(int position);
    }

    public MedicineAdapter(List<Medicine> medicineList, OnItemClickListener listener) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);

        // Set Medicine Name
        holder.tvName.setText(medicine.getName());

        // FIX 1: Show the Dosage Quantity (e.g., "1 Tablet (Full)")
        if (medicine.getDosage() != null) {
            holder.tvDosage.setText("Dosage: " + medicine.getDosage());
        }

        // FIX 2: Show the Course Dates instead of "Details"
        String courseInfo = "From: " + medicine.getStartDate() + " To: " + medicine.getEndDate();
        holder.tvDetails.setText(courseInfo);

        // Hide the "Done" button if the medicine is already in History
        if ("history".equals(medicine.getStatus())) {
            holder.btnDone.setVisibility(View.GONE);
        } else {
            holder.btnDone.setVisibility(View.VISIBLE);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(position));
        holder.btnDone.setOnClickListener(v -> listener.onDoneClick(position));
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        // Added tvDosage to match your updated item_medicine.xml
        TextView tvName, tvDetails, tvDosage;
        ImageView btnDelete, btnDone;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvMedicineDosage); // New ID!
            tvDetails = itemView.findViewById(R.id.tvMedicineDetails);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDone = itemView.findViewById(R.id.btnDone);
        }
    }
}