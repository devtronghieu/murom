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

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.ViewHolder> {
    private Context context;

    private final ArrayList<HighlightBubbleModel> localDataSet;
    public static class HighlightBubbleModel {
        private final ArrayList<String> url;
        private final String name;
        private final String image_url;

        public HighlightBubbleModel(ArrayList<String> url, String name, String image_url)
        {
            this.url = url;
            this.name = name;
            this.image_url = image_url;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout highlightImageContainer;
        private final ImageView highlightImage;
        private final TextView highlightName;

        public ViewHolder(View view) {
            super(view);

            highlightImageContainer = view.findViewById(R.id.highlights_bubble_image_container);
            highlightImage = view.findViewById(R.id.highlight_bubble_image);
            highlightName = view.findViewById(R.id.highlight_name);
        }

        public LinearLayout getHighlightImageContainer() { return highlightImageContainer; }

        public ImageView getHighlightImage() {
            return highlightImage;
        }

        public TextView getHighlightName() {
            return highlightName;
        }
    }

    public HighlightAdapter(ArrayList<HighlightAdapter.HighlightBubbleModel> dataSet) {
        localDataSet = dataSet;
    }

    @NonNull

    @Override
    public HighlightAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_highlight_bubble, viewGroup, false);

        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(HighlightAdapter.ViewHolder viewHolder, final int position) {
        HighlightAdapter.HighlightBubbleModel data = localDataSet.get(position);
        viewHolder.highlightName.setText(data.name);
        Glide.with(this.context).load(data.image_url).into(viewHolder.highlightImage);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

}
