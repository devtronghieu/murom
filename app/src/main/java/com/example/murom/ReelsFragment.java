package com.example.murom;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Recycler.CommentAdapter;
import com.example.murom.Recycler.PostAdapter;
import com.example.murom.Recycler.ReelsAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.State.CommentState;
import com.example.murom.State.ProfileState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.disposables.Disposable;


public class ReelsFragment extends Fragment {

    Activity activity;
    BottomSheetDialog commentBottomSheet;
    ViewPager2 reelViewPager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reels, container, false);
        reelViewPager = rootView.findViewById(R.id.reels_viewpager);
        ArrayList<ReelsAdapter.ReelModel> reels = new ArrayList<>();
        activity = getActivity();
        commentBottomSheet = new BottomSheetDialog(this.getContext());
        Database.getReels(10, new Database.GetReelsCallback() {
            @Override
            public void onGetReelsSuccess(ArrayList<Schema.Post> posts) {
                for (Schema.Post post : posts) {
                    Database.getUser(post.userId, new Database.GetUserCallback() {
                        @Override
                        public void onGetUserSuccess(Schema.User user) {
                            ArrayList<String> images = new ArrayList<>();
                            images.add(post.url);
                            reels.add(new ReelsAdapter.ReelModel(
                                    post.id,
                                    user.profilePicture,
                                    user.username,
                                    images.get(0),
                                    post.caption,
                                    post.lovedByUIDs
                            ));
                            setReels(reels);
                        }

                        @Override
                        public void onGetUserFailure() {
                        }
                    });
                }
            }

            @Override
            public void onGetReelsFailure(Exception exception) {
                // Handle failure to fetch reels
            }
        });

        return rootView;
    }
    void setReels(ArrayList<ReelsAdapter.ReelModel>reels){
        Log.d(".............", "reelsize: " + reels.size());
            ReelsAdapter reelsAdapter = new ReelsAdapter(reels, ReelsFragment.this::showCommentBottomSheet);
            reelViewPager.setAdapter(reelsAdapter);

    }

    void showCommentBottomSheet(String postID) {
        Log.d(".............", "here");
        View view = getLayoutInflater().inflate(R.layout.component_comment_bottom_sheet, null, false);

        RecyclerView commentRecyclerView = view.findViewById(R.id.comment_recycler);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        commentRecyclerView.addItemDecoration(new SpacingItemDecoration(0, 20));

        CommentState commentState = CommentState.getInstance();

        Database.getCommentsByPostID(postID, new Database.GetCommentsByPostIDCallback() {
            @Override
            public void onGetCommentsSuccess(ArrayList<Schema.Comment> comments) {
                HashMap<String, ArrayList<Schema.Comment>> commentsMap = commentState.commentsMap;
                commentsMap.put(postID, comments);
                commentState.updateObservableCommentsMap(commentsMap);
            }

            @Override
            public void onGetCommentsFailure() {
                Toast.makeText(activity, "Failed to get comments", Toast.LENGTH_SHORT).show();
            }
        });

        TextInputEditText inputEditText = view.findViewById(R.id.comment_input);
        ImageButton sendBtn = view.findViewById(R.id.comment_send_btn);
        sendBtn.setOnClickListener(v -> {
            String content = inputEditText.getText() != null ? inputEditText.getText().toString() : "";
            if (content.length() > 0) {
                Database.createComment(postID, content, new Database.CreateCommentCallback() {
                    @Override
                    public void onCreateCommentSuccess(String commentID) {
                        Schema.Comment comment = new Schema.Comment(
                                commentID,
                                postID,
                                ProfileState.getInstance().profile.id,
                                content,
                                new ArrayList<>(),
                                Timestamp.now()
                        );
                        HashMap<String, ArrayList<Schema.Comment>> commentsMap = commentState.commentsMap;
                        ArrayList<Schema.Comment> comments = commentsMap.get(postID);
                        if (comments == null) {
                            comments = new ArrayList<>();
                        }
                        comments.add(0, comment);
                        commentState.updateObservableCommentsMap(commentsMap);

                        inputEditText.setText("");
                    }

                    @Override
                    public void onCreateCommentFailure() {
                        Toast.makeText(activity, "Failed to comment", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Disposable commentsDisposable = commentState.getObservableCommentsMap().subscribe(commentsMap -> {
            ArrayList<Schema.Comment> comments = commentsMap.get(postID);
            if (comments == null) return;

            ArrayList<CommentAdapter.CommentAdapterModel> commentAdapterModels = new ArrayList<>();
            for (int i = 0; i < comments.size(); i++) {
                Schema.Comment comment = comments.get(i);
                commentAdapterModels.add(new CommentAdapter.CommentAdapterModel(
                        new Schema.Comment(comment.id, comment.postID, comment.userID, comment.content, comment.lovedBy, comment.timestamp),
                        comment.lovedBy.contains(ProfileState.getInstance().profile.id)
                ));
            }
            CommentAdapter commentAdapter = new CommentAdapter(commentAdapterModels);
            commentRecyclerView.setAdapter(commentAdapter);
        });

        commentBottomSheet.setContentView(view);
        commentBottomSheet.setOnCancelListener(dialogInterface -> {
            if (!commentsDisposable.isDisposed()) {
                commentsDisposable.dispose();
            }
        });
        commentBottomSheet.show();
    }
}