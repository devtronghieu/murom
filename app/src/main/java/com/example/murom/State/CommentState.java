package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CommentState {
    public HashMap<String, ArrayList<Schema.Comment>> commentsMap = new HashMap<>();
    private final BehaviorSubject<HashMap<String, ArrayList<Schema.Comment>>> observableCommentsMap = BehaviorSubject.createDefault(commentsMap);
    public void updateObservableCommentsMap(HashMap<String, ArrayList<Schema.Comment>> comments) {
        this.commentsMap = comments;
        observableCommentsMap.onNext(comments);
    }
    public Observable<HashMap<String, ArrayList<Schema.Comment>>> getObservableCommentsMap() {
        return observableCommentsMap;
    }

    // Singleton
    private static CommentState instance = null;

    private CommentState() {}

    public static CommentState getInstance() {
        if (instance == null) {
            instance = new CommentState();
        }
        return instance;
    }
}
