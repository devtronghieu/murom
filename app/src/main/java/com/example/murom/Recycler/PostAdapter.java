package com.example.murom.Recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.R;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;

import java.util.ArrayList;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context context;

    private final ArrayList<PostModel> localDataSet;

    public static class PostModel {
        private final String postID;
        private final String avatarUrl;
        private final String username;
        private final ArrayList<String> images;
        private final String caption;
        private final ArrayList<String> lovedByUsers;
        private final boolean loved;

        public PostModel(
                String postID,
                String avatarUrl,
                String username,
                ArrayList<String> images,
                String caption,
                ArrayList<String> lovedByUsers,
                boolean loved
        ) {
            this.postID = postID;
            this.avatarUrl = avatarUrl;
            this.username = username;
            this.images = images;
            this.caption = caption;
            this.lovedByUsers = lovedByUsers;
            this.loved = loved;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView username;
        private final ImageView image;
        private final ImageButton loveBtn;
        private final ImageButton commentBtn;
        private final TextView loveText;
        private final TextView caption;

        private final ImageButton editButton;
        private final LinearLayout editContainer;
        private final Button deleteButton;
        private final Button archiveButton;


        public ViewHolder(View view) {
            super(view);

            avatar = view.findViewById(R.id.post_avatar);
            username = view.findViewById(R.id.post_username);
            image = view.findViewById(R.id.post_image);
            loveBtn = view.findViewById(R.id.post_love_icon);
            commentBtn = view.findViewById(R.id.post_comment_icon);
            loveText = view.findViewById(R.id.post_love_text);
            caption = view.findViewById(R.id.post_desc);

            editButton = view.findViewById(R.id.post_edit_button);
            editContainer = view.findViewById(R.id.post_edit_container);
            deleteButton = view.findViewById(R.id.post_delete_button);
            archiveButton = view.findViewById(R.id.post_archive_button);
        }
    }

    public PostAdapter(ArrayList<PostModel> dataSet) {
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_post, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        PostModel data = localDataSet.get(position);

        viewHolder.username.setText(data.username);

        Glide.with(this.context).load(data.avatarUrl).into(viewHolder.avatar);

        Glide.with(this.context).load(data.images.get(0)).into(viewHolder.image);

        String loveText = "";
        loveText += data.lovedByUsers.size();
        loveText += " likes";
        viewHolder.loveText.setText(loveText);

        viewHolder.caption.setText(data.caption);

        // Edit: Delete / Archive
        if (Objects.equals(data.username, ProfileState.getInstance().profile.username)) {
            viewHolder.editButton.setVisibility(View.VISIBLE);

            viewHolder.editContainer.setElevation(8);

            viewHolder.editButton.setOnClickListener(v -> {
                boolean isEditContainerShowing = viewHolder.editContainer.getVisibility() == View.VISIBLE;
                if (isEditContainerShowing) {
                    viewHolder.editContainer.setVisibility(View.GONE);
                } else {
                    viewHolder.editContainer.setVisibility(View.VISIBLE);
                }
            });

            viewHolder.deleteButton.setOnClickListener(v -> {
                deletePost(data.postID);
            });
            viewHolder.archiveButton.setOnClickListener(v -> {
                Database.archivePost(data.postID);
                viewHolder.editContainer.setVisibility(View.GONE);
            });
        }
    }

    private void deletePost(String id) {
        Database.deletePost(id, new Database.DeletePostCallback() {
            @Override
            public void onDeleteSuccess(String postID) {
                PostState instance = PostState.getInstance();

                for (int i = 0; i < instance.myPosts.size(); i++) {
                    if (Objects.equals(instance.myPosts.get(i).id, postID)) {
                        instance.myPosts.remove(i);
                    }
                }
                instance.updateObservableMyPosts(instance.myPosts);
            }

            @Override
            public void onDeleteFailure() {
                Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
