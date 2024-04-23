package com.example.murom;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.State.ProfileState;
import com.example.murom.State.ActiveStoryState;
import com.example.murom.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton home;
    View fragmentContainer;
    View fullscreenFragmentContainer;
    BottomNavigationView bottomMenu;

    // State
    Disposable profileDisposable;
    Disposable followerIDsDisposable;
    boolean isReadyToFetchFollowers = false;

    // Fragments
    FragmentManager fragmentManager;

    Fragment postFragment;
    Fragment searchFragment;
    Fragment newsfeedFragment;
    Fragment reelsFragment;
    Fragment profileFragment;
    Fragment storyFragment;
    Fragment editProfileFragment;
    Fragment archiveFragment;
    Fragment otherProfileFragment;
    Fragment detailPostFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup env
        String uid = Auth.getUser().getUid();
        ProfileState profileState = ProfileState.getInstance();

        setupFragments();
        setupBottomNavigation();

        Database.getUser(uid, new Database.GetUserCallback() {
            @Override
            public void onGetUserSuccess(Schema.User user) {
                profileState.updateObservableProfile(user);
            }

            @Override
            public void onGetUserFailure() {
                Toast.makeText(MainActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                handleSignOut();
            }
        });

        profileDisposable = ProfileState.getInstance().getObservableProfile().subscribe(profile -> {
            if (!Objects.equals(profile.id, "")) {
                Database.getFollowersByUID(profile.id, new Database.GetFollowersByUIDCallback() {
                    @Override
                    public void onGetFollowersByUIDSuccess(ArrayList<String> followerIDs) {
                        isReadyToFetchFollowers = true;
                        ProfileState.getInstance().updateObservableFollowerIDs(followerIDs);
                    }

                    @Override
                    public void onGetFollowersByUIDFailure() {
                        Log.d("-->", "failed to get follower ids");
                    }
                });
            }
        });

        followerIDsDisposable = ProfileState.getInstance().getObservableFollowerIDs().subscribe(ids -> {
            if (isReadyToFetchFollowers) {
                Database.getProfiles(ids, new Database.GetProfilesCallback() {
                    @Override
                    public void onGetProfilesSuccess(HashMap<String, Schema.User> profileMap) {
                        profileMap.put(profileState.profile.id, profileState.profile);
                        ProfileState.getInstance().updateObservableFollowerProfileMap(profileMap);
                        launchNewsFeedFragmentOnStartup();
                    }

                    @Override
                    public void onGetProfilesFailure() {
                        Toast.makeText(MainActivity.this, "Failed to fetch followers' data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!profileDisposable.isDisposed()) {
            profileDisposable.dispose();
        }

        if (!followerIDsDisposable.isDisposed()) {
            followerIDsDisposable.dispose();
        }
    }

    private void setupFragments() {
        fragmentManager = getSupportFragmentManager();
        postFragment = new PostFragment();
        searchFragment = new SearchFragment(this::handleOnSearchUserClick);
        newsfeedFragment = new NewsfeedFragment(new NewsfeedFragment.NewsfeedFragmentCallback() {
            @Override
            public void onViewStory(String uid) {
                handleViewStory(uid);
            }

            @Override
            public void onViewProfile(String uid) {
                handleOnSearchUserClick(uid);
            }
        });
        reelsFragment = new ReelsFragment();
        profileFragment = new ProfileFragment(new ProfileFragment.ProfileFragmentCallback() {
            @Override
            public void onEditProfile() {
                handleEditProfile();
            }
            @Override
            public void onArchiveClick() {
                handleOnArchiveClick();
            }
            @Override
            public void onPostClick(String postId) {handleOnPostClick(postId);}
        });

    }

    private void setupBottomNavigation() {
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
    }

    private void launchNewsFeedFragmentOnStartup() {
        replaceFragment(newsfeedFragment);
        setHomeActive();
        ProgressBar loadingBlock = findViewById(R.id.main_loading);
        loadingBlock.setVisibility(View.GONE);
    }

    private void handleSignOut() {
        Auth.signOut();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
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
        ActiveStoryState.getInstance().updateObservableActiveStoryOwner(uid);
        storyFragment = new StoryFragment(() -> removeFullscreenFragment(storyFragment));
        addFullscreenFragment(storyFragment);
    }

    private void handleEditProfile(){
        editProfileFragment = new EditProfileFragment(() -> removeFullscreenFragment(editProfileFragment));
        addFullscreenFragment(editProfileFragment);
    }

    private void handleOnArchiveClick(){
        archiveFragment = new ArchiveFragment(() -> removeFullscreenFragment(archiveFragment));
        addFullscreenFragment(archiveFragment);
    }

    private void handleOnSearchUserClick(String uid) {
        otherProfileFragment = OtherProfileFragment.newInstance(uid, this::handleOnPostClick);
        replaceFragmentWithBackStack(otherProfileFragment);
    }

    private void replaceFragmentWithBackStack(Fragment fragment) {
        fullscreenFragmentContainer.setVisibility(View.GONE);
        toggleBottomMenu(true);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.main_layout_fragment, fragment);
        fragmentTransaction.commit();
    }

    private void handleOnPostClick(String postId){
        detailPostFragment = DetailPostFragment.newInstance(postId);
        replaceFragmentWithBackStack(detailPostFragment);
    }


}