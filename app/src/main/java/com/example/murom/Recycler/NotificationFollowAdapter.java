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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.murom.Firebase.Schema;
import com.example.murom.R;

import java.util.ArrayList;

public class NotificationFollowAdapter extends RecyclerView.Adapter<NotificationFollowAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<Schema.Notification> notifications;

    public NotificationFollowAdapter(Context context, ArrayList<Schema.Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
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
        Schema.Notification data = notifications.get(position);
        viewHolder.username.setText(data.username);
        viewHolder.timestamp.setText(data.timestamp);
        Glide.with(this.context)
                .load(data.avatarUrl).centerCrop()
                .skipMemoryCache(true)
                .into(viewHolder.avatar);

    }
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(ArrayList<Schema.Notification> updatedNotifications) {
        notifications.clear();
        notifications.addAll(updatedNotifications);
        notifyDataSetChanged();
    }
}
