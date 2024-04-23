package com.example.murom;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Recycler.ArchiveStoryAdapter;
import com.example.murom.Recycler.PostsProfileAdapter;
import com.example.murom.State.ArchivedStoryState;
import com.example.murom.State.PostState;
import com.example.murom.State.ProfileState;

import java.util.ArrayList;
import java.util.HashMap;

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
    Schema.User profile = ProfileState.getInstance().profile;

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

        storiesRecycler.setVisibility(View.GONE);

       return rootView;
    }

    private void showDropdown() {
        selectionDropdown.setVisibility(View.VISIBLE);
    }

    private void selectArchivePosts() {
        selectedOption.setText("Post");
        postsRecycler.setVisibility(View.VISIBLE);
        storiesRecycler.setVisibility(View.GONE);
        selectionDropdown.setVisibility(View.GONE);
    }

    private void selectArchiveStories() {
        Log.d("-->", "selectArchiveStories: ");
        Database.getArchivedStoriesByUID(profile.id, new Database.GetStoriesByUIDCallback() {
            @Override
            public void onGetStoriesSuccess(ArrayList<Schema.Story> stories) {
                ArchivedStoryState.getInstance().updateObservableArchivedStoriesMap(stories);
            }

            @Override
            public void onGetStoriesFailure() {
                Toast.makeText(requireContext(), "Failed to load stories", Toast.LENGTH_SHORT).show();
            }
        });

        myArchiveStoriesDisposable = ArchivedStoryState
                .getInstance().getObservableArchivedStoriesMap()
                .subscribe(archivedStoryMap -> renderMyStories(archivedStoryMap));
        selectedOption.setText("Story");
        storiesRecycler.setVisibility(View.VISIBLE);
        postsRecycler.setVisibility(View.GONE);
        selectionDropdown.setVisibility(View.GONE);

        Log.d("-->", "selectArchiveStories: ");

    }

    void renderMyPosts(ArrayList<Schema.Post> myPosts) {
        ArrayList<PostsProfileAdapter.PostsProfileModel> archivePostModel = new ArrayList<>();

        myPosts.forEach(post -> {
            if (!post.isArchived) return;
            archivePostModel.add(new PostsProfileAdapter.PostsProfileModel(post.id, post.url));
        });

        PostsProfileAdapter postsProfileAdapter = new PostsProfileAdapter(archivePostModel, null);
        postsRecycler.setAdapter(postsProfileAdapter);
    }

    private void renderMyStories(ArrayList<Schema.Story> myStories) {
        ArrayList<ArchiveStoryAdapter.ArchiveStoryModel> archiveStoryModel = new ArrayList<>();

        myStories.forEach(story -> {
            archiveStoryModel.add(new ArchiveStoryAdapter.ArchiveStoryModel(story.id, story.url, false, false));
        });

        ArchiveStoryAdapter archiveStoryAdapter = new ArchiveStoryAdapter(archiveStoryModel, new ArchiveStoryAdapter.ArchiveStoryCallback() {
            @Override
            public void handleSelectStory(String id) {
                Log.d("-->", "handleSelectStory: " + id);
            }

            @Override
            public void handleUnselectStory(String id) {
                Log.d("-->", "handleSelectStory: " + id);
            }
        });
        storiesRecycler.setAdapter(archiveStoryAdapter);
    }
}