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
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.example.murom.State.StoryState;
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
    Disposable profileDisposable;
    Disposable storiesMapDisposable;
    Disposable socialPostsDisposable;

    int offset = 0;
    int limit = 20;

    // Components
    RecyclerView storiesRecycler;
    RecyclerView postRecycler;
    SwipeRefreshLayout swipeRefreshLayout;

    // Component states
    ArrayList<StoryBubbleAdapter.StoryBubbleModel> storyBubbles = new ArrayList<>();

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

                                            StoryState storyState = StoryState.getInstance();
                                            HashMap<String, ArrayList<Schema.Story>> newStoriesMap = storyState.storiesMap;
                                            ArrayList<Schema.Story> myStories = storyState.storiesMap.get(uid);
                                            if (myStories == null) {
                                                myStories = new ArrayList<>();
                                            }
                                            myStories.add(story);
                                            storyState.updateObservableStoriesMap(newStoriesMap);

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

        // Stories
        storiesRecycler = rootView.findViewById(R.id.stories_recycler);
        storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        storiesRecycler.addItemDecoration(new SpacingItemDecoration(40, 0));

        profileDisposable = ProfileState.getInstance().getObservableProfile().subscribe(profile -> {
            handleWatchStoriesToRender(profile, StoryState.getInstance().storiesMap);
        });

        storiesMapDisposable = StoryState.getInstance().getObservableStoriesMap().subscribe(storiesMap -> {
            handleWatchStoriesToRender(ProfileState.getInstance().profile, storiesMap);
        });

        // Social Posts
        postRecycler = rootView.findViewById(R.id.post_recycler);
        postRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        postRecycler.addItemDecoration(new SpacingItemDecoration(0, 45));

        PostState postState = PostState.getInstance();
        ProfileState profileState = ProfileState.getInstance();

        socialPostsDisposable = postState.getObservableSocialPosts().subscribe(posts -> {
            ArrayList<PostAdapter.PostModel> postModels = new ArrayList<>();
            Log.d("-->", "Newsfeed posts: " + posts);
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
        String uid = ProfileState.getInstance().profile.id;
        Database.getActiveStoriesByUID(uid, new Database.GetStoriesByUIDCallback() {
            @Override
            public void onGetStoriesSuccess(ArrayList<Schema.Story> stories) {
                HashMap<String, ArrayList<Schema.Story>> storiesMap = new HashMap<>();
                storiesMap.put(uid, stories);
                StoryState.getInstance().updateObservableStoriesMap(storiesMap);
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
        if (!profileDisposable.isDisposed()) {
            profileDisposable.dispose();
        }

        if (!storiesMapDisposable.isDisposed()) {
            storiesMapDisposable.dispose();
        }

        if (!socialPostsDisposable.isDisposed()) {
            socialPostsDisposable.dispose();
        }

        super.onDestroyView();
    }

    void handleWatchStoriesToRender(Schema.User profile, HashMap<String, ArrayList<Schema.Story>> storiesMap) {
        if (Objects.equals(profile.id, "")) return;

        ArrayList<Schema.Story> myStories = storiesMap.get(profile.id);

        int storySize = 0;
        boolean isViewed = true;
        if (myStories != null && myStories.size() >= 1) {
            storySize = myStories.size();
            isViewed = Objects.equals(profile.viewedStories.get(profile.id), myStories.get(myStories.size() - 1).id);
        }

        StoryBubbleAdapter.StoryBubbleModel myStoriesBubble = new StoryBubbleAdapter.StoryBubbleModel(
                profile.id,
                profile.profilePicture,
                "Your story",
                storySize,
                isViewed
        );

        if (storyBubbles.size() == 0) {
            storyBubbles.add(myStoriesBubble);
        } else {
            storyBubbles.set(0, myStoriesBubble);
        }

        // TODO: fetch friends' stories

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