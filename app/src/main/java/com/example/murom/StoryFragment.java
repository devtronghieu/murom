package com.example.murom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
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

    String uid;
    StoryFragmentCallback callback;
    ArrayList<Schema.Story> stories;
    int currentStoryIndex = 0;

    public StoryFragment(String uid, StoryFragmentCallback callback) {
        this.uid = uid;
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story, container, false);

        Database.getStoriesByUID(this.uid, new Database.GetStoriesByUIDCallback() {
            @Override
            public void onGetStoriesSuccess(ArrayList<Schema.Story> storyList) {
                stories = storyList;
                setStory(stories.get(currentStoryIndex));
            }

            @Override
            public void onGetStoriesFailure() {
                Toast.makeText(requireContext(), "Failed to load stories", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });

        ImageButton closeBtn = rootView.findViewById(R.id.story_fragment_close_button);
        closeBtn.setOnClickListener(v -> {
            callback.onClose();
        });

        progressBar = rootView.findViewById(R.id.story_fragment_image_loading);
        imageView = rootView.findViewById(R.id.story_fragment_image);
        videoView = rootView.findViewById(R.id.story_fragment_video);
        touchSurface = rootView.findViewById(R.id.story_fragment_touch_surface);

        touchSurface.setOnClickListener(v -> {
            if (currentStoryIndex < stories.size() - 1) {
                currentStoryIndex++;
            } else {
                currentStoryIndex = 0;
            }

            Log.d("-->", "story: " + currentStoryIndex);
            setStory(stories.get(currentStoryIndex));
        });

        return rootView;
    }

    void setStory(Schema.Story story) {
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (Objects.equals(story.type, "image")) {
            Glide.with(this).load(story.url).into(imageView);
            imageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            videoView.setVideoPath(story.url);
            videoView.setOnPreparedListener(mediaPlayer -> {
                progressBar.setVisibility(View.GONE);
            });
            videoView.setVisibility(View.VISIBLE);
            videoView.start();
        }
    }
}