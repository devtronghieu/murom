package com.example.murom.State;

import com.example.murom.Firebase.Schema;
import com.example.murom.Recycler.PostAdapter;

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
    public ArrayList<PostAdapter.PostModel> socialPosts = new ArrayList<>();
    private final BehaviorSubject<ArrayList<PostAdapter.PostModel>> observableSocialPosts = BehaviorSubject.createDefault(socialPosts);
    public void constructObservableSocialPosts() {
        ArrayList<PostAdapter.PostModel> socialPosts = new ArrayList<>();
        Schema.User me = ProfileState.getInstance().profile;

        this.myPosts.forEach(post -> {
            ArrayList<String> images = new ArrayList<>();
            images.add(post.url);
            socialPosts.add(new PostAdapter.PostModel(
                    post.id,
                    me.profilePicture,
                    me.username,
                    images,
                    post.caption,
                    post.lovedByUIDs
            ));
        });

        this.socialPosts = socialPosts;
        observableSocialPosts.onNext(socialPosts);
    }
    public Observable<ArrayList<PostAdapter.PostModel>> getObservableSocialPosts() {
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
