package com.example.murom;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {
    ImageView pickedImageView;
    ActivityResultLauncher<PickVisualMediaRequest> launcher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri o) {
                    if (o == null) {
                        Toast.makeText(MainActivity.this, "No image selected!", Toast.LENGTH_SHORT).show();
                    } else {
                        Glide.with(getApplicationContext()).load(o).into(pickedImageView);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Avatar
        ImageView avatar = findViewById(R.id.avatar);
        Button uploadAvatarBtn = findViewById(R.id.upload_avatar);
        uploadAvatarBtn.setOnClickListener(view -> setupImagePicker(avatar));

        // Test image
        ImageView testImageView = findViewById(R.id.test_image);
        Button testUploadImageBtn = findViewById(R.id.test_button);
        testUploadImageBtn.setOnClickListener(view -> setupImagePicker(testImageView));
    }

    private void setupImagePicker(ImageView imageView) {
        this.pickedImageView = imageView;
        launcher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }
}