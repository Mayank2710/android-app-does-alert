package com.example.medicinereminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // For Delete Icon
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<Member> memberList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public MemberAdapter(List<Member> memberList, OnItemClickListener listener) {
        this.memberList = memberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Connects to your item_member.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = memberList.get(position);
        holder.tvName.setText(member.getName());
        holder.tvRelation.setText(member.getRelation());

        // Handle Delete Click
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRelation;
        ImageView btnDelete;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            // MAKE SURE THESE IDs MATCH YOUR XML
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvRelation = itemView.findViewById(R.id.tvMemberRelation);
            btnDelete = itemView.findViewById(R.id.btnDeleteMember);
        }
    }
}