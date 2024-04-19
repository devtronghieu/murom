package com.example.murom.Recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.R;

import java.util.ArrayList;

public class HighlightBubbleAdapter extends RecyclerView.Adapter<HighlightBubbleAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<HighlightBubbleModel> localDataSet;
    private final HighlightBubbleCallback callback;

    public interface HighlightBubbleCallback {
        void handleEditHighlight(String highlightId, String url, String name, ArrayList<String> stories);
        void handleDeleteHighlight(String highlightId);
        void handleViewHighlight(String highlightId);
        void handleAddHighlight();
    }

    public static class HighlightBubbleModel {
        private final String highlightId;
        private final String imageUrl;
        private final String name;
        private final ArrayList<String> stories;

        public HighlightBubbleModel(String highlightId, String imageUrl, String name, ArrayList<String> stories) {
            this.highlightId = highlightId;
            this.imageUrl = imageUrl;
            this.name = name;
            this.stories = stories;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView highlightImage;
        private final TextView highlightText;
        private final Button editHighlight;
        private final Button deleteHighlight;
        private final LinearLayout buttonsContainer;
        public ViewHolder(View view){
            super(view);
            highlightImage = view.findViewById(R.id.highlight_bubble_image);
            highlightText = view.findViewById(R.id.highlight_bubble_text);
            editHighlight = view.findViewById(R.id.edit_highlight_btn);
            deleteHighlight = view.findViewById(R.id.delete_highlight_btn);
            buttonsContainer = view.findViewById(R.id.buttons_container);
        }
    }

    public HighlightBubbleAdapter(ArrayList<HighlightBubbleModel> dataSet, HighlightBubbleCallback callback) {
        localDataSet = dataSet;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        this.context = viewGroup.getContext();
        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_highlight_bubble,viewGroup,false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position){
        HighlightBubbleModel data = localDataSet.get(position);
        viewHolder.highlightText.setText(data.name);
        Glide.with(this.context).load(data.imageUrl).into(viewHolder.highlightImage);
        viewHolder.editHighlight.setOnClickListener(v -> callback.handleEditHighlight(data.highlightId, data.imageUrl, data.name, data.stories));
        viewHolder.deleteHighlight.setOnClickListener(v -> {
            callback.handleDeleteHighlight(data.highlightId);
            viewHolder.buttonsContainer.setVisibility(View.GONE);

        });

        viewHolder.highlightImage.setOnClickListener(v -> callback.handleViewHighlight(data.highlightId));
        viewHolder.highlightImage.setOnLongClickListener(v -> {
            viewHolder.buttonsContainer.setVisibility(View.VISIBLE);
            viewHolder.buttonsContainer.setElevation(12);
            return true;
        });

        if(position == 0){
            viewHolder.highlightImage.setOnClickListener(v -> callback.handleAddHighlight());
            viewHolder.highlightImage.setOnLongClickListener(v -> {
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {return localDataSet.size();}
}
