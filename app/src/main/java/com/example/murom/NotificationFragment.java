package com.example.murom;

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
import com.example.murom.Recycler.NotificationFollowAdapter;
import com.example.murom.Recycler.NotificationRequestAdapter;

import java.util.ArrayList;

public class NotificationFragment extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String uid = Auth.getUser().getUid();
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        TextView requestsText = rootView.findViewById(R.id.notification_request_text);
        RecyclerView requestRecyclerView = rootView.findViewById(R.id.notification_request_recycler);
        RecyclerView followRecyclerView = rootView.findViewById(R.id.notification_follow_recycler);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        followRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Database.getRequestNotification(uid, new Database.RequestsCallback() {
            @Override
            public void onRequestsLoaded(ArrayList<NotificationRequestAdapter.NotificationRequestModel> requests) {
                Log.d("-->", "request size" + requests.size());
                if (requests.size() != 0) {
                    Log.d("-->", "request size" + requests.size());
                    requestRecyclerView.setAdapter(new NotificationRequestAdapter(requests));
                    requestsText.setVisibility(View.VISIBLE);
                }
                else {
                    requestsText.setVisibility(View.GONE);
                    requestRecyclerView.setVisibility(View.GONE);
                }

            }

            @Override
            public void onRequestsLoadedFailure(String errorMessage) {
                Log.d("-->", errorMessage);
                requestsText.setVisibility(View.GONE);
                requestRecyclerView.setVisibility(View.GONE);
            }
        });
        Database.getFollowerNotification(uid, new Database.FollowsCallback() {
            @Override
            public void onFollowsLoaded(ArrayList<NotificationFollowAdapter.NotificationFollowModel> follows) {
                Log.d("-->", "follow size: " + follows.size());
                followRecyclerView.setAdapter(new NotificationFollowAdapter(follows));
            }

            @Override
            public void onFollowsLoadedFailure(String errorMessage) {

            }
        });

        return rootView;
    }
}


