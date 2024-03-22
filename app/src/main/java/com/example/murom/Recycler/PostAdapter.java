package com.example.murom.Recycler;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.R;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context context;

    private final ArrayList<PostModel> localDataSet;

    public static class PostModel {
        private final String avatarUrl;
        private final String username;
        private final ArrayList<String> images;
        private final String caption;
        private final ArrayList<String> lovedByUsers;
        private final boolean loved;

        public PostModel(
                String avatarUrl,
                String username,
                ArrayList<String> images,
                String caption,
                ArrayList<String> lovedByUsers,
                boolean loved
        ) {
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

        public ViewHolder(View view) {
            super(view);

            avatar = view.findViewById(R.id.post_avatar);
            username = view.findViewById(R.id.post_username);
            image = view.findViewById(R.id.post_image);
            loveBtn = view.findViewById(R.id.post_love_icon);
            commentBtn = view.findViewById(R.id.post_comment_icon);
            loveText = view.findViewById(R.id.post_love_text);
            caption = view.findViewById(R.id.post_desc);
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
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
