package com.example.murom.Recycler;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        public StoryBubbleModel(String imageUrl, String text) {
            this.imageUrl = imageUrl;
            this.text = text;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView storyImage;
        private final TextView storyText;

        public ViewHolder(View view) {
            super(view);

            storyImage = view.findViewById(R.id.story_bubble_image);
            storyText = view.findViewById(R.id.story_bubble_text);
        }

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
        viewHolder.getStoryText().setText(localDataSet.get(position).text);
        Glide.with(this.context).load(localDataSet.get(position).imageUrl).into(viewHolder.getStoryImage());
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
