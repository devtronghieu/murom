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
import com.example.murom.Recycler.ArchiveStoryAdapter;
import com.example.murom.Recycler.PostsProfileAdapter;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;
import com.example.murom.State.StoryState;
import com.example.murom.Utils.BitmapUtils;

import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.Disposable;

public class ArchiveFragment extends Fragment {
    RecyclerView postsRecycler;
    RecyclerView storiesRecycler;
    Disposable myArchivePostsDisposable;
    Disposable myArchiveStoriesDisposable;
    ImageButton backBtn;
    TextView selectedOption;
    ImageButton dropdownButton;
    View selectionDropdown;
    Button postBtn;
    Button storyBtn;

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
        Schema.User profile = ProfileState.getInstance().profile;

        backBtn = rootView.findViewById(R.id.back_btn);
        selectedOption = rootView.findViewById(R.id.selected_archive_option);
        dropdownButton = rootView.findViewById(R.id.dropdown_button);
        selectionDropdown = rootView.findViewById(R.id.archive_option_selection_dropdown);
        postBtn = rootView.findViewById(R.id.select_post_btn);
        storyBtn = rootView.findViewById(R.id.select_story_btn);
        postsRecycler = rootView.findViewById(R.id.archive_post_list);
        storiesRecycler = rootView.findViewById(R.id.archive_story_list);

        backBtn.setOnClickListener(v -> callback.onClose());
        dropdownButton.setOnClickListener(v -> showDropdown());
        postBtn.setOnClickListener(v -> selectArchivePosts());
        storyBtn.setOnClickListener(v -> selectArchiveStories());

        postsRecycler.setLayoutManager(new GridLayoutManager(requireContext(),3));
        myArchivePostsDisposable = PostState.getInstance().getObservableMyPosts().subscribe(this::renderMyPosts);

        storiesRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        myArchiveStoriesDisposable = StoryState.getInstance().getObservableStoriesMap().subscribe(storyMap -> renderMyStories(storyMap.get(profile.id)));

       return rootView;
    }

    private void showDropdown() {
        selectionDropdown.setVisibility(View.VISIBLE);
    }

    private void selectArchivePosts() {
        selectedOption.setText("Post");
        selectionDropdown.setVisibility(View.GONE);
    }

    private void selectArchiveStories() {
        selectedOption.setText("Story");
        selectionDropdown.setVisibility(View.GONE);
    }

    private void renderMyPosts(ArrayList<Schema.Post> myPosts) {
        ArrayList<PostsProfileAdapter.PostsProfileModel> postsProfileModel = new ArrayList<>();

        myPosts.forEach(post -> {
            postsProfileModel.add(new PostsProfileAdapter.PostsProfileModel(post.url));
        });

        PostsProfileAdapter postsProfileAdapter = new PostsProfileAdapter(postsProfileModel);
        postsRecycler.setAdapter(postsProfileAdapter);
    }

    private void renderMyStories(ArrayList<Schema.Story> myStories) {
        ArrayList<ArchiveStoryAdapter.ArchiveStoryModel> archiveStoryModel = new ArrayList<>();

        myStories.forEach(post -> {
            archiveStoryModel.add(new ArchiveStoryAdapter.ArchiveStoryModel(post.url));
        });

        ArchiveStoryAdapter archiveStoryAdapter = new ArchiveStoryAdapter(archiveStoryModel);
        postsRecycler.setAdapter(archiveStoryAdapter);
    }
}