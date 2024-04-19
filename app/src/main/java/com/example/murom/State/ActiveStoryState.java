package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ActiveStoryState {
    public HashMap<String, ArrayList<Schema.Story>> activeStoriesMap = new HashMap<>();
    private final BehaviorSubject<HashMap<String, ArrayList<Schema.Story>>> observableActiveStoriesMap = BehaviorSubject.createDefault(activeStoriesMap);
    public void updateObservableActiveStoriesMap(HashMap<String, ArrayList<Schema.Story>> storiesMap) {
        this.activeStoriesMap = storiesMap;
        observableActiveStoriesMap.onNext(storiesMap);
    }
    public Observable<HashMap<String, ArrayList<Schema.Story>>> getObservableActiveStoriesMap() {
        return observableActiveStoriesMap;
    }

    // Story Owner
    private final BehaviorSubject<Schema.User> observableActiveStoryOwner = BehaviorSubject.createDefault(
            new Schema.User("", "", "", "", "", "", new HashMap<>())
    );
    public void updateObservableActiveStoryOwner(String uid) {
        ProfileState profileState = ProfileState.getInstance();
        if (Objects.equals(profileState.profile.id, uid)) {
            observableActiveStoryOwner.onNext(ProfileState.getInstance().profile);
        } else {
            Schema.User followerProfile = ProfileState.getInstance().followerProfileMap.get(uid);
            if (followerProfile != null) {
                observableActiveStoryOwner.onNext(followerProfile);
            }
        }
    }
    public Observable<Schema.User> getObservableActiveStoryOwner() {
        return observableActiveStoryOwner;
    }


    // Singleton
    private static ActiveStoryState instance = null;

    private ActiveStoryState() {}

    public static ActiveStoryState getInstance() {
        if (instance == null) {
            instance = new ActiveStoryState();
        }
        return instance;
    }
}
