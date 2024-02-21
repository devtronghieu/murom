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

    public static class StoryBubbleModel {
        private final String imageUrl;
        private final String text;
        private final boolean isRead;

        public StoryBubbleModel(String imageUrl, String text, boolean isRead) {
            this.imageUrl = imageUrl;
            this.text = text;
            this.isRead = isRead;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout storyImageContainer;
        private final ImageView storyImage;
        private final TextView storyText;

        public ViewHolder(View view) {
            super(view);

            storyImageContainer = view.findViewById(R.id.story_bubble_image_container);
            storyImage = view.findViewById(R.id.story_bubble_image);
            storyText = view.findViewById(R.id.story_bubble_text);
        }

        public LinearLayout getStoryImageContainer() { return storyImageContainer; }

        public ImageView getStoryImage() {
            return storyImage;
        }

        public TextView getStoryText() {
            return storyText;
        }
    }

    public StoryBubbleAdapter(ArrayList<StoryBubbleModel> dataSet) {
        localDataSet = dataSet;
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

        if (!data.isRead) {
            viewHolder.storyImageContainer.setBackgroundResource(R.drawable.gradient_border);
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
