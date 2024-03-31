package com.example.murom.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class BitmapUtils {
    public static Uri bitmapToUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Unnamed", null);
        return Uri.parse(path);
    }

    public static Uri bitmapToUri(Context context, Bitmap bitmap, String title, String desc) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, title, desc);
        return Uri.parse(path);
    }

    public static Bitmap flipHorizontally(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flipVertically(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap crop(Bitmap bitmap, int startX, int startY, int width, int height) {
        return Bitmap.createBitmap(bitmap, startX, startY, width, height);
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap getVideoThumbnail(String filePath, int width, int height) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File videoFile = new File(filePath);
            try {
                ThumbnailUtils.createVideoThumbnail(videoFile, new Size(width, height), new CancellationSignal());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
            } catch (NoSuchMethodError | IllegalArgumentException e) {
                // Handle potential errors due to deprecated method being unavailable
                return null; // Or provide a default fallback thumbnail
            }
        }

        return null;
    }
}
