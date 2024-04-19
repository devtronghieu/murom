package com.example.murom.Firebase;

import com.google.firebase.Timestamp;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

public class Schema {
    public static class User {
        public String id;
        public String bio;
        public String email;
        public String passwordHash;
        public String profilePicture;
        public String username;
        public HashMap<String, String> viewedStories;

        public User(
                String id,
                String bio,
                String email,
                String passwordHash,
                String profilePicture,
                String username,
                HashMap<String, String> viewedStories
        ) {
            this.id = id;
            this.bio = bio;
            this.email = email;
            this.passwordHash = passwordHash;
            this.profilePicture = profilePicture;
            this.username = username;
            this.viewedStories = viewedStories;
        }
    }

    public static class Story {
        public String id;
        public Timestamp createdAt;
        public String uid;
        public String url;
        public String type;

        public Story(String id, Timestamp createdAt, String uid, String url, String type) {
            this.id = id;
            this.createdAt = createdAt;
            this.uid = uid;
            this.url = url;
            this.type = type;
        }
    }

    public static class Post {
        public String id;
        public String userId;
        public String url;
        public String type;
        public String caption;
        public ArrayList<String> lovedByUIDs;
        public Timestamp createdAt;
        public boolean isArchived;

        public Post(String id, String userId, String url, String type, String caption, ArrayList<String> lovedByUIDs,  boolean isArchived, Timestamp createdAt) {
            this.id = id;
            this.userId = userId;
            this.url = url;
            this.type = type;
            this.caption = caption;
            this.lovedByUIDs = lovedByUIDs;
            this.createdAt = createdAt;
            this.isArchived = isArchived;
        }
    }

    public static class SearchUser {
        public String avatarUrl;
        public String username;
        public String userId;

        public SearchUser(String avatarUrl, String username, String userId) {
            this.avatarUrl = avatarUrl;
            this.username = username;
            this.userId = userId;
        }
    }
    public static class HighlightStory {
        public String id;
        public String userId;
        public String name;
        public String coverUrl;
        public ArrayList<String> storiesID;
        public Timestamp lastEditedTime;

        public HighlightStory(String id, String userId, String name, String coverUrl, ArrayList<String> storiesID, Timestamp lastEditedTime) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.coverUrl = coverUrl;
            this.storiesID = storiesID;
            this.lastEditedTime = lastEditedTime;
        }
    }
}
