package com.example.murom;

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
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.Recycler.HighlightBottomSheetAdapter;
import com.example.murom.Recycler.HighlightBubbleAdapter;
import com.example.murom.Recycler.PostsProfileAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
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
    HighlightBottomSheetAdapter highlightBottomSheetAdapter;
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
        HighlightBottomSheetAdapter.HighlightBottomSheetModel newHighlight =
                new HighlightBottomSheetAdapter.HighlightBottomSheetModel("", "", "");
        highlightBottomSheetAdapter = new HighlightBottomSheetAdapter(newHighlight);

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
                HighlightBottomSheetAdapter.HighlightBottomSheetModel newHighlight =
                        new HighlightBottomSheetAdapter.HighlightBottomSheetModel("", "", "");
                highlightBottomSheetAdapter = new HighlightBottomSheetAdapter(newHighlight);
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
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet, null, false);
        RecyclerView recyclerView = view.findViewById(R.id.bottom_sheet_content);

        recyclerView.setAdapter(highlightBottomSheetAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bottomSheet.setContentView(view);
    }
}