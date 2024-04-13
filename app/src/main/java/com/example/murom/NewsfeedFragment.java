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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.Recycler.PostAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.Recycler.StoryBubbleAdapter;
import com.example.murom.State.ActiveStoryState;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.rxjava3.disposables.Disposable;

public class NewsfeedFragment extends Fragment {
    Activity activity;

    // AppState
    Disposable storiesMapDisposable;
    Disposable socialPostsDisposable;

    int offset = 0;
    int limit = 20;

    // Components
    RecyclerView storiesRecycler;
    RecyclerView postRecycler;
    SwipeRefreshLayout swipeRefreshLayout;

    ActivityResultLauncher<PickVisualMediaRequest> launcher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                    return;
                }

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

                                        ActiveStoryState activeStoryState = ActiveStoryState.getInstance();
                                        HashMap<String, ArrayList<Schema.Story>> newStoriesMap = activeStoryState.activeStoriesMap;
                                        ArrayList<Schema.Story> myStories = activeStoryState.activeStoriesMap.get(uid);
                                        if (myStories == null) {
                                            myStories = new ArrayList<>();
                                        }
                                        myStories.add(story);
                                        activeStoryState.updateObservableActiveStoriesMap(newStoriesMap);

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

        // Stories
        storiesRecycler = rootView.findViewById(R.id.stories_recycler);
        storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        storiesRecycler.addItemDecoration(new SpacingItemDecoration(40, 0));

        storiesMapDisposable = ActiveStoryState.getInstance().getObservableActiveStoriesMap().subscribe(this::handleWatchStoriesToRender);

        // Social Posts
        postRecycler = rootView.findViewById(R.id.post_recycler);
        postRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        postRecycler.addItemDecoration(new SpacingItemDecoration(0, 45));

        PostState postState = PostState.getInstance();
        ProfileState profileState = ProfileState.getInstance();

        socialPostsDisposable = postState.getObservableSocialPosts().subscribe(posts -> {
            ArrayList<PostAdapter.PostModel> postModels = new ArrayList<>();
            posts.forEach(post -> {
                Schema.User postOwnerProfile = profileState.followerProfileMap.get(post.userId);

                if (postOwnerProfile != null) {
                    ArrayList<String> images = new ArrayList<>();
                    images.add(post.url);
                    postModels.add(new PostAdapter.PostModel(
                            post.id,
                            postOwnerProfile.profilePicture,
                            postOwnerProfile.username,
                            images,
                            post.caption,
                            post.lovedByUIDs
                    ));
                }
            });
            this.setNewsfeeds(postModels);
        });

        // Swipe to refresh the posts
        swipeRefreshLayout = rootView.findViewById(R.id.post_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            PostState.getInstance().constructObservableSocialPosts(offset, limit);
            swipeRefreshLayout.setRefreshing(false);
        });

        // Fetch posts
        PostState.getInstance().constructObservableSocialPosts(offset, limit);

        // Fetch stories
        ArrayList<String> socialIDs = new ArrayList<>(profileState.followerIDs);
        socialIDs.add(profileState.profile.id);
        Database.getActiveStories(socialIDs, new Database.GetActiveStoriesCallback() {
            @Override
            public void onGetStoriesSuccess(HashMap<String, ArrayList<Schema.Story>> storyMap) {
                ActiveStoryState.getInstance().updateObservableActiveStoriesMap(storyMap);
            }

            @Override
            public void onGetStoriesFailure() {
                Toast.makeText(requireContext(), "Failed to load stories", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (!storiesMapDisposable.isDisposed()) {
            storiesMapDisposable.dispose();
        }

        if (!socialPostsDisposable.isDisposed()) {
            socialPostsDisposable.dispose();
        }

        super.onDestroyView();
    }

    void handleWatchStoriesToRender(HashMap<String, ArrayList<Schema.Story>> storiesMap) {
        ArrayList<StoryBubbleAdapter.StoryBubbleModel> storyBubbles = new ArrayList<>();
        ProfileState profileState = ProfileState.getInstance();
        Schema.User profile = profileState.profile;

        ArrayList<Schema.Story> myStories = storiesMap.get(profile.id);
        Log.d("-->", "stories map: " + storiesMap);
        Log.d("-->", "my stories: " + myStories);
        if (myStories != null) {
            StoryBubbleAdapter.StoryBubbleModel myStoryBubble = new StoryBubbleAdapter.StoryBubbleModel(
                    profile.id,
                    profile.profilePicture,
                    "Your story",
                    myStories.size(),
                    Objects.equals(profile.viewedStories.get(profile.id), myStories.get(myStories.size() - 1).id)
            );
            storyBubbles.add(myStoryBubble);
        }

        profileState.followerIDs.forEach(id -> {
            Schema.User ownerProfile = profileState.followerProfileMap.get(id);
            if (ownerProfile == null) {
                return;
            }

            ArrayList<Schema.Story> stories = storiesMap.get(ownerProfile.id);
            if (stories == null) {
                return;
            }

            StoryBubbleAdapter.StoryBubbleModel storyBubble = new StoryBubbleAdapter.StoryBubbleModel(
                    id,
                    ownerProfile.profilePicture,
                    ownerProfile.username,
                    stories.size(),
                    Objects.equals(profile.viewedStories.get(profile.id), stories.get(stories.size() - 1).id)
            );

            storyBubbles.add(storyBubble);
        });

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
    }

    void setNewsfeeds(ArrayList<PostAdapter.PostModel> newsfeeds) {
        PostAdapter postAdapter = new PostAdapter(newsfeeds);
        postRecycler.setAdapter(postAdapter);
    }
}