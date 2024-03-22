package com.example.murom.Utils;

import android.content.Context;
import android.net.Uri;

public class FileUtils {
    public static boolean deleteFile(Context context, Uri fileUri) {
        if (fileUri == null) {
            return false; // Can't delete a null Uri
        }

        try {
            // Use ContentResolver to delete the file
            int rowsDeleted = context.getContentResolver().delete(fileUri, null, null);
            return rowsDeleted > 0; // True if deletion succeeded
        } catch (SecurityException e) {
            // Handle potential lack of permissions
            System.err.println("SecurityException: Lack of permissions to delete " + fileUri);
            return false;
        }
    }
}
