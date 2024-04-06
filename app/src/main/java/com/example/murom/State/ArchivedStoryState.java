package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ArchivedStoryState {
    public HashMap<String, ArrayList<Schema.Story>> archivedStoriesMap = new HashMap<>();
    private final BehaviorSubject<HashMap<String, ArrayList<Schema.Story>>> observableArchivedStoriesMap = BehaviorSubject.createDefault(archivedStoriesMap);

    public void updateObservableArchivedStoriesMap(HashMap<String, ArrayList<Schema.Story>> archivedStoriesMap) {
        this.archivedStoriesMap = archivedStoriesMap;
        observableArchivedStoriesMap.onNext(archivedStoriesMap);
    }
    public Observable<HashMap<String, ArrayList<Schema.Story>>> getObservableArchivedStoriesMap() {
        return observableArchivedStoriesMap;
    }

    // Story Owner
    private final BehaviorSubject<Schema.User> observableStoryOwner = BehaviorSubject.createDefault(
            new Schema.User("", "", "", "", "", "", new HashMap<>())
    );
    public void updateObservableStoryOwner(String uid) {
        // TODO: fetch the corresponding story owner profile
        observableStoryOwner.onNext(ProfileState.getInstance().profile);
    }
    public Observable<Schema.User> getObservableStoryOwner() {
        return observableStoryOwner;
    }


    // Singleton
    private static ArchivedStoryState instance = null;

    private  ArchivedStoryState() {}

    public static ArchivedStoryState getInstance() {
        if (instance == null) {
            instance = new ArchivedStoryState();
        }
        return instance;
    }
}
