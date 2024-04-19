package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.HashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class OtherProfileState {
    public Schema.User profile = new Schema.User("", "", "", "", "", "", new HashMap<>());
    private final BehaviorSubject<Schema.User> observableProfile = BehaviorSubject.createDefault(profile);
    public void updateObservableProfile(Schema.User profile) {
        this.profile = profile;
        observableProfile.onNext(profile);
    }
    public Observable<Schema.User> getObservableProfile() { return observableProfile; }

    private static OtherProfileState instance = null;

    private OtherProfileState() {}

    public static OtherProfileState getInstance() {
        if (instance == null) {
            instance = new OtherProfileState();
        }
        return instance;
    }
}
