package com.example.murom.Recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.R;

import java.util.ArrayList;

public class StoryBubbleAdapter extends RecyclerView.Adapter<StoryBubbleAdapter.ViewHolder> {
    private Context context;

    private final ArrayList<StoryBubbleModel> localDataSet;

    private final StoryBubbleCallback callback;

    public interface StoryBubbleCallback {
        void handleUploadStory();
        void handleViewStories(String uid);
    }

    public static class StoryBubbleModel {
        private final String uid;
        private final String imageUrl;
        private final String text;
        private final boolean isViewed;

        public StoryBubbleModel(String uid, String imageUrl, String text, boolean isViewed) {
            this.uid = uid;
            this.imageUrl = imageUrl;
            this.text = text;
            this.isViewed = isViewed;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout storyImageContainer;
        private final ImageView storyImage;
        private final ImageView uploadButton;
        private final TextView storyText;

        public ViewHolder(View view) {
            super(view);

            storyImageContainer = view.findViewById(R.id.story_bubble_image_container);
            storyImage = view.findViewById(R.id.story_bubble_image);
            uploadButton = view.findViewById(R.id.story_bubble_upload);
            storyText = view.findViewById(R.id.story_bubble_text);
        }
    }


    public StoryBubbleAdapter(ArrayList<StoryBubbleModel> dataSet, StoryBubbleCallback callback) {
        localDataSet = dataSet;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_story_bubble, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        StoryBubbleModel data = localDataSet.get(position);

        viewHolder.storyText.setText(data.text);

        Glide.with(this.context).load(data.imageUrl).into(viewHolder.storyImage);

        viewHolder.storyImage.setOnClickListener(v -> callback.handleViewStories(data.uid));

        if (position == 0) {
            viewHolder.uploadButton.setVisibility(View.VISIBLE);
            viewHolder.uploadButton.setOnClickListener(v -> callback.handleUploadStory());
        }

        if (!data.isViewed) {
            viewHolder.storyImageContainer.setBackgroundResource(R.drawable.gradient_border);
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
