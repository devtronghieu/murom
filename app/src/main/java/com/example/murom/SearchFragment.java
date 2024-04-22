package com.example.murom;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Recycler.GridSpacingItemDecoration;
import com.example.murom.Recycler.PostImageAdapter;
import com.example.murom.Recycler.SearchUserAdapter;
import com.example.murom.State.ProfileState;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements SearchUserAdapter.OnUserItemClickListener {
    TextView popularPosts;
    private ArrayList<PostImageAdapter.PostImageModel> previousPostSearchResult;
    private ArrayList<Schema.SearchUser> previousUserSearchResult;
    private String previousSearchQuery;
    public interface SearchFragmentCallback {
        void onSearchUserItemClick(String userId);
    }

    SearchFragmentCallback callback;
    public SearchFragment(SearchFragmentCallback callback) {
        this.callback = callback;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        EditText searchEditText = rootView.findViewById(R.id.searchEditText);
        TextView keyword = rootView.findViewById(R.id.keyword);
        TextView postsCount = rootView.findViewById(R.id.posts_count);
        popularPosts = rootView.findViewById(R.id.popular_posts);
        RecyclerView resultRecycler = rootView.findViewById(R.id.result_recycler);

        if (previousPostSearchResult != null) {
            PostImageAdapter postImageAdapter = new PostImageAdapter(previousPostSearchResult);
            resultRecycler.setAdapter(postImageAdapter);
            keyword.setText(previousSearchQuery);
            postsCount.setText(MessageFormat.format("{0} posts", previousPostSearchResult.size()));
            popularPosts.setText("Popular Posts");
            resultRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        } else if (previousUserSearchResult != null) {
            SearchUserAdapter searchUserAdapter = new SearchUserAdapter(previousUserSearchResult, userId -> {
                onSearchUserItemClick(userId);
            });
            resultRecycler.setAdapter(searchUserAdapter);
            keyword.setText(previousSearchQuery);
            postsCount.setText(MessageFormat.format("{0} accounts", previousUserSearchResult.size()));
            resultRecycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
            popularPosts.setText("Most Related Accounts");
        }
        resultRecycler.addItemDecoration(new GridSpacingItemDecoration(5));
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = searchEditText.getText().toString();
                    if (query.startsWith("#")) {
                        resultRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
                        searchKeyword(query, keyword, postsCount, resultRecycler);
                        popularPosts.setText("Popular Posts");
                    }
                    else {
                        resultRecycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
                        searchUsername(query, keyword, postsCount, resultRecycler);
                        popularPosts.setText("Most Related Accounts");
                    }
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert in != null;
                    in.hideSoftInputFromWindow(searchEditText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true; // Consume the event
                }
                return false; // Let the system handle other actions
            }

        });
        return rootView;
    }

    private void searchKeyword(String hashtag, TextView keyword, TextView postsCount, RecyclerView postImageRecycler) {
        keyword.setText(hashtag);
        Database.searchPostByHashtag(hashtag, new Database.OnSearchPostCompleteListener() {
            @Override
            public void onSearchPostComplete(ArrayList<PostImageAdapter.PostImageModel> searchResult) {
                PostImageAdapter postImageAdapter = new PostImageAdapter(searchResult);
                postImageRecycler.setAdapter(postImageAdapter);
                postsCount.setText(MessageFormat.format("{0} posts", searchResult.size()));
                previousPostSearchResult = searchResult;
                previousSearchQuery = hashtag;
                Log.d("-->", "Search complete with " + searchResult.size() + " results");
            }

            @Override
            public void onSearchPostFailed(String errorMessage) {
                postImageRecycler.setAdapter(null);
                Log.d("-->", "Error searching post" + errorMessage);
            }

            @Override
            public void onNoPostFound() {
                postsCount.setText(MessageFormat.format("0 posts", 0));
                postImageRecycler.setAdapter(null);
                popularPosts.setVisibility(View.GONE);
            }
        });

    }

    private void searchUsername(String query, TextView keyword, TextView postsCount, RecyclerView userRecycler) {
        keyword.setText(query);
        Database.searchUser(query, new Database.OnSearchUserCompleteListener() {
            @Override
            public void onSearchUserComplete(ArrayList<Schema.SearchUser> searchResult) {
                SearchUserAdapter searchUserAdapter = new SearchUserAdapter(searchResult, userId -> {
                    onSearchUserItemClick(userId);
                });
                userRecycler.setAdapter(searchUserAdapter);
                postsCount.setText(MessageFormat.format("{0} accounts", searchResult.size()));
                previousUserSearchResult = searchResult;
                previousSearchQuery = query;
            }

            @Override
            public void onSearchUserFailed(String errorMessage) {
                Log.d("-->", "Error searching user" + errorMessage);
            }
        });

    }

    @Override
    public void onSearchUserItemClick(String userId) {
        if (callback != null) {
            callback.onSearchUserItemClick(userId);
        }
        Log.d("-->", "Click from search fragment: " + userId);
    }
}