package com.example.murom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;


public class StoryFragment extends Fragment {
    public interface  StoryFragmentCallback {
        void onClose();
    }

    StoryFragmentCallback callback;

    public StoryFragment(StoryFragmentCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story, container, false);

        ImageButton closeBtn = rootView.findViewById(R.id.story_fragment_close_button);
        closeBtn.setOnClickListener(v -> {
            callback.onClose();
        });

        return rootView;
    }
}