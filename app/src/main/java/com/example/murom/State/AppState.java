package com.example.murom.State;

import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.HashMap;

public class AppState {
    private static AppState instance = null;

    public Schema.User profile;
    public HashMap<String, ArrayList<Schema.Story>> storiesMap = new HashMap<>();

    private  AppState() {}

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }
}
