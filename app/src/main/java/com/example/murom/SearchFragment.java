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
        TextView popularPosts = rootView.findViewById(R.id.popular_posts);
        RecyclerView resultRecycler = rootView.findViewById(R.id.result_recycler);

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

    private void searchKeyword(String query, TextView keyword, TextView postsCount, RecyclerView postImageRecycler) {
        Random rand = new Random();

        keyword.setText(query);

        ArrayList<PostImageAdapter.PostImageModel> search_result = new ArrayList<>();

        for (int i = 0; i < rand.nextInt(5) + 5; i++) {
            search_result.add(new PostImageAdapter.PostImageModel(
                    "https://picsum.photos/200"
            ));
        }
        PostImageAdapter postImageAdapter = new PostImageAdapter(search_result);
        postImageRecycler.setAdapter(postImageAdapter);
        postsCount.setText(MessageFormat.format("{0} posts", search_result.size()));
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