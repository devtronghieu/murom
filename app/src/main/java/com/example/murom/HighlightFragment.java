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
import com.example.murom.State.ActiveStoryState;
import com.example.murom.State.CurrentSelectedStoriesState;
import com.example.murom.State.HighlightState;
import com.example.murom.State.ProfileState;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;


public class HighlightFragment extends Fragment {
    public interface HighlightFragmentCallback {
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
    HighlightFragmentCallback callback;
    String highlightId;
    Schema.User user;

    Schema.HighlightStory curHighlight = new Schema.HighlightStory(
            "",
            "",
            "",
            "",
            new ArrayList<>(),
            Timestamp.now()
    );
    boolean isDeleteButtonShowing = false;

    int currentStoryIndex = 0;

    public HighlightFragment(HighlightFragmentCallback callback, String highlightId, Schema.User user) {
        this.callback = callback;
        this.highlightId = highlightId;
        this.user = user;
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
        deleteButton.setText("Remove from highlight");
        deleteButton.setOnClickListener(v -> {
            handleDeleteStory();
        });

        ArrayList<Schema.HighlightStory> highlights = HighlightState.getInstance().highlights;

        for (int i = 0; i < highlights.size(); i++) {
            if (highlights.get(i).id == highlightId) {
                curHighlight = highlights.get(i);
                break;
            }
        }

        Database.getStoriesByStoriesID(curHighlight.storiesID, new Database.GetStoriesByUIDCallback() {
            @Override
            public void onGetStoriesSuccess(ArrayList<Schema.Story> result) {
                CurrentSelectedStoriesState.getInstance().updateObservableStoriesMap(result);
            }

            @Override
            public void onGetStoriesFailure() {}
        });


        storyOwnerDisposable = CurrentSelectedStoriesState.getInstance().getObservableStoriesMap().subscribe(highlightStories -> {
            stories = highlightStories;

            if (stories == null || stories.size() == 0) {
                Toast.makeText(requireContext(), "loading!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Objects.equals(user.id, ProfileState.getInstance().profile.id)) {
                editButton.setVisibility(View.VISIBLE);
            }

            username.setText(user.username);
            Glide.with(this)
                    .load(user.profilePicture)
                    .centerCrop()
                    .into(avatar);

            touchSurface.setOnClickListener(v -> {
                if (currentStoryIndex < stories.size() - 1) {
                    currentStoryIndex++;
                } else {
                    callback.onClose();
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

        if (currentStoryIndex == stories.size() - 1) {
            ProfileState profileState = ProfileState.getInstance();
            Database.setViewedStory(profileState.profile.id, story.id, story.uid);
            profileState.profile.viewedStories.put(story.uid, story.id);
            ActiveStoryState.getInstance().updateObservableActiveStoriesMap(ActiveStoryState.getInstance().activeStoriesMap);
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
        deleteButton.setText("Removing...");

        Schema.Story story = stories.get(currentStoryIndex);
        stories.remove(currentStoryIndex);
        curHighlight.storiesID.remove(story.id);

        ArrayList<Schema.HighlightStory> highlightStories = HighlightState.getInstance().highlights;

        for (int i = 0; i < highlightStories.size(); i++) {
            if (highlightStories.get(i).id == curHighlight.id) {
                highlightStories.get(i).storiesID = curHighlight.storiesID;
            }
        }

        CurrentSelectedStoriesState.getInstance().updateObservableStoriesMap(stories);
        HighlightState.getInstance().updateObservableHighlights(highlightStories);
        Database.addHighlight(curHighlight, new Database.AddHighlightCallback() {
            @Override
            public void onAddHighlightSuccess() {
                Toast.makeText(requireContext(), "Add highlights successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddHighlightFailed() {

            }
        });
        hideDeleteStoryButton();
    }
}