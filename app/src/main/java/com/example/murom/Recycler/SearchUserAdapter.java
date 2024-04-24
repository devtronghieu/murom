package com.example.murom.Recycler;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.R;
import com.example.murom.State.ProfileState;

import java.util.ArrayList;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    private Context context;

    private final ArrayList<Schema.SearchUser> localDataSet;
    private OnUserItemClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView username;
        private final Button btn_follow;
        public ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.component_user_display_avatar);
            username = view.findViewById(R.id.component_user_display_username);
            btn_follow = view.findViewById(R.id.search_follow_btn);
        }

        public void setOnClickListener(String uid) {
        }
    }

    public SearchUserAdapter(ArrayList<Schema.SearchUser> dataSet, OnUserItemClickListener listener) {
        localDataSet = dataSet;
        this.listener = listener;
    }

    public interface OnUserItemClickListener {
        void onSearchUserItemClick(String userId);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_user_display, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Schema.SearchUser data = localDataSet.get(position);

        viewHolder.username.setText(data.username);
        viewHolder.btn_follow.setVisibility(View.VISIBLE);
        if (Auth.getUser().getUid().equals(data.userId)) {
            viewHolder.btn_follow.setVisibility(View.GONE);
        }
        Glide.with(this.context).load(data.avatarUrl).centerCrop().into(viewHolder.avatar);

        Database.getUser(data.userId, new Database.GetUserCallback() {
            @Override
            public void onGetUserSuccess(Schema.User user) {
                Database.isFollowing(data.userId, viewHolder.btn_follow, isFollowing -> {
                    ProfileState profileState = ProfileState.getInstance();
                    if (!isFollowing) {
                        profileState.followerProfileMap.remove(user.id);
                        profileState.followerIDs.remove(user.id);
                    } else {
                        profileState.followerProfileMap.put(user.id, user);
                        if (!profileState.followerIDs.contains(user.id)) {
                            profileState.followerIDs.add(user.id);
                        }
                    }
                });
            }

            @Override
            public void onGetUserFailure() {

            }
        });


        viewHolder.itemView.setOnClickListener(v -> {
            int adapterPosition = viewHolder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onSearchUserItemClick(localDataSet.get(position).userId);
            }
            Log.d("-->", "Click on: " + data.userId);
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
