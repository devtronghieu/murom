package com.example.murom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.gowtham.library.utils.TrimType;
import com.gowtham.library.utils.TrimVideo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    ImageButton imageToolsAddButton;

    ImageButton videoToolsAddButton;
    ImageButton videoToolsTrimButton;

    TextInputEditText captionInput;

    // Initialize edit options
    public ConstraintLayout flipOptions;
    public ConstraintLayout rotateOptions;
    public ImageButton closeButton;

    public ImageButton uploadButton;

    ActivityResultLauncher<PickVisualMediaRequest> launcher;

    ActivityResultLauncher<Intent> trimForResult;

    interface MoveCompletionListener {
        void onMoveComplete(Uri destinationUri);
    }
    private void moveFile(Uri sourceUri, File destinationFile, MoveCompletionListener listener) {
        new Thread(() -> {
            try {
                Files.move(Paths.get(sourceUri.getPath()), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                activity.runOnUiThread(() -> {
                    listener.onMoveComplete(Uri.fromFile(destinationFile));
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("-->", "move error: " + e);
            }
        }).start();
    }

    public PostFragment() {
        // Required empty public constructor
        profileDisposable = ProfileState.getInstance().getObservableProfile().subscribe(profile -> {
            this.profile = profile;
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri == null) {
                Toast.makeText(context, "No image/video selected!", Toast.LENGTH_SHORT).show();
            } else {
                String mimeType = activity.getContentResolver().getType(uri);
                type = mimeType != null && mimeType.startsWith("image/") ? "image" : "video";
                postUri = uri;
                isEdited = false;
                uploadButton.setEnabled(true);
                addPostButton.setVisibility(View.GONE);

                showComponentsByType(type);
            }
        });

        trimForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null) {
                        postUri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));

                        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File destFile = new File(downloadsDir, "instamurom_trimmed_video.mp4");
                        moveFile(postUri, destFile, destinationUri -> {
                            postUri = destinationUri;
                            isEdited = true;

                            postVideo.setVideoPath(postUri.toString());
                            postVideo.start();
                        });

                        postVideo.setVideoPath(postUri.toString());
                        postVideo.start();
                    } else
                        Log.d("-->", "videoTrimResultLauncher data is null");
                });
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
            selectMediaResource();
        });

        videoToolsTrimButton = rootView.findViewById(R.id.post_video_edit_tools_trim_icon);
        videoToolsTrimButton.setOnClickListener(v -> {
            setEditOptionsNone();
            isEdited = true;

            TrimVideo.activity(postUri.toString())
                    .setTrimType(TrimType.MIN_MAX_DURATION)
                    .setMinToMax(1, 15)
                    .start(this, trimForResult);
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

            postImage.setImageUriAsync(postUri);
            try{
                InputImage image = InputImage.fromFilePath(context, postUri);
                generateHashtagSuggestions(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(type, "video")) {
            postImage.setVisibility(View.GONE);
            postVideo.setVisibility(View.VISIBLE);
            imageEditToolsContainer.setVisibility(View.GONE);
            videoEditToolsContainer.setVisibility(View.VISIBLE);

            postVideo.setOnPreparedListener(mediaPlayer -> {
                long videoLength = mediaPlayer.getDuration();
                if (videoLength > 15000) {
                    TrimVideo.activity(postUri.toString())
                            .setTrimType(TrimType.MIN_MAX_DURATION)
                            .setMinToMax(1, 15)
                            .start(this, trimForResult);
                }
            });
            postVideo.setVideoPath(postUri.toString());
            postVideo.start();
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
        if (postUri == null) {
            Toast.makeText(activity, "Please add image/video!", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);

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
                                postUri = null;
                                showComponentsByType(type);

                                Toast.makeText(context, "Uploaded!", Toast.LENGTH_SHORT).show();
                                postID = UUID.randomUUID().toString();
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
            if (Objects.equals(type, "image")) {
                try {
                    int rowsDeleted = context.getContentResolver().delete(postUri, null, null);
                    if (rowsDeleted == 0) {
                        Toast.makeText(context, "Failed to delete edited file", Toast.LENGTH_SHORT).show();
                    }
                } catch (SecurityException e) {
                    Log.d("-->", "failed to delete edited file: " + e);
                }
            }

            if (Objects.equals(type, "video")) {
                try {
                    File fileToDelete = new File(Objects.requireNonNull(postUri.getPath()));
                    if (fileToDelete.exists()) {
                        if (fileToDelete.delete()) {
                            Log.d("-->", "File deleted successfully");
                        } else {
                            Log.d("-->", "Failed to delete file");
                        }
                    } else {
                        Log.d("-->", "File does not exist: " + postUri.getPath());
                    }
                } catch (Error e) {
                    Log.d("-->", "failed to delete edited file: " + e);
                }
            }
        }
    }

    private void generateHashtagSuggestions(InputImage image) {
        final StringBuilder hashtagBuilder = new StringBuilder();
        ImageLabelerOptions options =
                new ImageLabelerOptions.Builder().setConfidenceThreshold(0.8f).build();
        ImageLabeler labeler = ImageLabeling.getClient(options);
        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    for (ImageLabel label : labels) {
                        hashtagBuilder.append("#").append(label.getText()).append(" ");
                    }
                    String temp = hashtagBuilder.toString().trim();
                    captionInput.setText(temp);
                });
    }
}