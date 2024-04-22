package com.example.murom;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Schema;
import com.example.murom.Recycler.PostAdapter;
import com.example.murom.Recycler.ReelsAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReelsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReelsFragment extends Fragment {
    public static ReelsFragment newInstance(String param1, String param2) {
        ReelsFragment fragment = new ReelsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reels, container, false);
        final ViewPager2 reelViewPager = rootView.findViewById(R.id.reels_viewpager);
        ArrayList<ReelsAdapter.ReelModel> reels = new ArrayList<>();

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
                                reelViewPager.setAdapter(new ReelsAdapter(reels));
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
}