package com.example.murom;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Recycler.CommentAdapter;
import com.example.murom.Recycler.PostAdapter;
import com.example.murom.Recycler.SpacingItemDecoration;
import com.example.murom.State.CommentState;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.Disposable;


public class DetailPostFragment extends Fragment {
    Activity activity;
    RecyclerView detailPostRecycler;
    BottomSheetDialog commentBottomSheet;
    Disposable socialPostsDisposable;

    public DetailPostFragment(DetailPostFragmentCallback callback) {
        this.callback = callback;
    }

    public interface DetailPostFragmentCallback{
        void onClose();
        void onViewProfile(String uid);
    }

    DetailPostFragment.DetailPostFragmentCallback callback;

    public static DetailPostFragment newInstance(String postId){
        DetailPostFragment detailPostFragment = new DetailPostFragment(null);
        Bundle args = new Bundle();
        args.putString("postId", postId);
        detailPostFragment.setArguments(args);
        return detailPostFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_post, container, false);
        activity = getActivity();
        String postId = getArguments().getString("postId");
        commentBottomSheet = new BottomSheetDialog(this.getContext());
        detailPostRecycler = rootView.findViewById(R.id.post_detail_recycler);
        detailPostRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        detailPostRecycler.addItemDecoration(new SpacingItemDecoration(0, 45));
        ImageButton backBtn = rootView.findViewById(R.id.back_detail_post_btn);

        backBtn.setOnClickListener(v -> callback.onClose());
        ProfileState profileState = ProfileState.getInstance();

        PostState postState = PostState.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d'th', yyyy", Locale.ENGLISH);

        Database.getPostsByUID(postId, new Database.GetPostsByUIDCallback() {
            @Override
            public void onGetPostsSuccess(ArrayList<Schema.Post> posts) {

            }

            @Override
            public void onGetPostsFailure() {

            }

            @Override
            public void onPostCountRetrieved(int postCount) {

            }
        });
        Database.getPostByID(postId, new Database.GetPostByIDCallback() {
            @Override
            public void onGetPostSuccess(Schema.Post post) {
                ArrayList<PostAdapter.PostModel> postModels = new ArrayList<>();
                Database.getUser(post.userId, new Database.GetUserCallback() {
                    @Override
                    public void onGetUserSuccess(Schema.User user) {
                        ArrayList<String> images = new ArrayList<>();
                        images.add(post.url);
                        postModels.add(new PostAdapter.PostModel(
                                post.userId,
                                post.id,
                                user.profilePicture,
                                user.username,
                                images,
                                post.caption,
                                dateFormat.format(post.createdAt.toDate()),
                                post.lovedByUIDs
                        ));
                        setNewsfeeds(postModels);
                    }

                    @Override
                    public void onGetUserFailure() {

                    }
                });
                Log.d("-----------------",post.id+ "   " + post.userId);
            }

            @Override
            public void onGetPostFailure() {

            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView(){
        if (!socialPostsDisposable.isDisposed()) {
            socialPostsDisposable.dispose();
        }

        super.onDestroyView();
    }

    void setNewsfeeds(ArrayList<PostAdapter.PostModel> newsfeeds) {
        PostAdapter postAdapter = new PostAdapter(newsfeeds, new PostAdapter.PostModelCallback() {
            @Override
            public void showCommentBottomSheet(String postID) {
                DetailPostFragment.this.showCommentBottomSheet(postID);
            }

            @Override
            public void showProfile(String uid) {
                callback.onViewProfile(uid);
            }
        });
        detailPostRecycler.setAdapter(postAdapter);
    }
    void showCommentBottomSheet(String postID) {
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