package com.example.murom.State;

import com.example.murom.Firebase.Schema;

public class AppState {
    private static AppState instance = null;

    public Schema.User profile;

    private  AppState() {}

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }
}
