package com.example.murom;

import android.os.Bundle;
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
import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;

import java.util.ArrayList;
import java.util.Objects;


public class StoryFragment extends Fragment {
    public interface  StoryFragmentCallback {
        void onClose();
    }

    ProgressBar progressBar;
    ImageView imageView;
    VideoView videoView;
    ConstraintLayout touchSurface;


    String viewerID;
    ArrayList<Schema.Story> stories;
    Schema.User profile;
    StoryFragmentCallback callback;

    int currentStoryIndex = 0;

    public StoryFragment(ArrayList<Schema.Story> stories, Schema.User profile, StoryFragmentCallback callback) {
        this.stories = stories;
        this.profile = profile;
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story, container, false);

        if (stories.size() == 0) {
            Toast.makeText(requireContext(), "No stories found!", Toast.LENGTH_SHORT).show();
            callback.onClose();
            return rootView;
        }

        viewerID = Auth.getUser().getUid();

        ImageButton closeBtn = rootView.findViewById(R.id.story_fragment_close_button);
        closeBtn.setOnClickListener(v -> callback.onClose());

        TextView username = rootView.findViewById(R.id.story_fragment_username);
        ImageView avatar = rootView.findViewById(R.id.story_fragment_avatar);
        progressBar = rootView.findViewById(R.id.story_fragment_image_loading);
        imageView = rootView.findViewById(R.id.story_fragment_image);
        videoView = rootView.findViewById(R.id.story_fragment_video);
        touchSurface = rootView.findViewById(R.id.story_fragment_touch_surface);

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

        return rootView;
    }

    void viewCurrentStory() {
        Schema.Story story = stories.get(currentStoryIndex);

        if (currentStoryIndex == stories.size() - 1) {
            Database.setViewedStory(viewerID, story.id, story.uid);
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