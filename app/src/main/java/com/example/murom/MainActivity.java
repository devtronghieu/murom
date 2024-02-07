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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.murom.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new NewsfeedFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_nav_newsfeed) {
                replaceFragment(new NewsfeedFragment());
            } else if (itemId == R.id.bottom_nav_post) {
                replaceFragment(new PostFragment());
            } else if (itemId == R.id.bottom_nav_search) {
                replaceFragment(new SearchFragment());
            } else if (itemId == R.id.bottom_nav_reels) {
                replaceFragment(new ReelsFragment());
            } else if (itemId == R.id.bottom_nav_profile) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });

        // Avatar
        ImageView avatar = findViewById(R.id.avatar);
        Button uploadAvatarBtn = findViewById(R.id.upload_avatar);
        uploadAvatarBtn.setOnClickListener(view -> setupImagePicker(avatar));

        // Test image
        ImageView testImageView = findViewById(R.id.test_image);
        Button testUploadImageBtn = findViewById(R.id.test_button);
        testUploadImageBtn.setOnClickListener(view -> setupImagePicker(testImageView));
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.relative_layout, fragment);
        fragmentTransaction.commit();
    }

    private void setupImagePicker(ImageView imageView) {
        this.pickedImageView = imageView;
        launcher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }
}