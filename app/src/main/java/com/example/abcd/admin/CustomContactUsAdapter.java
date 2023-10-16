package com.example.abcd.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.R;
import java.util.HashMap;
import java.util.List;

public class CustomContactUsAdapter extends RecyclerView.Adapter<CustomContactUsAdapter.ViewHolder> {

    private final List<HashMap<String, String>> contactUsList;
    private OnDeleteClickListener deleteClickListener;

    public CustomContactUsAdapter(List<HashMap<String, String>> contactUsList) {
        this.contactUsList = contactUsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_admin_contact_us, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> contactUsItem = contactUsList.get(position);

        String username = contactUsItem.get("username");
        String title = contactUsItem.get("title");
        String description = contactUsItem.get("description");

        holder.usernameTv.setText(username);
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactUsList.size();
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public void setDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTv;
        TextView titleTv;
        TextView descriptionTv;
        Button deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTv = itemView.findViewById(R.id.recUsernameTv);
            titleTv = itemView.findViewById(R.id.recTitleTv);
            descriptionTv = itemView.findViewById(R.id.recDescriptionTv);
            deleteBtn = itemView.findViewById(R.id.recDeleteBtn);
        }
    }
}
