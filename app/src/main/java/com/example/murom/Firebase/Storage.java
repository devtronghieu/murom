package com.example.murom.Firebase;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Storage {
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final StorageReference storageRef = storage.getReference();
    public static String downloadURL;
    public static StorageReference getRef(String storagePath) {
        return storageRef.child(storagePath);
    }

    public static void uploadAsset(Uri uri, String storagePath) {
        StorageReference assetRef = storageRef.child(storagePath);
        assetRef.putFile(uri);
    }
    public static void uploadAvatarAsset(Uri uri, String storagePath, OnSuccessListener<Uri> successListener) {
        StorageReference assetRef = storageRef.child(storagePath);
        assetRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Retrieve the download URL of the uploaded image
                    assetRef.getDownloadUrl().addOnSuccessListener(successListener);
                })
                .addOnFailureListener(exception -> {
                    // Handle any errors during the upload process
                    // Call the success listener with null URL to indicate failure
                    successListener.onSuccess(null);
                });
    }
}
