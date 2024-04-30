package com.example.murom.Recycler;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.R;

import java.util.ArrayList;

public class NotificationRequestAdapter extends RecyclerView.Adapter<NotificationRequestAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<Schema.Notification> notifications;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        TextView notificationTextView;
        Button acceptButton;
        Button denyButton;

        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.request_avatar);
            notificationTextView = view.findViewById(R.id.request_textView);
            acceptButton = view.findViewById(R.id.request_accept_button);
            denyButton = view.findViewById(R.id.request_deny_button);

        }
    }
    public NotificationRequestAdapter(Context context, ArrayList<Schema.Notification> notifications){
        this.context = context;
        this.notifications = notifications;
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
        Schema.Notification data = notifications.get(position);
        formatNotificationText(data.username, data.timestamp, viewHolder.notificationTextView, this.context);
        Glide.with(this.context)
                .load(data.avatarUrl).centerCrop()
                .skipMemoryCache(true)
                .into(viewHolder.avatar);
        viewHolder.acceptButton.setOnClickListener(v -> {
            Log.d("-->", "Click accept");
            Database.acceptFollowRequest(data.userId);
            Database.deleteFollowRequest(data.userId);
            updateNotifications(notifications);
        });
        viewHolder.denyButton.setOnClickListener(v -> {
            Log.d("-->", "Click deny");
            Database.deleteFollowRequest(data.userId);
            updateNotifications(notifications);
        });
        Log.d("-->", "test" + data.username);

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
    public void formatNotificationText(String username, String timestamp, TextView textView, Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String action = "sends a request to follow you.";
        int start = 0;

        builder.append(username);
        builder.setSpan(new StyleSpan(Typeface.BOLD), start, start + username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        start += username.length();

        builder.append(" ");
        start++;

        builder.append(action);
        start += action.length();

        builder.append(" ");
        start++;

        builder.append(timestamp);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.gray_500)), start, start + timestamp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(builder);
    }
}
