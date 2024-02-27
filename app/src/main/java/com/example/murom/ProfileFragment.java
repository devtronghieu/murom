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
import com.example.murom.Firebase.Storage;
import com.example.murom.Recycler.HighlightAdapter;
import com.example.murom.Recycler.PostAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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
                        Storage.uploadImage(uri, "avatar/" + Auth.getUser().getEmail());
                    }
                }
            });

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Random rand = new Random();

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView avatar = rootView.findViewById(R.id.avatar);
        Button editBtn = rootView.findViewById(R.id.edit);
        TextView numberPost = rootView.findViewById(R.id.number_post);
        TextView post = rootView.findViewById(R.id.post);
        TextView numberFollower = rootView.findViewById(R.id.number_follower);
        TextView numberFollowing = rootView.findViewById(R.id.number_following);
        TextView follower = rootView.findViewById(R.id.follower);
        TextView following = rootView.findViewById(R.id.following);
        TextView username = rootView.findViewById(R.id.username);
        TextView bio = rootView.findViewById(R.id.bio);

        RecyclerView highlightsRecycler = rootView.findViewById(R.id.highlights_recycler);
        highlightsRecycler.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
        highlightsRecycler.addItemDecoration(new SpacingItemDecoration(40, 0));
        ArrayList<HighlightAdapter.HighlightBubbleModel> highlights = new ArrayList<>();
        ArrayList<String> url= new ArrayList<>();
        for (int i = 0 ; i < 5; i++)
        {
            url.add("https://picsum.photo/200") ;
        }
        for (int i = 0; i < rand.nextInt(5) +3; i++) {
            highlights.add(new HighlightAdapter.HighlightBubbleModel(url, "hehe"+ i, "https://picsum.photos/200"));
        }

        HighlightAdapter highlightAdapter = new HighlightAdapter(highlights);
        highlightsRecycler.setAdapter(highlightAdapter);

        RecyclerView postRecycler = rootView.findViewById(R.id.posts_recycler);

        postRecycler.addItemDecoration(new SpacingItemDecoration(10,10));
        ArrayList<PostAdapter.PostModel> posts = new ArrayList<>();

        for (int i = 0; i < rand.nextInt(5)+3; i++){
            posts.add(new PostAdapter.PostModel("https://picsum.photos/200"));
        }
        PostAdapter postAdapter = new PostAdapter(posts);
        postRecycler.setAdapter(postAdapter);
        postRecycler.setLayoutManager(new GridLayoutManager(getContext(),3));

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

        return rootView;
    }

    private void setupImagePicker(ImageView imageView) {
        this.pickedImageView = imageView;
        launcher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }

    private void handleSignOut(View view) {
        Auth.signOut();
        Intent i = new Intent(requireContext(), LoginActivity.class);
        startActivity(i);
    }
}