package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ProfileState {
    // User Profile
    public Schema.User profile = new Schema.User("", "", "", "", "", "", "",new HashMap<>());
    private final BehaviorSubject<Schema.User> observableProfile = BehaviorSubject.createDefault(profile);
    public void updateObservableProfile(Schema.User profile) {
        this.profile = profile;
        observableProfile.onNext(profile);
    }
    public Observable<Schema.User> getObservableProfile() { return observableProfile; }

    // Followers' ids
    public ArrayList<String> followerIDs = new ArrayList<>();
    public ArrayList<String> socialIDs = new ArrayList<>();
    private final BehaviorSubject<ArrayList<String>> observableFollowerIDs =
            BehaviorSubject.createDefault(followerIDs);
    public void updateObservableFollowerIDs(ArrayList<String> followerIDs) {
        this.followerIDs = followerIDs;
        this.socialIDs = new ArrayList<>(followerIDs);
        this.socialIDs.add(0, profile.id);
        observableFollowerIDs.onNext(followerIDs);
    }
    public Observable<ArrayList<String>> getObservableFollowerIDs() { return observableFollowerIDs; }


    // Followers' profiles
    public HashMap<String, Schema.User> followerProfileMap = new HashMap<>();
    private final BehaviorSubject<HashMap<String, Schema.User>> observableFollowerProfileMap =
            BehaviorSubject.createDefault(followerProfileMap);
    public void updateObservableFollowerProfileMap(HashMap<String, Schema.User> followerProfileMap) {
        this.followerProfileMap = followerProfileMap;
        observableFollowerProfileMap.onNext(followerProfileMap);
    }
    public Observable<HashMap<String, Schema.User>> getObservableFollowerProfileMap() { return observableFollowerProfileMap; }

    // Singleton
    private static ProfileState instance = null;

    private ProfileState() {}

    public static ProfileState getInstance() {
        if (instance == null) {
            instance = new ProfileState();
        }
        return instance;
    }
}
