package com.example.murom;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.example.murom.State.StoryState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.Disposable;


public class ProfileFragment extends Fragment {
    Disposable profileDisposable;
    Schema.User profile;
    ProfileState profileState = ProfileState.getInstance();
    RecyclerView postsRecycler;
    ImageView pickedImageView;
    BottomSheetDialog bottomSheet;
    HighlightBubbleAdapter highlightBubbleAdapter;
    RecyclerView highlightsRecycler;
    ImageView avatar;
    TextView post;
    TextView post_num;
    TextView follower;
    TextView follower_num;
    TextView following;
    TextView following_num;
    ImageButton burgerBtn;
    TextView username;
    TextView bio;
    Button editBtn;
    ImageView picture;
    TextView photo;
    Disposable restStoriesDisposable;
    Disposable allStoriesDisposable;
    Disposable selectedStoriesDisposable;

    ActivityResultLauncher<PickVisualMediaRequest> launcher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri == null) {
                        Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                    } else {
                        Glide.with(requireContext()).load(uri).into(pickedImageView);
                        Storage.uploadAsset(uri, "avatar/" + Auth.getUser().getEmail());
                    }
                }
            });

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
    }

    @Override
    public void onDestroyView()
    {
        if (!profileDisposable.isDisposed()) {
            profileDisposable.dispose();
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
                            .into(avatar);
                })
                .addOnFailureListener(e -> {
                    Log.d("-->", "failed to get avatar: " + e);
                });

        username.setText(profileState.profile.username);
        bio.setText(profileState.profile.bio);

        highlightsRecycler.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL, false));
        highlightsRecycler.addItemDecoration(new SpacingItemDecoration(40, 0 ));

        ArrayList<HighlightBubbleAdapter.HighlightBubbleModel> highlights = new ArrayList<>();

        highlights.add(new HighlightBubbleAdapter.HighlightBubbleModel("", "", "New"));

        highlightBubbleAdapter = new HighlightBubbleAdapter(highlights, new HighlightBubbleAdapter.HighlightBubbleCallback() {
            @Override
            public void handleDeleteHighlight(String highlightId) {
                // delete highlight
            }

            @Override
            public void handleEditHighlight(String highlightId) {
                // get highlight by Id -> create new highlight with the highlight model
            }

            @Override
            public void handleAddHighlight() {

                createBottomSheet();
                bottomSheet.show();
            }

            @Override
            public void handleViewHighlight(String uid) {
                // view highlight
            }
        });
        highlightsRecycler.setAdapter(highlightBubbleAdapter);


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

    private void createBottomSheet() {
        View view = getLayoutInflater().inflate(R.layout.component_highlight_bottom_sheet, null, false);
        RecyclerView restStoriesRecycler = view.findViewById(R.id.rest_stories);
        RecyclerView selectedStoriesRecycler = view.findViewById(R.id.selected_stories);
        Button selectedBtn = view.findViewById(R.id.selected_stories_btn);
        Button allBtn = view.findViewById(R.id.all_stories_btn);

        allStoriesDisposable = StoryState.getInstance().getObservableStoriesMap().subscribe(stories -> {
            handleRenderAllObservableStories(restStoriesRecycler);
        });

        restStoriesDisposable = CurrentSelectedStoriesState.getInstance().getObservableStoriesMap().subscribe(stories -> {
            handleRenderAllObservableStories(restStoriesRecycler);
        });

        selectedStoriesDisposable = CurrentSelectedStoriesState.getInstance().getObservableStoriesMap().subscribe(stories -> {
            handleRenderSelectedObservableStories(stories, selectedStoriesRecycler);
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
            }
        });
        recyclerView.setAdapter(storyAdapter);
    }

    void handleRenderSelectedObservableStories(ArrayList<Schema.Story> stories, RecyclerView recyclerView) {
        Log.d("-->", "re-render");
        ArrayList<ArchiveStoryAdapter.ArchiveStoryModel> storyModel = new ArrayList<>();

        for (int i = 0; i < stories.size(); i++) {
            ArchiveStoryAdapter.ArchiveStoryModel storyData = new ArchiveStoryAdapter.ArchiveStoryModel(
                    stories.get(i).id,
                    stories.get(i).url,
                    true,
                    true
            );
            storyModel.add(storyData);

            Log.d("--> selected", "handleRenderSelectedStory: " + stories.get(i).id);
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
}