package com.example.murom.Recycler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.example.murom.R;

import java.util.ArrayList;

public class PostsProfileAdapter extends RecyclerView.Adapter<PostsProfileAdapter.ViewHolder> {
    private Context context;
    private  final ArrayList<PostsProfileModel> localDataSet;
    public  static class PostsProfileModel{
        private final ArrayList<String> images;

        public PostsProfileModel(ArrayList<String> images){this.images = images;}
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageButton posts;
        public ViewHolder(View view){
            super(view);
            posts = view.findViewById(R.id.post_btn);
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
                .inflate(R.layout.component_post, viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position){
        PostsProfileModel data = localDataSet.get(position);
        Glide.with(this.context).load(data.images.get(0)).into(viewHolder.posts);

    }
    @Override
    public int getItemCount(){return localDataSet.size();}
}
