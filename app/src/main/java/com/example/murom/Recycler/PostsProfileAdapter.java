package com.example.murom.Recycler;

import android.content.Context;
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
        private final String imageUrl;

        public PostsProfileModel(String imageUrl){ this.imageUrl = imageUrl; }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageButton postImageButton;
        public ViewHolder(View view){
            super(view);
            postImageButton = view.findViewById(R.id.post_btn);
        }
    }

    public PostsProfileAdapter(ArrayList<PostsProfileAdapter.PostsProfileModel> dataSet){
        localDataSet = dataSet;
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
                .fitCenter()
                .into(viewHolder.postImageButton);
    }
    @Override
    public int getItemCount(){ return localDataSet.size(); }
}
