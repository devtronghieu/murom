package com.example.murom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Firebase.Storage;
import com.example.murom.State.ProfileState;

import java.util.Objects;


public class EditProfileFragment extends Fragment {

    ProfileState profileState = ProfileState.getInstance();
    String currentUserUid = profileState.profile.id;
    Spinner privacy;
    String imageURL ="";
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

    public interface EditProfileFragmentCallback{
        void onClose();
    }

    EditProfileFragmentCallback callback;

    public EditProfileFragment (EditProfileFragmentCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_edit_profile, container, false);

        Schema.User profile = ProfileState.getInstance().profile;

        ImageButton backBtn = rootView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> callback.onClose());

        ImageView avatar = rootView.findViewById(R.id.edit_profile_avatar);
        Button saveChangeBtn = rootView.findViewById(R.id.save_edit_profile_button);

        Button changeBtn = rootView.findViewById(R.id.change_avatar_button);
        changeBtn.setOnClickListener(view -> setupImagePicker(avatar));

        EditText username = rootView.findViewById(R.id.edit_profile_username);
        username.setText(profile.username);

        EditText description = rootView.findViewById(R.id.edit_profile_description);
        description.setText(profile.bio);

        privacy = rootView.findViewById(R.id.edit_profile_privacy);
        initSpinnerFooter();

        saveChangeBtn.setOnClickListener(view -> saveChanges(
                username.getText().toString(),
                description.getText().toString(),
                privacy.getSelectedItem().toString())
        );
        Button logoutBtn = rootView.findViewById(R.id.log_out_btn);
        logoutBtn.setOnClickListener(this::handleSignOut);

        Glide.with(avatar.getContext())
                .load(profile.profilePicture)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(avatar);

        return rootView;
    }

    private void initSpinnerFooter(){
        String[] items = new String[]{"Public", "Private"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, items);
        privacy.setAdapter(adapter);
        privacy.setDropDownWidth(500);
        for (int index = 0; index < items.length; index++){
            if (Objects.equals(items[index], profileState.profile.status)){
                privacy.setSelection(index);
            }
        }

    }

    private void setupImagePicker(ImageView imageView) {
        this.pickedImageView = imageView;
        launcher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }

    private void handleSignOut(View view) {
        Auth.signOut();
        Intent i = new Intent(getActivity(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void saveChanges(String newUsername, String newDescription, String newStatus) {
        Database.updateUserProfile( currentUserUid, newUsername, newDescription, newStatus, new Database.UpdateUserProfileCallback() {
            @Override
            public void onSaveSuccess(Schema.User user) {
                profileState.updateObservableProfile(user);
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSaveFailure(String errorMessage) {
                Toast.makeText(requireContext(), "Failed to update profile" , Toast.LENGTH_SHORT).show();
            }
        });
    }

}