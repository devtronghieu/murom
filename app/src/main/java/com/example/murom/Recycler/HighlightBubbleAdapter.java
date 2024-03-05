package com.example.murom.Recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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
        void handleUploadHighlight();

        void handleViewHighlight();
    }

    public static class HighlightBubbleModel {
        private final String uid;
        private final String imageUrl;
        private final String text;


        public HighlightBubbleModel(String uid, String imageUrl, String text) {
            this.uid = uid;
            this.imageUrl = imageUrl;
            this.text = text;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final LinearLayout highlightImageContainer;
        private final ImageView highlightImage;
        private final ImageView uploadButton;
        private final TextView highlightText;
        public ViewHolder(View view){
            super(view);
            highlightImageContainer = view.findViewById(R.id.highlight_bubble_image_container);
            highlightImage = view.findViewById(R.id.highlight_bubble_image);
            uploadButton =view.findViewById(R.id.highlight_bubble_upload);
            highlightText = view.findViewById(R.id.highlight_bubble_text);

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
        viewHolder.highlightText.setText(data.text);
        Glide.with(this.context).load(data.imageUrl).into(viewHolder.highlightImage);
        viewHolder.highlightImage.setOnClickListener(v -> callback.handleViewHighlight());

        if(position == 0){
            viewHolder.uploadButton.setVisibility(View.VISIBLE);
            viewHolder.uploadButton.setOnClickListener(v -> callback.handleViewHighlight());
        }
    }

    @Override
    public int getItemCount() {return localDataSet.size();}
}
