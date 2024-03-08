package com.example.murom;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.State.AppState;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.StorageReference;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PostFragment extends Fragment {

    Schema.User profile;
    Activity activity;
    String postID = UUID.randomUUID().toString();
    String caption;
    String type;
    Uri postUri;
    ImageView postImage;
    VideoView postVideo;

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
    public ImageButton closeButton;

    public TextView addImageText;
    public ImageButton uploadButton;

    ActivityResultLauncher<PickVisualMediaRequest> launcher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    Toast.makeText(requireContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                } else {
                    String mimeType = activity.getContentResolver().getType(uri);
                    type = mimeType != null && mimeType.startsWith("image/") ? "image" : "video";
                    Log.d("-->", "type: " + type);
                    postUri = uri;

                    if (Objects.equals(type, "image")) {
                        postImage.setVisibility(View.VISIBLE);
                        postVideo.setVisibility(View.GONE);
                        Glide.with(this).load(uri).into(postImage);
                    } else {
                        postImage.setVisibility(View.GONE);
                        postVideo.setVisibility(View.VISIBLE);
                        postVideo.setVideoPath(uri.toString());
                        postVideo.start();
                    }
                }
            });

    public PostFragment() {
        // Required empty public constructor
        this.profile = AppState.getInstance().profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        activity = getActivity();
        addImageText = rootView.findViewById(R.id.text_add_image);
        uploadButton = rootView.findViewById(R.id.upload_button);

        postImage = rootView.findViewById(R.id.post_images);
        postVideo = rootView.findViewById(R.id.post_video);

        TextInputEditText captionInput = rootView.findViewById(R.id.caption_input);

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
            setEditOptionsNone();
            flipOptions.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);

            setEditButtonActive(flipContainer);
        });

        cropButton.setOnClickListener(v -> {
            setEditOptionsNone();
            cropOptions.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);

            setEditButtonActive(cropContainer);
        });

        rotateButton.setOnClickListener(v -> {
            setEditOptionsNone();
            rotateOptions.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);

            setEditButtonActive(rotateContainer);
        });

        addButton.setOnClickListener(v -> {
            setEditOptionsNone();
            setEditButtonActive(addContainer);
            selectMediaResource();
        });

        closeButton.setOnClickListener(v -> {
            setEditOptionsNone();
        });

        uploadButton.setOnClickListener(v -> uploadPost());

        captionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String captionString = String.valueOf(captionInput.getText());
                caption = captionString;
            }
        });


        return rootView;
    }

    private void setEditButtonActive(ConstraintLayout button) {
        button.setBackgroundColor(getResources().getColor(R.color.primary_100, null));
    }

    private void setEditOptionsNone() {
        flipContainer.setBackgroundColor(getResources().getColor(R.color.white, null));
        cropContainer.setBackgroundColor(getResources().getColor(R.color.white, null));
        rotateContainer.setBackgroundColor(getResources().getColor(R.color.white, null));
        addContainer.setBackgroundColor(getResources().getColor(R.color.white, null));
        closeButton.setVisibility(View.GONE);

        flipOptions.setVisibility(View.GONE);
        cropOptions.setVisibility(View.GONE);
        rotateOptions.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
    }

    private void selectMediaResource() {
        launcher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE).build());
        addImageText.setVisibility(View.GONE);
        setEditOptionsNone();
    }

    private void uploadPost() {
        String createdAt = Instant.now().toString();
        String storagePath = "post/" + postID;

        Storage.getRef(storagePath).putFile(postUri)
                .addOnSuccessListener(taskSnapshot -> {
                    StorageReference postRef = Storage.getRef(storagePath);
                    postRef.getDownloadUrl()
                            .addOnSuccessListener(postURI -> {
                                Schema.Post post = new Schema.Post(postID, profile.id, postURI.toString(), type, caption, createdAt);
                                Database.addPost(post);
                                Toast.makeText(requireContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.d("-->", "failed to get post: " + e);
                                Toast.makeText(requireContext(), "Failed to upload post!", Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Log.d("-->", "failed to get post: " + e);
                    Toast.makeText(requireContext(), "Failed to upload post!", Toast.LENGTH_SHORT).show();
                });
    }

}