package com.example.murom.Firebase;

import java.util.ArrayList;

public class Schema {
    public static class User {
        public String bio;
        public String email;
        public String passwordHash;
        public String profilePicture;
        public String username;
        public ArrayList<String> viewedStories;

        public User(
                String bio,
                String email,
                String passwordHash,
                String profilePicture,
                String username,
                ArrayList<String> viewedStories
        ) {
            this.bio = bio;
            this.email = email;
            this.passwordHash = passwordHash;
            this.profilePicture = profilePicture;
            this.username = username;
            this.viewedStories = viewedStories;
        }
    }

    public static class Story {
        public String createdAt;
        public String uid;
        public String url;
        public String type;

        public Story(String createdAt, String uid, String url, String type) {
            this.createdAt = createdAt;
            this.uid = uid;
            this.url = url;
            this.type = type;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }
}
