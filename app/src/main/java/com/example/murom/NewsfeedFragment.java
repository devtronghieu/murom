package com.example.murom;

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
import com.example.murom.Firebase.Storage;
import com.example.murom.Recycler.NewsfeedAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.Recycler.StoryBubbleAdapter;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsfeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsfeedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ActivityResultLauncher<PickVisualMediaRequest> launcher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                } else {
                    String storagePath = "story/" + Auth.getUser().getUid() + "/" + UUID.randomUUID().toString();
                    Storage.uploadAsset(uri, storagePath);
                    Toast.makeText(requireContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                }
            });

    public NewsfeedFragment() {
        // Required empty public constructor
    }

    public static NewsfeedFragment newInstance(String param1, String param2) {
        NewsfeedFragment fragment = new NewsfeedFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        Random rand = new Random();


        // Stories Recycler
        RecyclerView storiesRecycler = rootView.findViewById(R.id.stories_recycler);
        storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        storiesRecycler.addItemDecoration(new SpacingItemDecoration(40, 0));

        ArrayList<StoryBubbleAdapter.StoryBubbleModel> stories = new ArrayList<>();
        stories.add(new StoryBubbleAdapter.StoryBubbleModel("my_id", "", "Your story", false));

        for (int i = 0; i < rand.nextInt(5) + 3; i++) {
            stories.add(new StoryBubbleAdapter.StoryBubbleModel("id" + i, "https://picsum.photos/200", "username" + i, rand.nextBoolean()));
        }
        StoryBubbleAdapter storyBubbleAdapter = new StoryBubbleAdapter(stories, new StoryBubbleAdapter.StoryBubbleCallback() {
            @Override
            public void handleUploadStory() {
                launcher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                        .build());
            }

            @Override
            public void handleViewStories(String uid) {
                Log.d("-->", "View stories of " + uid);
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

        return rootView;
    }
}