package com.example.murom.Recycler;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.murom.Firebase.Database;
import com.example.murom.R;
import com.example.murom.State.ProfileState;

import java.util.ArrayList;

public class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<ReelsAdapter.ReelModel> localDataSet;
    public interface ReelModelCallback {
        void showCommentBottomSheet(String reelID);
    }
    ReelModelCallback callback;
    public static class ReelModel {
        private  final String reelID;
        private final String avatarUrl;
        private final String username;
        private final String videoUrl;
        private final String caption;
        private final ArrayList<String> lovedByUsers;

        public ReelModel(
                String reelID,
                String avatarUrl,
                String username,
                String videoUrl,
                String caption,
                ArrayList<String> lovedByUsers
        ) {
            this.reelID = reelID;
            this.avatarUrl = avatarUrl;
            this.username = username;
            this.videoUrl = videoUrl;
            this.caption = caption;
            this.lovedByUsers = lovedByUsers;
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final VideoView video;
        private final ImageView avatar;
        private final TextView username, caption, loveText;
        private final ImageButton loveBtn, commentBtn;
        private final ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            video = view.findViewById(R.id.reels_video);
            avatar = view.findViewById(R.id.reels_avatar);
            loveBtn = view.findViewById(R.id.reels_love_icon);
            loveText = view.findViewById(R.id.reels_love_text);
            commentBtn = view.findViewById(R.id.reels_comment_icon);
            username = view.findViewById(R.id.reels_username);
            caption = view.findViewById(R.id.reels_caption);
            progressBar = view.findViewById(R.id.reels_progressbar);
        }
    }
    public ReelsAdapter(ArrayList<ReelModel> dataSet) {
        localDataSet = dataSet;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_reels_display, viewGroup, false);

        return new ReelsAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ReelModel data = localDataSet.get(position);
        viewHolder.video.setVideoPath(data.videoUrl);

        viewHolder.username.setText(data.username);
        viewHolder.caption.setText(data.caption);
        viewHolder.video.setOnPreparedListener(mp -> {
            viewHolder.progressBar.setVisibility(View.GONE);
            mp.start();
            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
            float screenRatio = viewHolder.video.getWidth() / (float) viewHolder.video.getHeight();
            float scale = videoRatio / screenRatio;
            if (scale >= 1f) {
                viewHolder.video.setScaleX(scale);
            } else {
                viewHolder.video.setScaleY(1f / scale);
            }
        });
        viewHolder.video.setOnCompletionListener(MediaPlayer::start);

        Log.d("-->", "avatar: " + data.avatarUrl);

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
            Database.updatePostLovedBy(data.reelID, data.lovedByUsers);
            notifyItemChanged(viewHolder.getAdapterPosition());
        });

        String loveText = "";
        loveText += data.lovedByUsers.size();
        viewHolder.loveText.setText(loveText);

        viewHolder.commentBtn.setOnClickListener(v -> {
            callback.showCommentBottomSheet(data.reelID);
        });
        Glide.with(this.context)
                .load(data.avatarUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(viewHolder.avatar);

    }
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

}
