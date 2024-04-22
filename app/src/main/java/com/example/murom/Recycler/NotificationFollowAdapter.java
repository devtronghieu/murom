package com.example.murom.Recycler;

import android.content.Context;
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

public class NotificationFollowAdapter extends RecyclerView.Adapter<NotificationFollowAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<NotificationFollowAdapter.NotificationFollowModel> localDataSet;
    public static class NotificationFollowModel {
        private final String avatarUrl;
        private final String username;
        private final String timestamp;

        public NotificationFollowModel(
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
            avatar = view.findViewById(R.id.follow_avatar);
            username = view.findViewById(R.id.follow_username);
            timestamp = view.findViewById(R.id.follow_timestamp);
        }
    }
    public NotificationFollowAdapter(ArrayList<NotificationFollowAdapter.NotificationFollowModel> dataSet) {
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public NotificationFollowAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_notification_follow, viewGroup, false);

        return new NotificationFollowAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        NotificationFollowAdapter.NotificationFollowModel data = localDataSet.get(position);
        viewHolder.username.setText(data.username);
        viewHolder.timestamp.setText(data.timestamp);
        Glide.with(this.context)
                .load(data.avatarUrl)
                .into(viewHolder.avatar);

    }
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
