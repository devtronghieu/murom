package com.example.murom;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Recycler.NotificationFollowAdapter;
import com.example.murom.Recycler.NotificationRequestAdapter;
import com.example.murom.State.NotificationState;

import java.util.ArrayList;
import java.util.Objects;

public class NotificationFragment extends DialogFragment {

    private NotificationFollowAdapter notificationFollowAdapter;
    private NotificationState notificationState;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("CheckResult")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String uid = Auth.getUser().getUid();
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        TextView requestsText = rootView.findViewById(R.id.notification_request_text);
        Database.getUser(uid, new Database.GetUserCallback() {
            @Override
            public void onGetUserSuccess(Schema.User user) {
                if (Objects.equals(user.status, "Private")) {
                    requestsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onGetUserFailure() {

            }
        });
        RecyclerView requestRecyclerView = rootView.findViewById(R.id.notification_request_recycler);
        RecyclerView followRecyclerView = rootView.findViewById(R.id.notification_follow_recycler);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        followRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationFollowAdapter = new NotificationFollowAdapter(requireContext(), new ArrayList<>());
        followRecyclerView.setAdapter(notificationFollowAdapter);

        notificationState = NotificationState.getInstance();
        notificationState.getObservableFollowNotifications().subscribe(this::updateFollowNotifications);
        notificationState.fetchFollowNotifications(uid);

        return rootView;
    }

    private void updateFollowNotifications(ArrayList<Schema.Notification> notifications) {
        notificationFollowAdapter.updateNotifications(notifications);
    }
}


