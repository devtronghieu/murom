package com.example.murom;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.murom.Recycler.PostImageAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LinearLayoutManager imagesLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);

    // Initialize edit buttons containers
    public ConstraintLayout flipContainer;
    public ConstraintLayout cropContainer;
    public ConstraintLayout rotateContainer;
    public ConstraintLayout addContainer;


    // Initialize edit buttons
    public ImageButton flipButton;
    public ImageButton cropButton;
    public ImageButton rotateButton;
    public ImageButton addButton;

    // Initialize edit options
    public ConstraintLayout flipOptions;
    public ConstraintLayout cropOptions;
    public ConstraintLayout rotateOptions;
    public ConstraintLayout addOptions;
    public ImageButton closeButton;

    int currentImageIndex = 0;

    public PostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostFragment newInstance(String param1, String param2) {
        PostFragment fragment = new PostFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        Random rand = new Random();

        RecyclerView postImagesRecycler = rootView.findViewById(R.id.post_images);
        postImagesRecycler.setLayoutManager(imagesLayoutManager);
        ArrayList<PostImageAdapter.PostImageModel> postImages = new ArrayList<>();

        for (int i = 0; i < rand.nextInt(5) + 5; i++) {
            ArrayList<String> images = new ArrayList<>();
            images.add("https://picsum.photos/200");

            postImages.add(new PostImageAdapter.PostImageModel("https://picsum.photos/200"));
        }

        PostImageAdapter postImageAdapter = new PostImageAdapter(postImages);
        postImagesRecycler.setAdapter(postImageAdapter);

        TextInputEditText captionInput = rootView.findViewById(R.id.caption_input);

        // Set function for slide buttons
        ImageButton slideBack = rootView.findViewById(R.id.slide_left);
        ImageButton slideNext = rootView.findViewById(R.id.slide_right);

        slideBack.setOnClickListener(v -> {
            if (currentImageIndex < postImages.size() - 1) {
                currentImageIndex--;
            } else {
                currentImageIndex = 0;
            }

            imagesLayoutManager.smoothScrollToPosition(postImagesRecycler, null,  currentImageIndex);
        });
        slideNext.setOnClickListener(v -> {
            if (currentImageIndex < postImages.size() - 1) {
                currentImageIndex++;
            } else {
                currentImageIndex = 0;
            }

            imagesLayoutManager.smoothScrollToPosition(postImagesRecycler, null,  currentImageIndex);
        });

        // Set container for edit buttons
        flipContainer = rootView.findViewById(R.id.flip_button);
        cropContainer = rootView.findViewById(R.id.crop_button);
        rotateContainer = rootView.findViewById(R.id.rotate_button);
        addContainer = rootView.findViewById(R.id.add_button);

        // Set layout for edit options
        flipOptions = rootView.findViewById(R.id.flip_options);
        cropOptions = rootView.findViewById(R.id.crop_options);
        rotateOptions = rootView.findViewById(R.id.rotate_options);
        closeButton = rootView.findViewById(R.id.close_edit_options_button);

        // Set function for edit buttons
        flipButton = rootView.findViewById(R.id.flip_icon);
        cropButton = rootView.findViewById(R.id.crop_icon);
        rotateButton = rootView.findViewById(R.id.rotate_icon);
        addButton = rootView.findViewById(R.id.add_icon);

        flipButton.setOnClickListener(v -> {
            flipOptions.setVisibility(View.VISIBLE);
            cropOptions.setVisibility(View.GONE);
            rotateOptions.setVisibility(View.GONE);
            closeButton.setVisibility(View.VISIBLE);

            flipContainer.setBackgroundColor(getResources().getColor(R.color.primary_100, null));
        });

        cropButton.setOnClickListener(v -> {
            flipOptions.setVisibility(View.GONE);
            cropOptions.setVisibility(View.VISIBLE);
            rotateOptions.setVisibility(View.GONE);
            closeButton.setVisibility(View.VISIBLE);

            cropContainer.setBackgroundColor(getResources().getColor(R.color.primary_100, null));
        });

        rotateButton.setOnClickListener(v -> {
            flipOptions.setVisibility(View.GONE);
            cropOptions.setVisibility(View.GONE);
            rotateOptions.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);

            rotateContainer.setBackgroundColor(getResources().getColor(R.color.primary_100, null));
        });

        closeButton.setOnClickListener(v -> {
            flipOptions.setVisibility(View.GONE);
            cropOptions.setVisibility(View.GONE);
            rotateOptions.setVisibility(View.GONE);
            closeButton.setVisibility(View.GONE);

        });


        captionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String caption = String.valueOf(captionInput.getText());
                Log.d("-->", "beforeTextChanged: " + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String caption = String.valueOf(captionInput.getText());
                Log.d("-->", "onTextChanged: " + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String caption = String.valueOf(captionInput.getText());
                Log.d("-->", "afterTextChanged: " + s);
            }
        });


        return rootView;    }
}