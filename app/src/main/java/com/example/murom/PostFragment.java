package com.example.murom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canhub.cropper.CropImageView;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.example.murom.Utils.BitmapUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.rxjava3.disposables.Disposable;

public class PostFragment extends Fragment {
    // AppState
    Disposable profileDisposable;

    Schema.User profile;
    Activity activity;
    String postID = UUID.randomUUID().toString();
    String caption;
    String type;
    Uri postUri;

    ProgressBar loadingBar;

    CropImageView postImage;
    boolean isCropping;
    boolean isEdited;

    VideoView postVideo;

    Context context;

    public LinearLayoutManager imagesLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);

    Button addPostButton;


    // Initialize edit buttons containers
    ConstraintLayout imageEditToolsContainer;
    ConstraintLayout videoEditToolsContainer;

    public ConstraintLayout flipContainer;
    public ConstraintLayout cropContainer;
    public ConstraintLayout rotateContainer;
    public ConstraintLayout addContainer;

    // Initialize edit buttons
    public ImageButton flipButton;
    public ImageButton cropButton;
    public ImageButton flipHorizontallyButton;
    public ImageButton flipVerticallyButton;
    public ImageButton rotateButton;
    public ImageButton rotateLeftButton;
    public ImageButton rotateRightButton;
    public ImageButton imageToolsAddButton;
    ImageButton videoToolsAddButton;
    TextInputEditText captionInput;

    // Initialize edit options
    public ConstraintLayout flipOptions;
    public ConstraintLayout rotateOptions;
    public ImageButton closeButton;

    public ImageButton uploadButton;

    ActivityResultLauncher<PickVisualMediaRequest> launcher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    Toast.makeText(context, "No image/video selected!", Toast.LENGTH_SHORT).show();
                } else {
                    String mimeType = activity.getContentResolver().getType(uri);
                    type = mimeType != null && mimeType.startsWith("image/") ? "image" : "video";
                    postUri = uri;
                    isEdited = false;
                    uploadButton.setEnabled(true);
                    addPostButton.setVisibility(View.GONE);

                    if (Objects.equals(type, "image")) {
                        postImage.setImageUriAsync(uri);
                    } else {
                        postVideo.setVideoPath(uri.toString());
                        postVideo.start();
                    }

                    showComponentsByType(type);
                }
            });

    public PostFragment() {
        // Required empty public constructor
        profileDisposable = ProfileState.getInstance().getObservableProfile().subscribe(profile -> {
            this.profile = profile;
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (!profileDisposable.isDisposed()) {
            profileDisposable.dispose();
        }

        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);
        context = requireContext();

        activity = getActivity();

        loadingBar = rootView.findViewById(R.id.post_fragment_add_post_loading);

        addPostButton = rootView.findViewById(R.id.add_post_button);
        addPostButton.setOnClickListener(v -> { selectMediaResource(); });

        imageEditToolsContainer = rootView.findViewById(R.id.post_image_edit_tools_container);
        videoEditToolsContainer = rootView.findViewById(R.id.post_video_edit_tools_container);

        uploadButton = rootView.findViewById(R.id.upload_button);
        uploadButton.setEnabled(false);

        postImage = rootView.findViewById(R.id.post_images);
        postVideo = rootView.findViewById(R.id.post_video);

        captionInput = rootView.findViewById(R.id.caption_input);

        // Set container for edit buttons
        flipContainer = rootView.findViewById(R.id.flip_button);
        cropContainer = rootView.findViewById(R.id.crop_button);
        rotateContainer = rootView.findViewById(R.id.rotate_button);
        addContainer = rootView.findViewById(R.id.add_button);

        // Set layout for edit options
        flipOptions = rootView.findViewById(R.id.flip_options);
        rotateOptions = rootView.findViewById(R.id.rotate_options);
        closeButton = rootView.findViewById(R.id.close_edit_options_button);

        // Set function for edit buttons
        flipButton = rootView.findViewById(R.id.flip_icon);
        flipHorizontallyButton = rootView.findViewById(R.id.flip_horizontally_button);
        flipVerticallyButton = rootView.findViewById(R.id.flip_vertically_button);
        cropButton = rootView.findViewById(R.id.crop_icon);
        rotateButton = rootView.findViewById(R.id.rotate_icon);
        rotateLeftButton = rootView.findViewById(R.id.rotate_left_button);
        rotateRightButton = rootView.findViewById(R.id.rotate_right_button);

        imageToolsAddButton = rootView.findViewById(R.id.add_icon);
        imageToolsAddButton.setOnClickListener(v -> {
            setEditOptionsNone();
            setEditButtonActive(addContainer);
            selectMediaResource();
        });

        videoToolsAddButton = rootView.findViewById(R.id.post_video_edit_tools_add_icon);
        videoToolsAddButton.setOnClickListener(v -> {
            setEditOptionsNone();
            setEditButtonActive(addContainer);
            selectMediaResource();
        });

        flipButton.setOnClickListener(v -> {
            setEditOptionsNone();
            flipOptions.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);
            setEditButtonActive(flipContainer);
        });

        flipHorizontallyButton.setOnClickListener(v -> postImage.flipImageHorizontally());
        flipVerticallyButton.setOnClickListener(v -> postImage.flipImageVertically());

        cropButton.setOnClickListener(v -> {
            setEditOptionsNone();

            if (!isCropping) {
                setEditButtonActive(cropContainer);
                isCropping = true;
                postImage.setShowCropOverlay(true);
            } else {
                isCropping = false;
                postImage.setShowCropOverlay(false);
            }
        });
        postImage.setShowCropOverlay(false);

        rotateButton.setOnClickListener(v -> {
            setEditOptionsNone();
            rotateOptions.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);
            setEditButtonActive(rotateContainer);
        });

        rotateLeftButton.setOnClickListener(v -> postImage.rotateImage(90));
        rotateRightButton.setOnClickListener(v -> postImage.rotateImage(-90));



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
                caption = String.valueOf(captionInput.getText());
            }
        });

        showComponentsByType(type);

        return rootView;
    }

    void showComponentsByType(String type) {
        if (Objects.equals(type, "image")) {
            postVideo.setVisibility(View.GONE);
            postImage.setVisibility(View.VISIBLE);
            videoEditToolsContainer.setVisibility(View.GONE);
            imageEditToolsContainer.setVisibility(View.VISIBLE);
        } else if (Objects.equals(type, "video")) {
            postImage.setVisibility(View.GONE);
            postVideo.setVisibility(View.VISIBLE);
            imageEditToolsContainer.setVisibility(View.GONE);
            videoEditToolsContainer.setVisibility(View.VISIBLE);
        } else {
            postImage.setVisibility(View.GONE);
            postVideo.setVisibility(View.GONE);
            imageEditToolsContainer.setVisibility(View.GONE);
            videoEditToolsContainer.setVisibility(View.GONE);
            addPostButton.setVisibility(View.VISIBLE);
        }
    }

    private void setEditButtonActive(ConstraintLayout button) {
        button.setBackgroundColor(getResources().getColor(R.color.primary_100, null));

        if (button.getId() != R.id.crop_button) {
            isCropping = false;
            postImage.setShowCropOverlay(false);
        }
    }

    private void setEditOptionsNone() {
        flipContainer.setBackgroundColor(getResources().getColor(R.color.white, null));
        cropContainer.setBackgroundColor(getResources().getColor(R.color.white, null));
        rotateContainer.setBackgroundColor(getResources().getColor(R.color.white, null));
        addContainer.setBackgroundColor(getResources().getColor(R.color.white, null));

        flipOptions.setVisibility(View.GONE);
        rotateOptions.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
    }

    private void selectMediaResource() {
        launcher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE).build());
        setEditOptionsNone();
    }

    private void uploadPost() {
        loadingBar.setVisibility(View.VISIBLE);

        Date currentDate = new Date();
        Timestamp createdAt = new Timestamp(currentDate);
        String storagePath = "post/" + postID;

        if (Objects.equals(type, "image")) {
            Bitmap bitmap = postImage.getCroppedImage();
            if (bitmap != null) {
                postUri = BitmapUtils.bitmapToUri(context, bitmap);
                isEdited = true;
            }
        }

        Storage.getRef(storagePath).putFile(postUri)
                .addOnSuccessListener(taskSnapshot -> {
                    StorageReference postRef = Storage.getRef(storagePath);
                    postRef.getDownloadUrl()
                            .addOnSuccessListener(postURI -> {
                                Schema.Post post = new Schema.Post(postID, profile.id, postURI.toString(), type, caption, new ArrayList<>(), false, createdAt);
                                Database.addPost(post);

                                PostState postState = PostState.getInstance();
                                ArrayList<Schema.Post> myPosts = postState.myPosts;
                                myPosts.add(0, post);
                                postState.updateObservableMyPosts(myPosts);

                                removeFileIfItIsEdited();

                                postImage.setImageBitmap(null);
                                postVideo.setVideoURI(null);
                                captionInput.setText("");

                                setEditOptionsNone();

                                type = "";
                                showComponentsByType(type);

                                Toast.makeText(context, "Uploaded!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                removeFileIfItIsEdited();
                                Log.d("-->", "failed to get post: " + e);
                                Toast.makeText(context, "Failed to upload post!", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    removeFileIfItIsEdited();
                    Log.d("-->", "failed to put file: " + e);
                    Toast.makeText(context, "Failed to upload post!", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(e -> {
                    loadingBar.setVisibility(View.GONE);
                });
    }

    void removeFileIfItIsEdited() {
        if (isEdited) {
            try {
                int rowsDeleted = context.getContentResolver().delete(postUri, null, null);
                if (rowsDeleted == 0) {
                    Toast.makeText(context, "Failed to delete edited file", Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                Toast.makeText(context, "SecurityException: Lack of permissions to delete " + postUri, Toast.LENGTH_SHORT).show();
            }
        }
    }
}