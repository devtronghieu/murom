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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.murom.Firebase.Database;
import com.example.murom.R;
import com.example.murom.State.ProfileState;

import java.util.ArrayList;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context context;

    private final ArrayList<PostModel> localDataSet;


    public interface PostModelCallback {
        void showCommentBottomSheet(String postID);
        void showProfile(String uid);
    }

    PostModelCallback callback;

    public static class PostModel {
        private final String postOwnerID;
        private final String postID;
        private final String avatarUrl;
        private final String username;
        private final ArrayList<String> images;
        private final String caption;
        private final String date;
        private final ArrayList<String> lovedByUsers;

        public PostModel(
                String postOwnerID,
                String postID,
                String avatarUrl,
                String username,
                ArrayList<String> images,
                String caption,
                String date,
                ArrayList<String> lovedByUsers
        ) {
            this.postOwnerID = postOwnerID;
            this.postID = postID;
            this.avatarUrl = avatarUrl;
            this.username = username;
            this.images = images;
            this.caption = caption;
            this.date = date;
            this.lovedByUsers = lovedByUsers;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout dataContainer;
        private final ImageView avatar;
        private final TextView username;
        private final ImageView image;
        private final ImageButton loveBtn;
        private final ImageButton commentBtn;
        private final TextView loveText;
        private final TextView caption;
        private final TextView date;

        private final ImageButton editButton;
        private final LinearLayout editContainer;
        private final Button deleteButton;
        private final Button archiveButton;

        public ViewHolder(View view) {
            super(view);

            dataContainer = view.findViewById(R.id.post_user_data_container);
            avatar = view.findViewById(R.id.post_avatar);
            username = view.findViewById(R.id.post_username);
            image = view.findViewById(R.id.post_image);
            loveBtn = view.findViewById(R.id.post_love_icon);
            commentBtn = view.findViewById(R.id.post_comment_icon);
            loveText = view.findViewById(R.id.post_love_text);
            caption = view.findViewById(R.id.post_desc);
            date = view.findViewById(R.id.post_date);

            editButton = view.findViewById(R.id.post_edit_button);
            editContainer = view.findViewById(R.id.post_edit_container);
            deleteButton = view.findViewById(R.id.post_delete_button);
            archiveButton = view.findViewById(R.id.post_archive_button);
        }
    }

    public PostAdapter(ArrayList<PostModel> dataSet, PostModelCallback callback) {
        localDataSet = dataSet;
        this.callback = callback;
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

        Glide.with(this.context).load(data.avatarUrl).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(viewHolder.avatar);

        Glide.with(this.context).load(data.images.get(0)).into(viewHolder.image);

        String uid = ProfileState.getInstance().profile.id;
        boolean isLoved = data.lovedByUsers.contains(uid);
        if (isLoved) {
            viewHolder.loveBtn.setImageResource(R.drawable.murom_ic_love_active);
        }
        viewHolder.loveBtn.setOnClickListener(v -> {
            if (isLoved) {
                data.lovedByUsers.remove(uid);
            } else {
                data.lovedByUsers.add(uid);
            }
            Database.updatePostLovedBy(data.postID, data.lovedByUsers);
            notifyItemChanged(viewHolder.getAdapterPosition());
        });

        String loveText = "";
        loveText += data.lovedByUsers.size();
        loveText += " likes";
        viewHolder.loveText.setText(loveText);

        viewHolder.dataContainer.setOnClickListener(v -> {
            callback.showProfile(data.postOwnerID);
        });

        viewHolder.commentBtn.setOnClickListener(v -> {
            callback.showCommentBottomSheet(data.postID);
        });

        if (Objects.equals(data.caption, "")) {
            viewHolder.caption.setVisibility(View.GONE);
        } else {
            viewHolder.caption.setText(data.caption);
        }

        viewHolder.date.setText(data.date);

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
                Database.deletePost(data.postID, new Database.DeletePostCallback() {
                    @Override
                    public void onDeleteSuccess(String postID) {
                        localDataSet.remove(viewHolder.getBindingAdapterPosition());
                        notifyItemRemoved(viewHolder.getBindingAdapterPosition());
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDeleteFailure() {
                        Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            
            viewHolder.archiveButton.setOnClickListener(v -> {
                Database.archivePost(data.postID, new Database.ArchivePostCallback() {
                    @Override
                    public void onArchivePostSuccess() {
                        localDataSet.remove(viewHolder.getBindingAdapterPosition());
                        notifyItemRemoved(viewHolder.getBindingAdapterPosition());
                        Toast.makeText(context, "Archived", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onArchivePostFailure() {
                        Toast.makeText(context, "Failed to archive post", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
