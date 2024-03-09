package com.example.murom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class ProfileFragment extends Fragment {
    ImageView pickedImageView;
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

    public interface ProfileFragementCallback{
        void onEditProfile(String uid);
        void onViewHighlight(String uid);
    }

    Schema.User profile;
    HashMap<String, ArrayList<Schema.Story>> highlightsMap;
    ProfileFragementCallback callback;

    public ProfileFragment(Schema.User profile, HashMap<String, ArrayList<Schema.Story>> highlightsMap) {
        this.profile = profile;
        this.highlightsMap = highlightsMap;
    }





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Random rand = new Random();
        String uid = Auth.getUser().getUid();


        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        ImageView avatar = rootView.findViewById(R.id.avatar);
        TextView post = rootView.findViewById(R.id.post);
        TextView post_num = rootView.findViewById(R.id.num_post);
        TextView follower = rootView.findViewById(R.id.follower);
        TextView follower_num = rootView.findViewById(R.id.num_follower);
        TextView following = rootView.findViewById(R.id.following);
        TextView following_num = rootView.findViewById(R.id.num_following);
        TextView username = rootView.findViewById(R.id.username);
        TextView bio = rootView.findViewById(R.id.bio);
        Button editBtn = rootView.findViewById(R.id.edit_btn);
        ImageView picture = rootView.findViewById(R.id.imageView);
        TextView photo = rootView.findViewById(R.id.photo);




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

        RecyclerView highlightsRecycler = rootView.findViewById(R.id.highlights_recycler);
        highlightsRecycler.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL, false));
        highlightsRecycler.addItemDecoration(new SpacingItemDecoration(40, 0 ));

        ArrayList<HighlightBubbleAdapter.HighlightBubbleModel> highlights = new ArrayList<>();
        ArrayList<Schema.Story> myHighlights = highlightsMap.get(uid);

        for (int i = 0 ; i < rand.nextInt(5)+5; i++){
            highlights.add(new HighlightBubbleAdapter.HighlightBubbleModel(uid,"https://picsum.photos/200", "hehe" + i));
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


        RecyclerView postsRecycler = rootView.findViewById(R.id.posts_recycler);
        postsRecycler.setLayoutManager(new GridLayoutManager(getContext(),3));
        ArrayList<PostsProfileAdapter.PostsProfileModel> posts = new ArrayList<>();

        for (int i = 0; i < rand.nextInt(5)+ 5; i++) {
            posts.add(new PostsProfileAdapter.PostsProfileModel(
                    "https://picsum.photos/200"));
        }
        PostsProfileAdapter postsProfileAdapter = new PostsProfileAdapter(posts);
        postsRecycler.setAdapter(postsProfileAdapter);

        return rootView;
    }


}