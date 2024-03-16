package com.example.murom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.murom.State.ProfileState;
import com.example.murom.State.StoryState;

import java.util.ArrayList;
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

    ArrayList<Schema.Story> stories;
    StoryFragmentCallback callback;

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

        StoryState storyState = StoryState.getInstance();

        storyOwnerDisposable = storyState.getObservableStoryOwner().subscribe(profile -> {
            stories = storyState.storiesMap.get(profile.id);
            Log.d("-->", "stories: " + profile.id);

            if (stories == null || stories.size() == 0) {
                Toast.makeText(requireContext(), "No stories found!", Toast.LENGTH_SHORT).show();
                return;
            }

            username.setText(profile.username);
            Glide.with(this).load(profile.profilePicture).into(avatar);

            touchSurface.setOnClickListener(v -> {
                if (currentStoryIndex < stories.size() - 1) {
                    currentStoryIndex++;
                } else {
                    currentStoryIndex = 0;
                }

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
}