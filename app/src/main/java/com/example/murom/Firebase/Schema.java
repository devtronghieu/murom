package com.example.murom.Firebase;

public class Schema {
    public static class Story {
        public String createdAt;
        public String uid;
        public String url;

        public Story(String createdAt, String uid, String url) {
            this.createdAt = createdAt;
            this.uid = uid;
            this.url = url;
        }
    }
}
