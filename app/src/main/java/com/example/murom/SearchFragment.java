package com.example.murom;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murom.Recycler.GridSpacingItemDecoration;
import com.example.murom.Recycler.PostAdapter;
import com.example.murom.Recycler.PostImageAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.State.AppState;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        Random rand = new Random();

        // Inflate the layout for this fragment

        RecyclerView postImageRecycler = rootView.findViewById(R.id.post_recycler);
        postImageRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        postImageRecycler.addItemDecoration(new GridSpacingItemDecoration(3, 5, true));
        ArrayList<PostImageAdapter.PostImageModel> search_result = new ArrayList<>();

        for (int i = 0; i < rand.nextInt(5) + 5; i++) {
            search_result.add(new PostImageAdapter.PostImageModel(
                    "https://picsum.photos/200"
            ));
        }
        PostImageAdapter postImageAdapter = new PostImageAdapter(search_result);
        postImageRecycler.setAdapter(postImageAdapter);
        return rootView;
    }
}