package com.example.murom.Firebase;

import android.util.Log;

import com.example.murom.State.ProfileState;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static void addStory(Schema.Story story) {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("created_at", story.createdAt);
        documentData.put("user_id", story.uid);
        documentData.put("url", story.url);
        documentData.put("type", story.type);

        storyCollection
                .document(story.id)
                .set(documentData)
                .addOnSuccessListener(documentReference -> Log.d("-->", "Uploaded Story doc: " + story.id))
                .addOnFailureListener(e -> Log.d("-->", "Failed to add Story doc: " + e));
    }

    public interface GetStoriesByUIDCallback {
        void onGetStoriesSuccess(ArrayList<Schema.Story> stories);
        void onGetStoriesFailure();
    }

    public static void getStoriesByUID(String uid, GetStoriesByUIDCallback callback) {
        Date yesterday = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
        Timestamp yesterdayTimestamp = new Timestamp(yesterday);

        storyCollection
                .whereEqualTo("user_id", uid)
                .whereGreaterThanOrEqualTo("created_at", yesterdayTimestamp)
                .orderBy("created_at", Query.Direction.ASCENDING)
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
                                    Timestamp.now(),
                                    "",
                                    "",
                                    ""
                            );

                            story.id = doc.getId();
                            story.createdAt = doc.getTimestamp("created_at");
                            story.uid = uid;
                            story.url = doc.getString("url");
                            story.type = doc.getString("type");

                            stories.add(story);
                        }

                        callback.onGetStoriesSuccess(stories);
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("-->", "Failed to get stories:", exception);
                        } else {
                            Log.e("-->", "Failed to get stories: Unknown reason");
                        }
                        callback.onGetStoriesFailure();
                    }
                });
    }

    public interface DeleteStoryCallback {
        void onDeleteStorySuccess(String storyID);
        void onDeleteStoryFailure();
    }
    public static void deleteStory(String storyID, DeleteStoryCallback callback) {
        DocumentReference docRef = storyCollection.document(storyID);
        docRef.delete()
                .addOnSuccessListener(runnable -> {
                    Log.d("-->", "Deleted story: " + storyID);
                    callback.onDeleteStorySuccess(storyID);
                })
                .addOnFailureListener(e -> {
                    Log.e("-->", "Failed to delete story:", e);
                    callback.onDeleteStoryFailure();
                });
    }

    public static void setViewedStory(String viewerID, String storyID, String storyUID) {
        DocumentReference userRef = userCollection.document(viewerID);
        Map<String, Object> updates = new HashMap<>();
        updates.put("viewed_stories." + storyUID, storyID);
        userRef.update(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("-->", "Update viewed_story of " + storyID);
            } else {
                Log.d("-->", "Failed to update viewed_story of " + storyID);
            }
        });
    }


    // Post Collection
    public static final CollectionReference postCollection = db.collection("Post");

    public static void addPost(Schema.Post doc) {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("created_at", doc.createdAt);
        documentData.put("user_id", doc.userId);
        documentData.put("url", doc.url);
        documentData.put("type", doc.type);
        documentData.put("caption", doc.caption);

        postCollection
                .document(doc.id)
                .set(documentData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("-->", "Uploaded post doc: " + doc.id);
                })
                .addOnFailureListener(e -> Log.d("-->", "Failed to add Story doc: " + e));;
    }

    public interface GetPostsByUIDCallback {
        void onGetPostsSuccess(ArrayList<Schema.Post> posts);
        void onGetPostsFailure();
    }

    public static void getPostsByUID(String uid, GetPostsByUIDCallback callback) {
        postCollection
                .whereEqualTo("user_id", uid)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Schema.Post> posts = new ArrayList<>();

                        QuerySnapshot snap = task.getResult();
                        List<DocumentSnapshot> docs = snap.getDocuments();

                        for (int i = 0; i < docs.size(); i++) {
                            DocumentSnapshot doc = docs.get(i);

                            Schema.Post post = new Schema.Post(
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    Timestamp.now()
                            );

                            post.id = doc.getId();
                            post.createdAt = doc.getTimestamp("created_at");
                            post.userId = doc.getString("user_id");
                            post.url = doc.getString("url");
                            post.type = doc.getString("type");
                            post.caption = doc.getString("caption");

                            posts.add(post);
                        }

                        callback.onGetPostsSuccess(posts);
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("-->", "Failed to get posts:", exception);
                        } else {
                            Log.e("-->", "Failed to get posts: Unknown reason");
                        }
                        callback.onGetPostsFailure();
                    }
                });
    }

    public interface DeletePostCallback {
        void onDeleteSuccess(String postID);
        void onDeleteFailure();
    }

    public static void deletePost(String postID, DeletePostCallback callback) {
        postCollection.document(postID).delete()
                .addOnSuccessListener(runnable -> {
                    Storage.getRef("post/" + postID).delete()
                            .addOnFailureListener(e -> {
                                Log.e("-->", "Failed to delete post on storage:", e);
                            });
                    callback.onDeleteSuccess(postID);
                })
                .addOnFailureListener(e -> {
                    Log.e("-->", "Failed to delete post on firestore:", e);
                    callback.onDeleteFailure();
                });
    }

    // Profile aka User
    public interface UpdateUserProfileCallback {
        void onSaveSuccess(Schema.User user);
        void onSaveFailure(String errorMessage);
    }

    public static void updateUserProfile( String uid, String newUsername, String newDescription, UpdateUserProfileCallback callback){
        DocumentReference docRef = userCollection.document(uid);
        ProfileState profileState = ProfileState.getInstance();
        if (newUsername.equals(""))
            newUsername = profileState.profile.username;
        if (newDescription.equals("") )
            newDescription = profileState.profile.bio;
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("bio", newDescription);
        Schema.User user = new Schema.User(
                "",
                "",
                "",
                "",
                "",
                "",
                new HashMap<>());

        user.id = profileState.profile.id;
        user.bio = newDescription;
        user.email = profileState.profile.email;
        user.passwordHash = profileState.profile.passwordHash;
        user.profilePicture = profileState.profile.profilePicture;
        user.username = newUsername;
        docRef.set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    callback.onSaveSuccess(user);
                })
                .addOnFailureListener(e -> {
                    callback.onSaveFailure(e.getMessage());
                });
    }

    public interface checkUsernameCallback {
        void onUsernameAvailable();
        void onUsernameUnavailable();
        void onUsernameCheckError(String errorMessage);
        void onCheckFailure();
    }
    public static void checkUsernameAvailability(String username, checkUsernameCallback callback) {
        db.collection("User")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            callback.onUsernameUnavailable();
                        } else {
                            callback.onUsernameAvailable();
                        }
                    } else {
                        callback.onUsernameCheckError("Error checking username: " + task.getException().getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCheckFailure();
                });

    }
    public static void addUserToFirestore(String userId, String email, String password, String username, Auth.RegistrationCallback callback) {
        Map<String, String> authInfo = new HashMap<>();
        authInfo.put("id", userId);
        authInfo.put("email", email);
        authInfo.put("password", password);
        authInfo.put("username", username);
        db.collection("User")
                .document(userId)
                .set(authInfo)
                .addOnSuccessListener(aVoid -> {
                    callback.onRegistrationSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onRegistrationFailure("Error adding user data to Firestore: " + e.getMessage());
                });
    }
}
