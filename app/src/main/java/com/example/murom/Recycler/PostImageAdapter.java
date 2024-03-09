package com.example.murom.Recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.R;

import java.util.ArrayList;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ViewHolder> {
    private Context context;

    private final ArrayList<PostImageModel> localDataSet;

    public static class PostImageModel {
        private final String url;
        public PostImageModel(
                String url
        ) {
            this.url = url;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;

        public ViewHolder(View view) {
            super(view);

            image = view.findViewById(R.id.post_image);
        }
    }

    public PostImageAdapter(ArrayList<PostImageModel> dataSet) {
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_post_image, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        PostImageModel data = localDataSet.get(position);
        Glide.with(this.context).load(data.url).into(viewHolder.image);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
