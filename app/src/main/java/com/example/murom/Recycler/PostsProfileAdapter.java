package com.example.murom.Recycler;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.R;

import java.util.ArrayList;

public class PostsProfileAdapter extends RecyclerView.Adapter<PostsProfileAdapter.ViewHolder> {
    private Context context;
    private  final ArrayList<PostsProfileModel> localDataSet;
    public  static class PostsProfileModel{
        private final String postId;
        private final String imageUrl;

        public PostsProfileModel(String postId, String imageUrl){
            this.postId = postId;
            this.imageUrl = imageUrl;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageButton postImageButton;
        public ViewHolder(View view){
            super(view);
            postImageButton = view.findViewById(R.id.post_btn);
        }
    }

    public PostsProfileAdapter(ArrayList<PostsProfileAdapter.PostsProfileModel> dataSet, OnPostItemClickListener listener){
        localDataSet = dataSet;
        this.listener = listener;
    }
    private  OnPostItemClickListener listener;
    public interface OnPostItemClickListener {
        void onPostClick(String postId);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        this.context= viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_profile_post, viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position){
        PostsProfileModel data = localDataSet.get(position);
        Glide.with(this.context)
                .load(data.imageUrl)
                .override(viewHolder.postImageButton.getWidth(), viewHolder.postImageButton.getHeight())
                .fitCenter().centerCrop()
                .into(viewHolder.postImageButton);
        viewHolder.postImageButton.setOnClickListener(v -> {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onPostClick(localDataSet.get(position).postId);
            }
        });
    }
    @Override
    public int getItemCount(){ return localDataSet.size(); }
}
