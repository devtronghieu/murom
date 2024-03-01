package com.example.murom.Firebase;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Database {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static final CollectionReference userCollection = db.collection("User");

    public interface GetUserCallback {
        void onGetUserSuccess(Schema.User user);
        void onGetUserFailure();
    }

    public static void getUser(String uid, GetUserCallback callback) {
        DocumentReference docRef = userCollection.document(uid);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    Schema.User user = new Schema.User(
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            new HashMap<>()
                    );

                    user.id = document.getString("id");
                    user.bio = document.getString("bio");
                    user.email = document.getString("email");
                    user.passwordHash = document.getString("password");
                    user.profilePicture = document.getString("profile_picture");
                    user.username = document.getString("username");
                    HashMap<String, String> viewedStories = (HashMap<String, String>) document.get("viewed_stories");
                    if (viewedStories != null) {
                        user.viewedStories = viewedStories;
                    }

                    callback.onGetUserSuccess(user);
                } else {
                    callback.onGetUserFailure();
                }
            } else {
                callback.onGetUserFailure();
            }
        });
    }

    public static final CollectionReference storyCollection = db.collection("Story");

    public static void addStory(Schema.Story doc) {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("created_at", doc.createdAt);
        documentData.put("user_id", doc.uid);
        documentData.put("url", doc.url);
        documentData.put("type", doc.type);

        String storyID = UUID.randomUUID().toString();

        storyCollection
                .document(storyID)
                .set(documentData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("-->", "Uploaded Story doc: " + storyID);
                })
                .addOnFailureListener(e -> Log.d("-->", "Failed to add Story doc: " + e));;
    }

    public interface GetStoriesByUIDCallback {
        void onGetStoriesSuccess(ArrayList<Schema.Story> stories);
        void onGetStoriesFailure();
    }

    public static void getStoriesByUID(String uid, GetStoriesByUIDCallback callback) {
        storyCollection
                .whereEqualTo("user_id", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Schema.Story> stories = new ArrayList<>();

                        QuerySnapshot snap = task.getResult();
                        List<DocumentSnapshot> docs = snap.getDocuments();

                        for (int i = 0; i < docs.size(); i++) {
                            DocumentSnapshot doc = docs.get(i);

                            Schema.Story story = new Schema.Story(
                                    "",
                                    "",
                                    "",
                                    ""
                            );

                            story.id = doc.getString("id");
                            story.createdAt = doc.getString("created_at");
                            story.uid = uid;
                            story.url = doc.getString("url");
                            story.type = doc.getString("type");

                            stories.add(story);
                        }

                        stories.sort(Comparator.comparing(Schema.Story::getCreatedAt));

                        callback.onGetStoriesSuccess(stories);
                    } else {
                        callback.onGetStoriesFailure();
                    }
                });
    }
}
