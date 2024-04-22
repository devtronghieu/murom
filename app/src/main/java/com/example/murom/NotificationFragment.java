package com.example.murom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        Log.d("-->", "create dialog");
        RecyclerView requestRecyclerView = rootView.findViewById(R.id.notification_request_recycler);
        RecyclerView followRecyclerView = rootView.findViewById(R.id.notification_follow_recycler);

        requestRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        followRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<NotificationRequestAdapter.NotificationRequestModel> requests = new ArrayList<>();
        requests.add(new NotificationRequestAdapter.NotificationRequestModel("https://picsum.photos/200", "sdaada", "1 day"));
        requests.add(new NotificationRequestAdapter.NotificationRequestModel("https://picsum.photos/200", "sdaeedada", "1 hour"));

        ArrayList<NotificationFollowAdapter.NotificationFollowModel> follows = new ArrayList<>();
        follows.add(new NotificationFollowAdapter.NotificationFollowModel("https://picsum.photos/200", "ersdfd", "1 day"));
        follows.add(new NotificationFollowAdapter.NotificationFollowModel("https://picsum.photos/200", "lftrv", "1 hour"));
        Log.d("-->", "create temp data");

        requestRecyclerView.setAdapter(new NotificationRequestAdapter(requests));
        followRecyclerView.setAdapter(new NotificationFollowAdapter(follows));
        Log.d("-->", "set adapter");
        return rootView;
    }
}


