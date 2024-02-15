package com.example.murom;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;

public class StorageUtils {
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final StorageReference storageRef = storage.getReference();

    public static void uploadImage(Context context, Uri uri, String storagePath) {
        StorageReference imageRef = storageRef.child(storagePath);

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            assert inputStream != null;
            imageRef.putStream(inputStream);
        } catch (IOException e) {
            Log.d("-->", "uploadImage error: " + e);
        }
    }
}
