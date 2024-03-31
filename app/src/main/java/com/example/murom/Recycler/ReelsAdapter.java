package com.example.murom.Recycler;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.R;

import java.util.ArrayList;

public class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<ReelsAdapter.ReelModel> localDataSet;
    public static class ReelModel {
        private final String avatarUrl;
        private final String username;
        private final String videoUrl;
        private final String caption;

        public ReelModel(
                String avatarUrl,
                String username,
                String videoUrl,
                String caption
        ) {
            this.avatarUrl = avatarUrl;
            this.username = username;
            this.videoUrl = videoUrl;
            this.caption = caption;
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final VideoView video;
        private final ImageView avatar;
        private final TextView username, caption;
        private final ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            video = view.findViewById(R.id.reels_video);
            avatar = view.findViewById(R.id.reels_avatar);
            username = view.findViewById(R.id.reels_username);
            caption = view.findViewById(R.id.reels_caption);
            progressBar = view.findViewById(R.id.reels_progressbar);
        }
    }
    public ReelsAdapter(ArrayList<ReelsAdapter.ReelModel> dataSet) {
        localDataSet = dataSet;
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
        Glide.with(this.context)
                .load(data.avatarUrl)
                .into(viewHolder.avatar);

    }
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }


}
