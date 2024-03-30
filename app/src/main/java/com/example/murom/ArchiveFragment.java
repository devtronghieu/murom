package com.example.murom;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.murom.Firebase.Schema;
import com.example.murom.Recycler.PostsProfileAdapter;
import com.example.murom.State.PostState;

import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.Disposable;

public class ArchiveFragment extends Fragment {
    RecyclerView postsRecycler;
    Disposable myPostsDisposable;

    public interface ArchiveFragmentCallback{
        void onClose();
    }

    ArchiveFragment.ArchiveFragmentCallback callback;

    public ArchiveFragment (ArchiveFragment.ArchiveFragmentCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView = inflater.inflate(R.layout.fragment_archive, container, false);

        ImageButton backBtn = rootView.findViewById(R.id.back_btn);
        TextView selectedOption = rootView.findViewById(R.id.selected_archive_option);
        ImageButton dropdownButton = rootView.findViewById(R.id.dropdown_button);
        View selectionDropdown = rootView.findViewById(R.id.archive_option_selection_dropdown);
        Button postBtn = rootView.findViewById(R.id.select_post_btn);
        Button storyBtn = rootView.findViewById(R.id.select_story_btn);

        backBtn.setOnClickListener(v -> callback.onClose());
        dropdownButton.setOnClickListener(v -> showDropdown(selectionDropdown));
        postBtn.setOnClickListener(v -> {
            selectArchivePosts(selectedOption);
            selectionDropdown.setVisibility(View.GONE);
        });
        storyBtn.setOnClickListener(v -> {
            selectArchiveStories(selectedOption);
            selectionDropdown.setVisibility(View.GONE);
        });

        postsRecycler = rootView.findViewById(R.id.archive_post_list);
        postsRecycler.setLayoutManager(new GridLayoutManager(requireContext(),3));
        myPostsDisposable = PostState.getInstance().getObservableMyPosts().subscribe(this::renderMyPosts);

       return rootView;
    }

    private void showDropdown(View selectionDropdown) {
        selectionDropdown.setVisibility(View.VISIBLE);
    }

    private void selectArchivePosts(TextView selectedOption) {
        selectedOption.setText("Post");
    }

    private void selectArchiveStories(TextView selectedOption) {
        selectedOption.setText("Story");
    }

    private void renderMyPosts(ArrayList<Schema.Post> myPosts) {
        ArrayList<PostsProfileAdapter.PostsProfileModel> postsProfileModel = new ArrayList<>();

        myPosts.forEach(post -> {
            postsProfileModel.add(new PostsProfileAdapter.PostsProfileModel(post.url));
        });

        PostsProfileAdapter postsProfileAdapter = new PostsProfileAdapter(postsProfileModel);
        postsRecycler.setAdapter(postsProfileAdapter);
    }
}