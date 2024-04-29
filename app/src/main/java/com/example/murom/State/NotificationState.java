package com.example.murom.State;

import android.util.Log;

import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class NotificationState {
    public ArrayList<Schema.Notification> followNotifications = new ArrayList<>();
    private final BehaviorSubject<ArrayList<Schema.Notification>> observableFollowNotifications = BehaviorSubject.createDefault(followNotifications);

    public void updateObservableFollowNotifications(ArrayList<Schema.Notification> followNotifications) {
        this.followNotifications = followNotifications;
        observableFollowNotifications.onNext(followNotifications);
    }

    public Observable<ArrayList<Schema.Notification>> getObservableFollowNotifications() {
        return observableFollowNotifications;
    }

    // Fetch notifications from database
    public void fetchFollowNotifications(String uid) {
        Database.getFollowerNotification(uid, new Database.FollowsCallback() {
            @Override
            public void onFollowsLoaded(ArrayList<Schema.Notification> followNotifications) {
                updateObservableFollowNotifications(followNotifications);
            }

            @Override
            public void onFollowsLoadedFailure(String errorMessage) {
                Log.d("-->", "Failed to load notifications: " + errorMessage);
            }
        });
    }

    // Singleton
    private static NotificationState instance = null;

    private NotificationState() {}

    public static NotificationState getInstance() {
        if (instance == null) {
            instance = new NotificationState();
        }
        return instance;
    }
}
