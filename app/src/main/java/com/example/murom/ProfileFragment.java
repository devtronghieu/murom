package com.example.murom;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.Recycler.ArchiveStoryAdapter;
import com.example.murom.Recycler.HighlightBubbleAdapter;
import com.example.murom.Recycler.PostsProfileAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.State.CurrentSelectedStoriesState;
import com.example.murom.State.HighlightState;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.example.murom.State.StoryState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

import io.reactivex.rxjava3.disposables.Disposable;


public class ProfileFragment extends Fragment {
    Disposable profileDisposable;
    Schema.User profile;
    ProfileState profileState = ProfileState.getInstance();
    RecyclerView postsRecycler, highlightsRecycler;
    ImageView pickedImageView, avatar, picture;
    BottomSheetDialog bottomSheet;
    HighlightBubbleAdapter highlightBubbleAdapter;
    TextView post, post_num, follower, follower_num, following, following_num, username, bio, photo;
    ImageButton burgerBtn;
    Button editBtn;
    Disposable restStoriesDisposable, allStoriesDisposable, selectedStoriesDisposable, highlighgtsDisposable;
    String currentHighlightId = "";
    String currentHighlightCoverUrl;
    ActivityResultLauncher<PickVisualMediaRequest> launcher;
    Disposable myPostsDisposable;

    public interface ProfileFragmentCallback {
        void onEditProfile();
        void onArchiveClick();
    }

    ProfileFragmentCallback callback;
    public ProfileFragment(ProfileFragmentCallback callback) {
        this.callback = callback;
        profileDisposable = ProfileState.getInstance().getObservableProfile().subscribe(profile -> {
            this.profile = profile;
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launcher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri == null) {
                            Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        currentHighlightCoverUrl = uri.toString();
                        Glide.with(requireContext()).load(uri).into(pickedImageView);
                        String highlightPath = "highlight/" + currentHighlightId;
                        Storage.uploadAsset(uri, highlightPath);

                        Storage.getRef(highlightPath).putFile(uri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    StorageReference storyRef = Storage.getRef(highlightPath);
                                    storyRef.getDownloadUrl().addOnSuccessListener(highlightURI -> {
                                        currentHighlightCoverUrl = highlightURI.toString();
                                    });
                                });

                    }
                });
    }

    @Override
    public void onDestroyView()
    {
        if (!profileDisposable.isDisposed()) {
            profileDisposable.dispose();
        }
        if (!myPostsDisposable.isDisposed()) {
            myPostsDisposable.dispose();
        }
        if (restStoriesDisposable != null && !restStoriesDisposable.isDisposed()) {
            restStoriesDisposable.dispose();
        }
        if (selectedStoriesDisposable != null && !selectedStoriesDisposable.isDisposed()) {
            selectedStoriesDisposable.dispose();
        }
        if (allStoriesDisposable != null && !allStoriesDisposable.isDisposed()) {
            allStoriesDisposable.dispose();
        }
        if (!highlighgtsDisposable.isDisposed()) {
            highlighgtsDisposable.dispose();
        }

        super.onDestroyView();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        avatar = rootView.findViewById(R.id.profile_avatar);
        post = rootView.findViewById(R.id.profile_post);
        post_num = rootView.findViewById(R.id.num_post);
        follower = rootView.findViewById(R.id.profile_follower);
        follower_num = rootView.findViewById(R.id.num_follower);
        following = rootView.findViewById(R.id.profile_following);
        following_num = rootView.findViewById(R.id.num_following);
        burgerBtn = rootView.findViewById(R.id.burger_button);

        username = rootView.findViewById(R.id.profile_username);
        bio = rootView.findViewById(R.id.profile_bio);
        editBtn = rootView.findViewById(R.id.profile_edit_btn);

        picture = rootView.findViewById(R.id.profile_imageView);
        photo = rootView.findViewById(R.id.profile_phototext);
        highlightsRecycler = rootView.findViewById(R.id.highlights_recycler);
        bottomSheet = new BottomSheetDialog(this.getContext());

        editBtn.setOnClickListener(v -> callback.onEditProfile());
        burgerBtn.setOnClickListener(v -> callback.onArchiveClick());

        StorageReference avatarRef = Storage.getRef("avatar/" + Auth.getUser().getEmail());
        avatarRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Glide.with(avatar.getContext())
                            .load(imageUrl)
                            .centerCrop()
                            .into(avatar);
                })
                .addOnFailureListener(e -> {
                    Log.d("-->", "failed to get avatar: " + e);
                });

        username.setText(profileState.profile.username);
        bio.setText(profileState.profile.bio);
        post_num.setText(String.valueOf(PostState.getInstance().myPosts.size()));
        Database.countFollower(Auth.getUser().getUid(), new Database.CountFollowerCallback() {
            @Override
            public void onCountFollowerSuccess(int count) {
                follower_num.setText(String.valueOf(count));
            }

            @Override
            public void onCountFollowerFailure(String errorMessage) {

            }
        });
        Database.countFollowing(Auth.getUser().getUid(), new Database.CountFollowingCallback() {
            @Override
            public void onCountFollowingSuccess(int count) {
                following_num.setText(String.valueOf(count));
            }

            @Override
            public void onCountFollowingFailure(String errorMessage) {

            }
        });

        highlightsRecycler.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL, false));
        highlightsRecycler.addItemDecoration(new SpacingItemDecoration(40, 0 ));

        Database.getHighlightsByUID(profile.id, new Database.GetHighlightsCallback() {
            @Override
            public void handleGetSuccessfully(ArrayList<Schema.HighlightStory> highlights) {
                HighlightState.getInstance().updateObservableHighlights(highlights);
            }

            @Override
            public void handleGetFail() {

            }
        });

        highlighgtsDisposable = HighlightState.getInstance().getObservableHighlights().subscribe(highlightStories -> {
            handleRenderHighlightBubbles(highlightStories);
        });
        // My Posts
        postsRecycler = rootView.findViewById(R.id.profile_posts_recycler);
        postsRecycler.setLayoutManager(new GridLayoutManager(requireContext(),3));
        myPostsDisposable = PostState.getInstance().getObservableMyPosts().subscribe(this::renderMyPosts);

        // Fetch archive stories
        Database.getStoriesByUID(profile.id, new Database.GetStoriesByUIDCallback() {
            @Override
            public void onGetStoriesSuccess(ArrayList<Schema.Story> stories) {
                StoryState.getInstance().updateObservableStoriesMap(stories);
            }

            @Override
            public void onGetStoriesFailure() {
            }
        });

        return rootView;
    }

    void renderMyPosts(ArrayList<Schema.Post> myPosts) {
        ArrayList<PostsProfileAdapter.PostsProfileModel> postsProfileModel = new ArrayList<>();

        myPosts.forEach(post -> {
            if (post.isArchived) return;
            postsProfileModel.add(new PostsProfileAdapter.PostsProfileModel(post.url));
        });

        PostsProfileAdapter postsProfileAdapter = new PostsProfileAdapter(postsProfileModel);
        postsRecycler.setAdapter(postsProfileAdapter);
    }

    private void createBottomSheet(String highlightId, String url, String name, ArrayList<String> storiesID) {
        View view = getLayoutInflater().inflate(R.layout.component_highlight_bottom_sheet, null, false);
        EditText editName = view.findViewById(R.id.edit_highlight_name);
        ImageView cover = view.findViewById(R.id.highlight_photo);
        TextView title = view.findViewById(R.id.bottom_sheet_title);

        RecyclerView restStoriesRecycler = view.findViewById(R.id.rest_stories);
        RecyclerView selectedStoriesRecycler = view.findViewById(R.id.selected_stories);
        Button selectedBtn = view.findViewById(R.id.selected_stories_btn);
        Button allBtn = view.findViewById(R.id.all_stories_btn);
        Button editCoverBtn = view.findViewById(R.id.edit_cover_btn);
        Button saveHighlightBtn = view.findViewById(R.id.save_highlight_btn);

        editName.setText(name);
        Glide.with(this).load(url).into(cover);
        title.setText("Add new highlight");

        if (highlightId != "") {
            title.setText("Edit your highlight");
        }
        if (url != "") {
            currentHighlightCoverUrl = url;
        }

        currentHighlightId = highlightId;
        editCoverBtn.setOnClickListener(v -> {
            if (currentHighlightId == "") {
                currentHighlightId = UUID.randomUUID().toString();
            }

            setPickedImageView(cover);
        });

        saveHighlightBtn.setOnClickListener(v -> {
            String finalName = editName.getText().toString();
            ArrayList<String> finalStories = new ArrayList<>();
            CurrentSelectedStoriesState.getInstance().stories.forEach(story -> {
                finalStories.add(story.id);
            });

            saveChanges(currentHighlightId, finalName, currentHighlightCoverUrl, finalStories);

            currentHighlightCoverUrl = "";
            currentHighlightId = "";
            destroyBottomSheet();
        });

        allStoriesDisposable = StoryState.getInstance().getObservableStoriesMap().subscribe(stories -> {
            handleRenderAllObservableStories(restStoriesRecycler);
        });
        restStoriesDisposable = CurrentSelectedStoriesState.getInstance().getObservableStoriesMap().subscribe(stories -> {
            handleRenderAllObservableStories(restStoriesRecycler);
        });
        selectedStoriesDisposable = CurrentSelectedStoriesState.getInstance().getObservableStoriesMap().subscribe(stories -> {
            handleRenderSelectedObservableStories(stories, selectedStoriesRecycler);
        });

        Database.getStoriesByStoriesID(storiesID, new Database.GetStoriesByUIDCallback() {
            @Override
            public void onGetStoriesSuccess(ArrayList<Schema.Story> stories) {
                CurrentSelectedStoriesState.getInstance().updateObservableStoriesMap(stories);
            }

            @Override
            public void onGetStoriesFailure() {}
        });

        restStoriesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        selectedStoriesRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        selectedBtn.setOnClickListener(v -> {
            Resources resources = getResources();
            selectedBtn.setBackground(resources.getDrawable(R.drawable.murom_ic_underline_btn, null));
            allBtn.setBackground(resources.getDrawable(R.color.transparent, null));
            selectedStoriesRecycler.setVisibility(View.VISIBLE);
            restStoriesRecycler.setVisibility(View.GONE);
        });
        allBtn.setOnClickListener(v -> {
            Resources resources = getResources();
            selectedBtn.setBackground(resources.getDrawable(R.color.transparent, null));
            allBtn.setBackground(resources.getDrawable(R.drawable.murom_ic_underline_btn, null));
            selectedStoriesRecycler.setVisibility(View.GONE);
            restStoriesRecycler.setVisibility(View.VISIBLE);
        });

        bottomSheet.setContentView(view);
    }

    private void destroyBottomSheet() {
        bottomSheet.cancel();
        if (!restStoriesDisposable.isDisposed()) {
            restStoriesDisposable.dispose();
        }
        if (!selectedStoriesDisposable.isDisposed()) {
            selectedStoriesDisposable.dispose();
        }
        if (!allStoriesDisposable.isDisposed()) {
            allStoriesDisposable.dispose();
        }
        if (!highlighgtsDisposable.isDisposed()) {
            highlighgtsDisposable.dispose();
        }
    }

    void handleRenderHighlightBubbles(ArrayList<Schema.HighlightStory> highlightStories) {
        ArrayList<HighlightBubbleAdapter.HighlightBubbleModel> highlights = new ArrayList<>();
        highlights.add(new HighlightBubbleAdapter.HighlightBubbleModel("", "", "New", new ArrayList<>()));

        highlightStories.forEach(highlight -> {
            highlights.add(new HighlightBubbleAdapter.HighlightBubbleModel(highlight.id, highlight.coverUrl, highlight.name, highlight.storiesID));
            Log.d("--> highlight model", "onCreateView: " + highlight.id);
        });

        highlightBubbleAdapter = new HighlightBubbleAdapter(highlights, new HighlightBubbleAdapter.HighlightBubbleCallback() {
            @Override
            public void handleDeleteHighlight(String highlightId) {
                Database.deleteHighlight(highlightId);

                ArrayList<Schema.HighlightStory> highlights = HighlightState.getInstance().highlights;
                for (int i = 0; i < highlights.size(); i++) {
                    if (highlights.get(i).id == highlightId) {
                        highlights.remove(i);
                        HighlightState.getInstance().updateObservableHighlights(highlights);

                        return;
                    }
                }
            }

            @Override
            public void handleEditHighlight(String highlightId, String url, String name, ArrayList<String> stories) {
                createBottomSheet(highlightId, url, name, stories);
                bottomSheet.show();
                bottomSheet.setOnCancelListener(v -> {
                    destroyBottomSheet();
                });
            }

            @Override
            public void handleAddHighlight() {
                createBottomSheet("", "", "", new ArrayList<>());
                bottomSheet.show();
                bottomSheet.setOnCancelListener(v -> {
                    destroyBottomSheet();
                });
            }

            @Override
            public void handleViewHighlight(String uid) {
                // view highlight
            }
        });
        highlightsRecycler.setAdapter(highlightBubbleAdapter);
    }

    void handleRenderAllObservableStories(RecyclerView recyclerView) {
        ArrayList<Schema.Story> highlightStories = CurrentSelectedStoriesState.getInstance().stories;
        ArrayList<Schema.Story> stories = StoryState.getInstance().stories;

        ArrayList<ArchiveStoryAdapter.ArchiveStoryModel> storyModel = new ArrayList<ArchiveStoryAdapter.ArchiveStoryModel>();

        for (int i = 0; i < stories.size(); i++) {
            boolean isChecked = false;
            for (int j = 0; j < highlightStories.size(); j++) {
                if (highlightStories.get(j).id == stories.get(i).id) {
                    isChecked = true;
                }
            }

            ArchiveStoryAdapter.ArchiveStoryModel storyData = new ArchiveStoryAdapter.ArchiveStoryModel(
                    stories.get(i).id,
                    stories.get(i).url,
                    true,
                    isChecked
            );

            Log.d("--> all", "handleRenderAllObservableStories: " + stories.get(i).id);
            storyModel.add(storyData);
        }

        ArchiveStoryAdapter storyAdapter = new ArchiveStoryAdapter(storyModel, new ArchiveStoryAdapter.ArchiveStoryCallback() {
            @Override
            public void handleSelectStory(String id) {
                ArrayList<Schema.Story> highlightStories = CurrentSelectedStoriesState.getInstance().stories;
                Schema.Story newStory = Database.getStoryByID(id);

                highlightStories.add(newStory);
                CurrentSelectedStoriesState.getInstance().updateObservableStoriesMap(highlightStories);

                for (int i = 0; i < highlightStories.size(); i++) {
                    Log.d("--> add", "handleSelectStory: " + highlightStories.get(i));
                }
            }

            @Override
            public void handleUnselectStory(String id) {
                ArrayList<Schema.Story> highlightStories = CurrentSelectedStoriesState.getInstance().stories;

                for (int i = 0; i < highlightStories.size(); i++) {
                    if (highlightStories.get(i).id == id) {
                        if (highlightStories.size() == 1) {
                            highlightStories = new ArrayList<>();
                        } else {
                            highlightStories.remove(i);
                        }
                    }
                }

                Log.d("--> unselect", "handleUnselectStory: " + highlightStories.size());
                CurrentSelectedStoriesState.getInstance().updateObservableStoriesMap(highlightStories);
                Log.d("--> unselect", "handleUnselectStory: 5");
            }
        });
        recyclerView.setAdapter(storyAdapter);
    }

    void handleRenderSelectedObservableStories(ArrayList<Schema.Story> stories, RecyclerView recyclerView) {
        ArrayList<ArchiveStoryAdapter.ArchiveStoryModel> storyModel = new ArrayList<>();

        for (int i = 0; i < stories.size(); i++) {
            ArchiveStoryAdapter.ArchiveStoryModel storyData = new ArchiveStoryAdapter.ArchiveStoryModel(
                    stories.get(i).id,
                    stories.get(i).url,
                    true,
                    true
            );
            storyModel.add(storyData);
        }

        ArchiveStoryAdapter storyAdapter = new ArchiveStoryAdapter(storyModel, new ArchiveStoryAdapter.ArchiveStoryCallback() {
            @Override
            public void handleSelectStory(String id) {
            }

            @Override
            public void handleUnselectStory(String id) {
                ArrayList<Schema.Story> highlightStories = CurrentSelectedStoriesState.getInstance().stories;

                for (int i = 0; i < highlightStories.size(); i++) {
                    if (highlightStories.get(i).id == id) {
                        if (highlightStories.size() == 1) {
                            highlightStories = new ArrayList<>();
                        } else {
                            highlightStories.remove(i);
                        }
                    }
                }

                Log.d("--> unselect", "handleUnselectStory: " + highlightStories.size());
                CurrentSelectedStoriesState.getInstance().updateObservableStoriesMap(highlightStories);
                Log.d("--> unselect", "handleUnselectStory: 5");

            }
        });
        recyclerView.setAdapter(storyAdapter);
    }

    void setPickedImageView(ImageView view) {
        pickedImageView = view;
        launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()
        );
    }

    void saveChanges(String highlightId, String name, String coverUrl, ArrayList<String> stories) {
        Schema.HighlightStory newHighlight =
                new Schema.HighlightStory(highlightId, profile.id, name, coverUrl, stories, Timestamp.now());

        ArrayList<Schema.HighlightStory> highlightStories = HighlightState.getInstance().highlights;
        highlightStories.add(0, newHighlight);
        HighlightState.getInstance().updateObservableHighlights(highlightStories);

        Database.addHighlight(newHighlight);
    }

}