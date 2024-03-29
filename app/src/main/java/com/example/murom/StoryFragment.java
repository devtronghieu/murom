package com.example.murom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.State.ProfileState;
import com.example.murom.State.StoryState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;


public class StoryFragment extends Fragment {
    public interface  StoryFragmentCallback {
        void onClose();
    }

    Disposable storyOwnerDisposable;

    ProgressBar progressBar;
    ImageView imageView;
    VideoView videoView;
    ConstraintLayout touchSurface;
    ImageButton editButton;
    Button deleteButton;

    ArrayList<Schema.Story> stories;
    StoryFragmentCallback callback;

    boolean isDeleteButtonShowing = false;

    int currentStoryIndex = 0;

    public StoryFragment(StoryFragmentCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story, container, false);

        TextView username = rootView.findViewById(R.id.story_fragment_username);
        ImageView avatar = rootView.findViewById(R.id.story_fragment_avatar);
        progressBar = rootView.findViewById(R.id.story_fragment_image_loading);
        imageView = rootView.findViewById(R.id.story_fragment_image);
        videoView = rootView.findViewById(R.id.story_fragment_video);
        touchSurface = rootView.findViewById(R.id.story_fragment_touch_surface);

        ImageButton closeBtn = rootView.findViewById(R.id.story_fragment_close_button);
        closeBtn.setOnClickListener(v -> callback.onClose());

        editButton = rootView.findViewById(R.id.story_fragment_edit_button);
        editButton.setOnClickListener(v -> {
            if (isDeleteButtonShowing) {
                hideDeleteStoryButton();
            } else {
                showDeleteStoryButton();
            }
        });

        deleteButton = rootView.findViewById(R.id.story_fragment_delete_button);
        deleteButton.setOnClickListener(v -> {
            handleDeleteStory();
        });

        StoryState storyState = StoryState.getInstance();

        storyOwnerDisposable = storyState.getObservableStoryOwner().subscribe(profile -> {
            stories = storyState.storiesMap.get(profile.id);

            if (stories == null || stories.size() == 0) {
                Toast.makeText(requireContext(), "No stories found!", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < stories.size(); i++) {
                Log.d("-->", "story: " + stories.get(i).id);
            }

            if (Objects.equals(profile.id, ProfileState.getInstance().profile.id)) {
                editButton.setVisibility(View.VISIBLE);
            }

            username.setText(profile.username);
            Glide.with(this).load(profile.profilePicture).into(avatar);

            touchSurface.setOnClickListener(v -> {
                if (currentStoryIndex < stories.size() - 1) {
                    currentStoryIndex++;
                } else {
                    currentStoryIndex = 0;
                }

                hideDeleteStoryButton();
                viewCurrentStory();
            });

            viewCurrentStory();
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (!storyOwnerDisposable.isDisposed()) {
            storyOwnerDisposable.dispose();
        }

        super.onDestroyView();
    }

    void viewCurrentStory() {
        Schema.Story story = stories.get(currentStoryIndex);

        if (currentStoryIndex == stories.size() - 1) {
            ProfileState profileState = ProfileState.getInstance();

            Database.setViewedStory(profileState.profile.id, story.id, story.uid);

            Schema.User newProfile = profileState.profile;
            newProfile.viewedStories.put(newProfile.id, story.id);

            ProfileState.getInstance().updateObservableProfile(newProfile);
        }

        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (Objects.equals(story.type, "image")) {
            Glide.with(this).load(story.url).into(imageView);
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            videoView.setVideoPath(story.url);
            videoView.setOnPreparedListener(mediaPlayer -> progressBar.setVisibility(View.GONE));
            videoView.setVisibility(View.VISIBLE);
            videoView.start();
        }
    }

    void showDeleteStoryButton() {
        isDeleteButtonShowing = true;
        deleteButton.setVisibility(View.VISIBLE);
    }

    void hideDeleteStoryButton() {
        isDeleteButtonShowing = false;
        deleteButton.setVisibility(View.GONE);
    }

    void handleDeleteStory() {
        deleteButton.setEnabled(false);
        deleteButton.setBackgroundColor(getResources().getColor(R.color.error_200, null));
        deleteButton.setText(R.string.deleting);

        Schema.Story story = stories.get(currentStoryIndex);
        Database.deleteStory(story.id, new Database.DeleteStoryCallback() {
            @Override
            public void onDeleteStorySuccess(String storyID) {
                StoryState instance = StoryState.getInstance();
                HashMap<String, ArrayList<Schema.Story>> storiesMap = instance.storiesMap;
                String uid = ProfileState.getInstance().profile.id;

                String storagePath = "story/" + uid + "/" + story.createdAt;
                Storage.getRef(storagePath).delete()
                        .addOnSuccessListener(runnable -> {
                            if (storiesMap.get(uid) != null) {
                                Objects.requireNonNull(storiesMap.get(uid)).remove(currentStoryIndex);
                            }

                            instance.updateObservableStoriesMap(storiesMap);

                            if (stories.size() == 0) {
                                callback.onClose();
                            } else {
                                if (currentStoryIndex != 0) {
                                    currentStoryIndex--;
                                }

                                hideDeleteStoryButton();
                                viewCurrentStory();
                            }

                            deleteButton.setEnabled(true);
                            deleteButton.setBackgroundColor(getResources().getColor(R.color.white, null));
                            deleteButton.setText(R.string.delete_this_story);

                            Toast.makeText(requireContext(), "Delete story success", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            deleteButton.setEnabled(true);
                            deleteButton.setBackgroundColor(getResources().getColor(R.color.white, null));
                            deleteButton.setText(R.string.delete_this_story);

                            Log.d("-->", "failed to delete story ref");
                            Toast.makeText(requireContext(), "Failed to delete story", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onDeleteStoryFailure() {
                Toast.makeText(requireContext(), "Failed to delete story", Toast.LENGTH_SHORT).show();
            }
        });
    }
}