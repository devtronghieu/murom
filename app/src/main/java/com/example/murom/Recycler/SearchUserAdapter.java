package com.example.murom.Recycler;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Schema;
import com.example.murom.R;

import java.util.ArrayList;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    private Context context;

    private final ArrayList<Schema.SearchUser> localDataSet;
    private OnUserItemClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView username;

        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.component_user_display_avatar);
            username = view.findViewById(R.id.component_user_display_username);
        }

        public void setOnClickListener(String uid) {
        }
    }

    public SearchUserAdapter(ArrayList<Schema.SearchUser> dataSet, OnUserItemClickListener listener) {
        localDataSet = dataSet;
        this.listener = listener;
    }

    public interface OnUserItemClickListener {
        void onUserItemClick(String userId);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_user_display, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Schema.SearchUser data = localDataSet.get(position);

        viewHolder.username.setText(data.username);
        Glide.with(this.context).load(data.avatarUrl).into(viewHolder.avatar);
        viewHolder.itemView.setOnClickListener(v -> {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onUserItemClick(data.userId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
