package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class HighlightState {
    public ArrayList<Schema.HighlightStory> highlights = new ArrayList<>();
    private final BehaviorSubject<ArrayList<Schema.HighlightStory>> observableHighlights = BehaviorSubject.createDefault(highlights);

    public void updateObservableHighlights(ArrayList<Schema.HighlightStory> highlights) {
        this.highlights = highlights;
        observableHighlights.onNext(highlights);
    }
    public Observable<ArrayList<Schema.HighlightStory>> getObservableHighlights() {
        return observableHighlights;
    }

    // Singleton
    private static HighlightState instance = null;

    private  HighlightState() {}

    public static HighlightState getInstance() {
        if (instance == null) {
            instance = new HighlightState();
        }
        return instance;
    }
}
