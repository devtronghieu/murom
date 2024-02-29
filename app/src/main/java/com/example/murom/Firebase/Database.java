package com.example.murom.Firebase;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Database {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static final CollectionReference storyCollection = db.collection("Story");

    public static void addStory(Schema.Story doc) {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("created_at", doc.createdAt);
        documentData.put("user_id", doc.uid);
        documentData.put("url", doc.url);

        storyCollection
                .add(documentData)
                .addOnSuccessListener(documentReference -> Log.d("-->", "Uploaded Story doc: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.d("-->", "Failed to add Story doc: " + e));;
    }

    public static Schema.Story getStory(String docID) {
        DocumentReference docRef = storyCollection.document(docID);

        Schema.Story story = new Schema.Story("", "", "");

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    Log.d("-->", "DocumentSnapshot data: " + document.getData());
                } else {
                    Log.d("-->", "No such document");
                }
            } else {
                Log.d("-->", "get failed with ", task.getException());
            }
        });

        return story;
    }
}
