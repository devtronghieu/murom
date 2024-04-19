package com.example.murom.Recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Schema;
import com.example.murom.R;
import com.example.murom.State.ProfileState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<CommentAdapterModel> localDataSet;

    public static class CommentAdapterModel {
        Schema.Comment comment;
        boolean isLoved;
        int loveCount;

        public  CommentAdapterModel(Schema.Comment comment, boolean isLoved, int loveCount) {
            this.comment = comment;
            this.isLoved = isLoved;
            this.loveCount = loveCount;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView username, content, timestamp, loveCount;
        public ImageButton loveBtn;

        public ViewHolder(View view) {

            super(view);

            avatar = view.findViewById(R.id.comment_avatar);
            username = view.findViewById(R.id.comment_username);
            content = view.findViewById(R.id.comment_content);
            timestamp = view.findViewById(R.id.comment_timestamp);
            loveBtn = view.findViewById(R.id.comment_love_icon);
            loveCount = view.findViewById(R.id.comment_love_count);
        }
    }

    public CommentAdapter(ArrayList<CommentAdapterModel> dataSet) {
        this.localDataSet = dataSet;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_comment, viewGroup,false);

        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        CommentAdapterModel commentAdapterModel = localDataSet.get(position);
        if (commentAdapterModel == null) return;

        Schema.Comment comment = commentAdapterModel.comment;

        Schema.User profile = ProfileState.getInstance().followerProfileMap.get(comment.userID);
        if (profile == null) return;

        Glide.with(this.context)
                .load(profile.profilePicture)
                .centerCrop()
                .into(viewHolder.avatar);

        viewHolder.username.setText(profile.username);

        viewHolder.content.setText(comment.content);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("vi"));
        String formattedDate = formatter.format(comment.timestamp.toDate());
        viewHolder.timestamp.setText(formattedDate);

        if (commentAdapterModel.isLoved) {
            viewHolder.loveBtn.setImageResource(R.drawable.murom_ic_love_active);
        }
        viewHolder.loveBtn.setOnClickListener(v -> {});
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
