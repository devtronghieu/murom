package com.example.murom;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.murom.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FloatingActionButton home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        home = findViewById(R.id.bottom_nav_home);
        setContentView(binding.getRoot());
        replaceFragment(new NewsfeedFragment());

        binding.bottomNavigation.setItemActiveIndicatorEnabled(false);
        binding.bottomNavigation.setItemRippleColor(null);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_nav_post) {
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

        binding.bottomNavHome.setOnClickListener(v -> {
            replaceFragment(new NewsfeedFragment());
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_layout_fragment, fragment);
        fragmentTransaction.commit();
    }
}