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
import com.example.murom.Recycler.NewsfeedAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.Recycler.StoryBubbleAdapter;
import com.google.firebase.storage.StorageReference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

public class NewsfeedFragment extends Fragment {
    Activity activity;

    ActivityResultLauncher<PickVisualMediaRequest> launcher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                } else {
                    String createdAt = Instant.now().toString();
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
                                            Schema.Story story = new Schema.Story(createdAt, uid, storyURI.toString(), type);
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

        String uid = Auth.getUser().getUid();

        Database.getUser(uid, new Database.GetUserCallback() {
            @Override
            public void onGetUserSuccess(Schema.User user) {
                // Stories Recycler
                RecyclerView storiesRecycler = rootView.findViewById(R.id.stories_recycler);
                storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
                storiesRecycler.addItemDecoration(new SpacingItemDecoration(40, 0));

                ArrayList<StoryBubbleAdapter.StoryBubbleModel> stories = new ArrayList<>();
                stories.add(new StoryBubbleAdapter.StoryBubbleModel(
                        uid,
                        user.profilePicture,
                        "Your story",
                        false
                ));

                StoryBubbleAdapter storyBubbleAdapter = new StoryBubbleAdapter(stories, new StoryBubbleAdapter.StoryBubbleCallback() {
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
                RecyclerView newsfeedsRecycler = rootView.findViewById(R.id.newsfeeds_recycler);
                newsfeedsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                newsfeedsRecycler.addItemDecoration(new SpacingItemDecoration(0, 45));
                ArrayList<NewsfeedAdapter.NewsfeedModel> newsfeeds = new ArrayList<>();

                for (int i = 0; i < rand.nextInt(5) + 5; i++) {
                    ArrayList<String> images = new ArrayList<>();
                    images.add("https://picsum.photos/200");

                    ArrayList<String> lovedByUsers = new ArrayList<>();
                    for (int j = 0; j < rand.nextInt(5); j++) {
                        lovedByUsers.add("username" + j);
                    }

                    newsfeeds.add(new NewsfeedAdapter.NewsfeedModel(
                            "https://picsum.photos/200",
                            "username" + i, images,
                            "Caption" + i,
                            lovedByUsers,
                            rand.nextBoolean()
                    ));
                }
                NewsfeedAdapter newsfeedAdapter = new NewsfeedAdapter(newsfeeds);
                newsfeedsRecycler.setAdapter(newsfeedAdapter);
            }

            @Override
            public void onGetUserFailure() {
                Toast.makeText(requireContext(), "Failed to load data!", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}