package com.example.murom.State;

import android.util.Log;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class StoryState {
    public ArrayList<Schema.Story> stories = new ArrayList<>();
    private final BehaviorSubject<ArrayList<Schema.Story>> observableStoriesMap = BehaviorSubject.createDefault(stories);
    public void updateObservableStoriesMap(ArrayList<Schema.Story> stories) {
        this.stories = stories;
        observableStoriesMap.onNext(stories);

        observableStoriesMap.getValue().forEach(story -> {
            Log.d("--> update", "updateObservableStoriesMap: " + story.id);
        });
    }
    public Observable<ArrayList<Schema.Story>> getObservableStoriesMap() {
        return observableStoriesMap;
    }

    // Singleton
    private static StoryState instance = null;

    private StoryState() {}

    public static StoryState getInstance() {
        if (instance == null) {
            instance = new StoryState();
        }
        return instance;
    }
}
