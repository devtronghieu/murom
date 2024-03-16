package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class StoryState {
    // Stories Map
    public HashMap<String, ArrayList<Schema.Story>> storiesMap = new HashMap<>();
    private final BehaviorSubject<HashMap<String, ArrayList<Schema.Story>>> observableStoriesMap = BehaviorSubject.createDefault(storiesMap);
    public void updateObservableStoriesMap(HashMap<String, ArrayList<Schema.Story>> storiesMap) {
        this.storiesMap = storiesMap;
        observableStoriesMap.onNext(storiesMap);
    }
    public Observable<HashMap<String, ArrayList<Schema.Story>>> getObservableStoriesMap() {
        return observableStoriesMap;
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
    private static StoryState instance = null;

    private  StoryState() {}

    public static StoryState getInstance() {
        if (instance == null) {
            instance = new StoryState();
        }
        return instance;
    }
}
