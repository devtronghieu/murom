package com.example.murom.Recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.R;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<PostModel> localDataSet;
    public static class PostModel{
        private final  String imgUrl;
        public  PostModel(String imgUrl)
        {
            this.imgUrl = imgUrl;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton post;
        public ViewHolder(View view){
            super(view);

            post= view.findViewById(R.id.postButton);
        }
    }
    public PostAdapter(ArrayList<PostAdapter.PostModel> dataSet){
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public  ViewHolder onCreateViewHolder(ViewGroup  viewGroup, int viewType) {
        this.context = viewGroup.getContext();
        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_post, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        PostModel data = localDataSet.get(position);
        Glide.with(this.context).load(data.imgUrl).into(viewHolder.post);
    }

    @Override
    public int getItemCount(){return localDataSet.size();}
}
