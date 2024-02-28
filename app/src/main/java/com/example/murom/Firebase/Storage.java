package com.example.murom.Firebase;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Storage {
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final StorageReference storageRef = storage.getReference();

    public static StorageReference getRef(String storagePath) {
        return storageRef.child(storagePath);
    }

    public static void uploadAsset(Uri uri, String storagePath) {
        StorageReference assetRef = storageRef.child(storagePath);
        assetRef.putFile(uri);
    }
}
