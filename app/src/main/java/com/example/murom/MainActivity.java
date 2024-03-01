package com.example.murom;


import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton home;
    View fragmentContainer;
    View fullscreenFragmentContainer;
    BottomNavigationView bottomMenu;

    // Fragments
    FragmentManager fragmentManager;

    Fragment postFragment;
    Fragment searchFragment;
    Fragment newsfeedFragment;
    Fragment reelsFragment;
    Fragment profileFragment;
    Fragment storyFragment;

    String uid;
    HashMap<String, ArrayList<Schema.Story>> storiesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup env
        uid = Auth.getUser().getUid();

        // Setup fragments
        fragmentManager = getSupportFragmentManager();
        postFragment = new PostFragment();
        searchFragment = new SearchFragment();
        newsfeedFragment = new NewsfeedFragment(this::handleViewStory);
        reelsFragment = new ReelsFragment();
        profileFragment = new ProfileFragment();

        // Setup bottom nav
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomMenu = binding.bottomNavigation;

        fragmentContainer = findViewById(R.id.main_layout_fragment);
        fullscreenFragmentContainer = findViewById(R.id.main_layout_fullscreen_fragment);

        home = findViewById(R.id.bottom_nav_home);

        bottomMenu = findViewById(R.id.bottom_navigation);
        bottomMenu.setItemActiveIndicatorEnabled(false);
        bottomMenu.setItemRippleColor(null);
        bottomMenu.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_nav_post) {
                replaceFragment(postFragment);
            } else if (itemId == R.id.bottom_nav_search) {
                replaceFragment(searchFragment);
            } else if (itemId == R.id.bottom_nav_home_hidden) {
                replaceFragment(newsfeedFragment);
            } else if (itemId == R.id.bottom_nav_reels) {
                replaceFragment(reelsFragment);
            } else if (itemId == R.id.bottom_nav_profile) {
                replaceFragment(profileFragment);
            }
            return true;
        });

        binding.bottomNavHome.setOnClickListener(v -> {
            replaceFragment(newsfeedFragment);
            setHomeActive();
        });

        // Set default fragment to Newsfeed (Home icon)
        replaceFragment(newsfeedFragment);
        setHomeActive();

        // Stories
        Database.getStoriesByUID(this.uid, new Database.GetStoriesByUIDCallback() {
            @Override
            public void onGetStoriesSuccess(ArrayList<Schema.Story> stories) {
                storiesMap.put(uid, stories);
            }

            @Override
            public void onGetStoriesFailure() {
                Toast.makeText(MainActivity.this, "Failed to load stories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        fullscreenFragmentContainer.setVisibility(View.GONE);
        toggleBottomMenu(true);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_layout_fragment, fragment);
        fragmentTransaction.commit();
    }

    private void addFullscreenFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_layout_fullscreen_fragment, fragment);
        fragmentTransaction.commit();

        toggleBottomMenu(false);
        fullscreenFragmentContainer.setVisibility(View.VISIBLE);
    }

    private void removeFullscreenFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
        toggleBottomMenu(true);
    }

    // true to open, false to close
    private void toggleBottomMenu(boolean flag) {
        if (flag) {
            bottomMenu.setVisibility(View.VISIBLE);
            home.setVisibility(View.VISIBLE);
        } else {
            bottomMenu.setVisibility(View.GONE);
            home.setVisibility(View.GONE);
        }
    }

    private void setHomeActive() {
        bottomMenu.getMenu().findItem(R.id.bottom_nav_home_hidden).setChecked(true);
    }

    private void handleViewStory(String uid) {
        ArrayList<Schema.Story> stories = storiesMap.get(uid);
        if (stories != null) {
            storyFragment = new StoryFragment(stories, () -> removeFullscreenFragment(storyFragment));
            addFullscreenFragment(storyFragment);
        } else {
            Toast.makeText(MainActivity.this, "Failed to view story of " + uid, Toast.LENGTH_SHORT).show();
        }
    }
}