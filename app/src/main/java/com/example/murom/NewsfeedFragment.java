package com.example.murom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.murom.Recycler.NewsfeedAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.Recycler.StoryBubbleAdapter;

import java.util.ArrayList;
import java.util.Random;

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

    public NewsfeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsfeedFragment.
     */
    // TODO: Rename and change types and number of parameters
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


        RecyclerView storiesRecycler = rootView.findViewById(R.id.stories_recycler);
        storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        storiesRecycler.addItemDecoration(new SpacingItemDecoration(40, 0));
        ArrayList<StoryBubbleAdapter.StoryBubbleModel> stories = new ArrayList<>();
        for (int i = 0; i < rand.nextInt(5) + 3; i++) {
            stories.add(new StoryBubbleAdapter.StoryBubbleModel("https://picsum.photos/200", "username" + i, rand.nextBoolean()));
        }
        StoryBubbleAdapter storyBubbleAdapter = new StoryBubbleAdapter(stories);
        storiesRecycler.setAdapter(storyBubbleAdapter);

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