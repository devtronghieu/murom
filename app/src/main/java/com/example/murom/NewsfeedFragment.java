package com.example.murom;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.Recycler.PostAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.Recycler.StoryBubbleAdapter;
import com.example.murom.State.AppState;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class NewsfeedFragment extends Fragment {
    Activity activity;

    ActivityResultLauncher<PickVisualMediaRequest> launcher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                } else {
                    Date currentDate = new Date();
                    Timestamp createdAt = new Timestamp(currentDate);
                    String uid = Auth.getUser().getUid();

                    String storagePath = "story/" + uid + "/" + createdAt;

                    String mimeType = activity.getContentResolver().getType(uri);

                    boolean isImage = mimeType != null && mimeType.startsWith("image/");

                    String type;
                    if (isImage) {
                        type = "image";
                    } else {
                        type = "video";
                    }

                    Storage.getRef(storagePath).putFile(uri)
                            .addOnSuccessListener(taskSnapshot -> {
                                StorageReference storyRef = Storage.getRef(storagePath);
                                storyRef.getDownloadUrl()
                                        .addOnSuccessListener(storyURI -> {
                                            Schema.Story story = new Schema.Story(UUID.randomUUID().toString(), createdAt, uid, storyURI.toString(), type);
                                            Database.addStory(story);
                                            Toast.makeText(requireContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d("-->", "failed to get story: " + e);
                                            Toast.makeText(requireContext(), "Failed to upload story!", Toast.LENGTH_SHORT).show();
                                        });
                            }).addOnFailureListener(e -> {
                                Log.d("-->", "failed to get story: " + e);
                                Toast.makeText(requireContext(), "Failed to upload story!", Toast.LENGTH_SHORT).show();
                            });
                }
            });

    public interface  NewsfeedFragmentCallback {
        void onViewStory(String uid);
    }

    NewsfeedFragmentCallback callback;

    public NewsfeedFragment(NewsfeedFragmentCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        activity = getActivity();

        Random rand = new Random();

        AppState appState = AppState.getInstance();

        Database.getUser(appState.profile.id, new Database.GetUserCallback() {
            @Override
            public void onGetUserSuccess(Schema.User user) {
                // Stories Recycler
                RecyclerView storiesRecycler = rootView.findViewById(R.id.stories_recycler);
                storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
                storiesRecycler.addItemDecoration(new SpacingItemDecoration(40, 0));

                ArrayList<StoryBubbleAdapter.StoryBubbleModel> storyBubbles = new ArrayList<>();


                ArrayList<Schema.Story> myStories = appState.storiesMap.get(appState.profile.id);

                boolean isViewed = true;
                if (myStories != null && myStories.size() >= 1) {
                    isViewed = Objects.equals(appState.profile.viewedStories.get(appState.profile.id), myStories.get(myStories.size() - 1).id);
                }

                storyBubbles.add(new StoryBubbleAdapter.StoryBubbleModel(
                        appState.profile.id,
                        appState.profile.profilePicture,
                        "Your story",
                        isViewed
                ));

                StoryBubbleAdapter storyBubbleAdapter = new StoryBubbleAdapter(storyBubbles, new StoryBubbleAdapter.StoryBubbleCallback() {
                    @Override
                    public void handleUploadStory() {
                        launcher.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                                .build());
                    }

                    @Override
                    public void handleViewStories(String uid) {
                        callback.onViewStory(uid);
                    }
                });
                storiesRecycler.setAdapter(storyBubbleAdapter);

                // Newsfeeds Recycler
                RecyclerView postRecycler = rootView.findViewById(R.id.post_recycler);
                postRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                postRecycler.addItemDecoration(new SpacingItemDecoration(0, 45));
                ArrayList<PostAdapter.PostModel> newsfeeds = new ArrayList<>();

                for (int i = 0; i < rand.nextInt(5) + 5; i++) {
                    ArrayList<String> images = new ArrayList<>();
                    images.add("https://picsum.photos/200");

                    ArrayList<String> lovedByUsers = new ArrayList<>();
                    for (int j = 0; j < rand.nextInt(5); j++) {
                        lovedByUsers.add("username" + j);
                    }

                    newsfeeds.add(new PostAdapter.PostModel(
                            "https://picsum.photos/200",
                            "username" + i, images,
                            "Caption" + i,
                            lovedByUsers,
                            rand.nextBoolean()
                    ));
                }
                PostAdapter postAdapter = new PostAdapter(newsfeeds);
                postRecycler.setAdapter(postAdapter);
            }

            @Override
            public void onGetUserFailure() {
                Toast.makeText(requireContext(), "Failed to load data!", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}