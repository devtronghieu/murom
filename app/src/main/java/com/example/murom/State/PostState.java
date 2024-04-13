package com.example.murom.State;

import android.util.Log;

import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class PostState {
    public ArrayList<Schema.Post> myPosts = new ArrayList<>();
    private final BehaviorSubject<ArrayList<Schema.Post>> observableMyPosts = BehaviorSubject.createDefault(myPosts);
    public void updateObservableMyPosts(ArrayList<Schema.Post> myPosts) {
        this.myPosts = myPosts;
        observableMyPosts.onNext(myPosts);
    }
    public Observable<ArrayList<Schema.Post>> getObservableMyPosts() {
        return observableMyPosts;
    }

    // Social Posts = Mix of My Posts, My followers' posts, Social recommended Posts?
    public ArrayList<Schema.Post> socialPosts = new ArrayList<>();
    private final BehaviorSubject<ArrayList<Schema.Post>> observableSocialPosts = BehaviorSubject.createDefault(socialPosts);
    public void constructObservableSocialPosts(int offset, int limit) {
        ProfileState profileState = ProfileState.getInstance();
        ArrayList<String> socialIDs = new ArrayList<>(profileState.followerIDs);
        socialIDs.add(profileState.profile.id);

        Database.getPostsByUIDs(socialIDs, offset, limit, new Database.GetPostsByUIDsCallback() {
            @Override
            public void onGetPostsSuccess(ArrayList<Schema.Post> posts) {
                socialPosts = posts;
                observableSocialPosts.onNext(posts);
            }

            @Override
            public void onGetPostsFailure() {
                Log.d("-->", "failed to get social posts");
                observableMyPosts.onNext(socialPosts);
            }
        });
    }
    public Observable<ArrayList<Schema.Post>> getObservableSocialPosts() {
        return observableSocialPosts;
    }

    // Singleton
    private static PostState instance = null;

    private  PostState() {}

    public static PostState getInstance() {
        if (instance == null) {
            instance = new PostState();
        }

        return instance;
    }
}
