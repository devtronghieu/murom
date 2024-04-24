package com.example.murom;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.Recycler.CommentAdapter;
import com.example.murom.Recycler.PostAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.Recycler.StoryBubbleAdapter;
import com.example.murom.State.ActiveStoryState;
import com.example.murom.State.CommentState;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.rxjava3.disposables.Disposable;

public class NewsfeedFragment extends Fragment {
    Activity activity;
    ActivityResultLauncher<PickVisualMediaRequest> launcher;

    // AppState
    Disposable storiesMapDisposable, socialPostsDisposable;

    int offset = 0;
    int limit = 20;

    // Components
    RecyclerView storiesRecycler, postRecycler;
    SwipeRefreshLayout swipeRefreshLayout;
    BottomSheetDialog commentBottomSheet;
    ImageButton notificationIcon;
    NotificationFragment notificationDialog;

    public interface  NewsfeedFragmentCallback {
        void onViewStory(String uid);
        void onViewProfile(String uid);
    }

    NewsfeedFragmentCallback callback;

    public NewsfeedFragment(NewsfeedFragmentCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launcher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri == null) {
                        Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Timestamp createdAt = new Timestamp(new Date());

                    String uid = ProfileState.getInstance().profile.id;

                    String storagePath = "story/" + uid + "/" + createdAt;

                    String mimeType = activity.getContentResolver().getType(uri);

                    boolean isImage = mimeType != null && mimeType.startsWith("image/");

                    Storage
                            .getRef(storagePath).putFile(uri)
                            .addOnSuccessListener(taskSnapshot -> {
                                StorageReference storyRef = Storage.getRef(storagePath);
                                storyRef
                                        .getDownloadUrl()
                                        .addOnSuccessListener(storyURI -> {
                                            Schema.Story story = new Schema.Story(
                                                    UUID.randomUUID().toString(),
                                                    createdAt,
                                                    uid,
                                                    storyURI.toString(),
                                                    isImage ? "image" : "video"
                                            );
                                            Database.addStory(story);

                                            ActiveStoryState activeStoryState = ActiveStoryState.getInstance();
                                            HashMap<String, ArrayList<Schema.Story>> newStoriesMap = activeStoryState.activeStoriesMap;
                                            ArrayList<Schema.Story> myStories = activeStoryState.activeStoriesMap.get(uid);
                                            if (myStories == null) {
                                                myStories = new ArrayList<>();
                                            }
                                            myStories.add(story);
                                            activeStoryState.activeStoriesMap.put(uid, myStories);
                                            activeStoryState.updateObservableActiveStoriesMap(newStoriesMap);

                                            Toast.makeText(requireContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d("-->", "failed to get story: " + e);
                                            Toast.makeText(requireContext(), "Failed to upload story!", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.d("-->", "failed to get story: " + e);
                                Toast.makeText(requireContext(), "Failed to upload story!", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        activity = getActivity();

        commentBottomSheet = new BottomSheetDialog(this.getContext());

        // Stories
        storiesRecycler = rootView.findViewById(R.id.stories_recycler);
        storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        storiesRecycler.addItemDecoration(new SpacingItemDecoration(40, 0));

        // Social Posts
        postRecycler = rootView.findViewById(R.id.post_recycler);
        postRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        postRecycler.addItemDecoration(new SpacingItemDecoration(0, 45));

        // Swipe to refresh the posts
        swipeRefreshLayout = rootView.findViewById(R.id.post_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            PostState.getInstance().constructObservableSocialPosts(offset, limit);
            swipeRefreshLayout.setRefreshing(false);
        });

        ProfileState profileState = ProfileState.getInstance();

        // Stories state
        storiesMapDisposable = ActiveStoryState.getInstance().getObservableActiveStoriesMap().subscribe(this::handleWatchStoriesToRender);
        Database.getActiveStories(profileState.socialIDs, new Database.GetActiveStoriesCallback() {
            @Override
            public void onGetStoriesSuccess(HashMap<String, ArrayList<Schema.Story>> storyMap) {
                ActiveStoryState.getInstance().updateObservableActiveStoriesMap(storyMap);
            }

            @Override
            public void onGetStoriesFailure() {
                Toast.makeText(requireContext(), "Failed to load stories", Toast.LENGTH_SHORT).show();
            }
        });

        // Social Posts state
        PostState postState = PostState.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d'th', yyyy", Locale.ENGLISH);
        socialPostsDisposable = postState.getObservableSocialPosts().subscribe(posts -> {
            ArrayList<PostAdapter.PostModel> postModels = new ArrayList<>();
            posts.forEach(post -> {
                Schema.User postOwnerProfile = profileState.followerProfileMap.get(post.userId);

                if (postOwnerProfile != null) {
                    ArrayList<String> images = new ArrayList<>();
                    images.add(post.url);
                    postModels.add(new PostAdapter.PostModel(
                            post.userId,
                            post.id,
                            postOwnerProfile.profilePicture,
                            postOwnerProfile.username,
                            images,
                            post.caption,
                            dateFormat.format(post.createdAt.toDate()),
                            post.lovedByUIDs
                    ));
                }
            });
            this.setNewsfeeds(postModels);
        });

        if (postState.socialPosts.size() == 0) {
            postState.constructObservableSocialPosts(offset, limit);
        }

        notificationIcon = rootView.findViewById(R.id.notification_icon);
        notificationDialog = new NotificationFragment();
        notificationIcon.setOnClickListener(v -> showNotificationDialog());

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
        int myStoriesSize = myStories != null ? myStories.size() : 0;

        StoryBubbleAdapter.StoryBubbleModel myStoryBubble = new StoryBubbleAdapter.StoryBubbleModel(
                profile.id,
                profile.profilePicture,
                "Your story",
                myStoriesSize,
                myStoriesSize > 0 && Objects.equals(profile.viewedStories.get(profile.id), myStories.get(myStories.size() - 1).id)
        );
        storyBubbles.add(myStoryBubble);

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
                    stories.get(stories.size() - 1).url,
                    ownerProfile.username,
                    stories.size(),
                    Objects.equals(profile.viewedStories.get(ownerProfile.id), stories.get(stories.size() - 1).id)
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
        PostAdapter postAdapter = new PostAdapter(newsfeeds, new PostAdapter.PostModelCallback() {
            @Override
            public void showCommentBottomSheet(String postID) {
                NewsfeedFragment.this.showCommentBottomSheet(postID);
            }

            @Override
            public void showProfile(String uid) {
                callback.onViewProfile(uid);
            }
        });
        postRecycler.setAdapter(postAdapter);
    }

    void showCommentBottomSheet(String postID) {
        View view = getLayoutInflater().inflate(R.layout.component_comment_bottom_sheet, null, false);

        RecyclerView commentRecyclerView = view.findViewById(R.id.comment_recycler);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        commentRecyclerView.addItemDecoration(new SpacingItemDecoration(0, 20));

        CommentState commentState = CommentState.getInstance();

        Database.getCommentsByPostID(postID, new Database.GetCommentsByPostIDCallback() {
            @Override
            public void onGetCommentsSuccess(ArrayList<Schema.Comment> comments) {
                HashMap<String, ArrayList<Schema.Comment>> commentsMap = commentState.commentsMap;
                commentsMap.put(postID, comments);
                commentState.updateObservableCommentsMap(commentsMap);
            }

            @Override
            public void onGetCommentsFailure() {
                Toast.makeText(activity, "Failed to get comments", Toast.LENGTH_SHORT).show();
            }
        });

        TextInputEditText inputEditText = view.findViewById(R.id.comment_input);
        ImageButton sendBtn = view.findViewById(R.id.comment_send_btn);
        sendBtn.setOnClickListener(v -> {
            String content = inputEditText.getText() != null ? inputEditText.getText().toString() : "";
            if (content.length() > 0) {
                Database.createComment(postID, content, new Database.CreateCommentCallback() {
                    @Override
                    public void onCreateCommentSuccess(String commentID) {
                        Schema.Comment comment = new Schema.Comment(
                                commentID,
                                postID,
                                ProfileState.getInstance().profile.id,
                                content,
                                new ArrayList<>(),
                                Timestamp.now()
                        );
                        HashMap<String, ArrayList<Schema.Comment>> commentsMap = commentState.commentsMap;
                        ArrayList<Schema.Comment> comments = commentsMap.get(postID);
                        if (comments == null) {
                            comments = new ArrayList<>();
                        }
                        comments.add(0, comment);
                        commentState.updateObservableCommentsMap(commentsMap);

                        inputEditText.setText("");
                    }

                    @Override
                    public void onCreateCommentFailure() {
                        Toast.makeText(activity, "Failed to comment", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Disposable commentsDisposable = commentState.getObservableCommentsMap().subscribe(commentsMap -> {
            ArrayList<Schema.Comment> comments = commentsMap.get(postID);
            if (comments == null) return;

            ArrayList<CommentAdapter.CommentAdapterModel> commentAdapterModels = new ArrayList<>();
            for (int i = 0; i < comments.size(); i++) {
                Schema.Comment comment = comments.get(i);
                commentAdapterModels.add(new CommentAdapter.CommentAdapterModel(
                        new Schema.Comment(comment.id, comment.postID, comment.userID, comment.content, comment.lovedBy, comment.timestamp),
                        comment.lovedBy.contains(ProfileState.getInstance().profile.id)
                ));
            }
            CommentAdapter commentAdapter = new CommentAdapter(commentAdapterModels);
            commentRecyclerView.setAdapter(commentAdapter);
        });

        commentBottomSheet.setContentView(view);
        commentBottomSheet.setOnCancelListener(dialogInterface -> {
            if (!commentsDisposable.isDisposed()) {
                commentsDisposable.dispose();
            }
        });
        commentBottomSheet.show();
    }

    void showNotificationDialog() {
        notificationDialog.show(getChildFragmentManager(), "Notification Dialog");
    }

}