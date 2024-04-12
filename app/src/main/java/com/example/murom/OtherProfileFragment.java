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
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.Recycler.HighlightBubbleAdapter;
import com.example.murom.Recycler.PostsProfileAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

import io.reactivex.rxjava3.disposables.Disposable;


public class OtherProfileFragment extends Fragment {
    Disposable profileDisposable;
    Schema.User profile;
    RecyclerView postsRecycler;
    Disposable myPostsDisposable;
    ImageView avatar;
    TextView username;
    TextView bio;
    public interface OtherProfileFragmentCallback {

    }
    OtherProfileFragmentCallback callback;
    public OtherProfileFragment(OtherProfileFragmentCallback callback) {
        this.callback = callback;
        profileDisposable = ProfileState.getInstance().getObservableProfile().subscribe(profile -> {
            this.profile = profile;
        });
    }

    public static OtherProfileFragment newInstance(String userId) {
        OtherProfileFragment fragment = new OtherProfileFragment(null);
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userId = getArguments().getString("userId");
        fetchUserProfile(userId);
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
        Random rand = new Random();
        String userId = getArguments().getString("userId");

        View rootView = inflater.inflate(R.layout.fragment_other_profile, container, false);

        avatar = rootView.findViewById(R.id.profile_avatar);
        TextView post = rootView.findViewById(R.id.profile_post);
        TextView post_num = rootView.findViewById(R.id.num_post);
        TextView follower = rootView.findViewById(R.id.profile_follower);
        TextView follower_num = rootView.findViewById(R.id.num_follower);
        TextView following = rootView.findViewById(R.id.profile_following);
        TextView following_num = rootView.findViewById(R.id.num_following);

        username = rootView.findViewById(R.id.profile_username);
        bio = rootView.findViewById(R.id.profile_bio);
        Button followBtn = rootView.findViewById(R.id.profile_follow_btn);

        ImageView picture = rootView.findViewById(R.id.profile_imageView);
        TextView photo = rootView.findViewById(R.id.profile_phototext);


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
        ProfileState profileState = ProfileState.getInstance();
        username.setText(profileState.profile.username);
        bio.setText(profileState.profile.bio);

        RecyclerView highlightsRecycler = rootView.findViewById(R.id.highlights_recycler);
        highlightsRecycler.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL, false));
        highlightsRecycler.addItemDecoration(new SpacingItemDecoration(40, 0 ));

        ArrayList<HighlightBubbleAdapter.HighlightBubbleModel> highlights = new ArrayList<>();

        for (int i = 0 ; i < rand.nextInt(5)+5; i++){
            highlights.add(new HighlightBubbleAdapter.HighlightBubbleModel(userId,"https://picsum.photos/200", "hehe" + i));
        }

        HighlightBubbleAdapter highlightBubbleAdapter = new HighlightBubbleAdapter(highlights);
        /*HighlightBubbleAdapter highlightBubbleAdapter = new HighlightBubbleAdapter(highlights, new HighlightBubbleAdapter.HighlightBubbleCallback() {
            @Override
            public void handleUploadHighlight() {
                launcher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                        .build());
            }

            @Override
            public void handleViewHighlight(String uid) {
                callback.onViewHighlight(uid);
            }
        });*/
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
            postsProfileModel.add(new PostsProfileAdapter.PostsProfileModel(post.url));
        });

        PostsProfileAdapter postsProfileAdapter = new PostsProfileAdapter(postsProfileModel);
        postsRecycler.setAdapter(postsProfileAdapter);
    }

    private void fetchUserProfile(String userId) {

        Database.getUser(userId, new Database.GetUserCallback() {
            @Override
            public void onGetUserSuccess(Schema.User user) {
                updateProfileUI(user);
                ProfileState.getInstance().updateObservableProfile(user);
            }

            @Override
            public void onGetUserFailure() {
                Log.d("-->", "Failed to fetch user profile");
            }
        });
    }

    private void updateProfileUI(Schema.User user) {
        username.setText(user.username);
        bio.setText(user.bio);
        StorageReference avatarRef = Storage.getRef("avatar/" + user.email);
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
    }
}