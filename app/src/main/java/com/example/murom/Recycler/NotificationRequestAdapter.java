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
import com.example.murom.R;

import java.util.ArrayList;

public class NotificationRequestAdapter extends RecyclerView.Adapter<NotificationRequestAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<NotificationRequestAdapter.NotificationRequestModel> localDataSet;
    public static class NotificationRequestModel {
        private final String avatarUrl;
        private final String username;
        private final String timestamp;

        public NotificationRequestModel(
                String avatarUrl,
                String username,
                String timestamp
        ) {
            this.avatarUrl = avatarUrl;
            this.username = username;
            this.timestamp = timestamp;
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView username, timestamp;

        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.request_avatar);
            username = view.findViewById(R.id.request_username);
            timestamp = view.findViewById(R.id.request_timestamp);
        }
    }
    public NotificationRequestAdapter(ArrayList<NotificationRequestAdapter.NotificationRequestModel> dataSet) {
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public NotificationRequestAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_notification_request, viewGroup, false);

        return new NotificationRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        NotificationRequestAdapter.NotificationRequestModel data = localDataSet.get(position);
        viewHolder.username.setText(data.username);
        viewHolder.timestamp.setText(data.timestamp);
        Glide.with(this.context)
                .load(data.avatarUrl)
                .into(viewHolder.avatar);
        Log.d("-->", "test" + data.username);

    }
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
