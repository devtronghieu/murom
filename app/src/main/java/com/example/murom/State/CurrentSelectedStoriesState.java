package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CurrentSelectedStoriesState {
    public ArrayList<Schema.Story> stories = new ArrayList<>();
    private final BehaviorSubject<ArrayList<Schema.Story>> observableStoriesMap = BehaviorSubject.createDefault(stories);
    public void updateObservableStoriesMap(ArrayList<Schema.Story> stories) {
        this.stories = stories;
        observableStoriesMap.onNext(stories);
    }
    public Observable<ArrayList<Schema.Story>> getObservableStoriesMap() {
        return observableStoriesMap;
    }

    // Singleton
    private static CurrentSelectedStoriesState instance = null;

    private CurrentSelectedStoriesState() {}

    public static CurrentSelectedStoriesState getInstance() {
        if (instance == null) {
            instance = new CurrentSelectedStoriesState();
        }
        return instance;
    }
}
