package com.example.murom.Recycler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murom.Firebase.Schema;
import com.example.murom.R;
import com.example.murom.State.StoryState;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.rxjava3.disposables.Disposable;

public class HighlightBottomSheetAdapter extends RecyclerView.Adapter<HighlightBottomSheetAdapter.ViewHolder> {
    private Context context;
    private final HighlightBottomSheetModel localData;

    public static class HighlightBottomSheetModel {
        private final String highlightId;
        private final String highlightName;
        private final String highlightPhoto;

        public HighlightBottomSheetModel(String highlightId, String highlightName, String highlightPhoto) {
            this.highlightId = highlightId;
            this.highlightName = highlightName;
            this.highlightPhoto = highlightPhoto;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView highlightName;
        private final ShapeableImageView highlightPhoto;
        private final RecyclerView storyListRecycler;
        private final Button selectedStoryBtn;
        private final Button allStoryBtn;
        private final Disposable storyDisposable;

        public ViewHolder(View view) {
            super(view);
            highlightName = view.findViewById(R.id.edit_highlight_name);
            highlightPhoto = view.findViewById(R.id.highlight_photo);
            storyListRecycler = view.findViewById(R.id.highlight_story_list);
            selectedStoryBtn = view.findViewById(R.id.selected_stories_btn);
            allStoryBtn = view.findViewById(R.id.all_stories_btn);
            storyDisposable = StoryState.getInstance().getObservableStoriesMap().subscribe();
        }
    }

    public HighlightBottomSheetAdapter(HighlightBottomSheetModel data) {
        localData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.context = viewGroup.getContext();
        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_highlight_bottom_sheet, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        HighlightBottomSheetModel data = localData;
        viewHolder.highlightName.setText(data.highlightName);
        Glide.with(this.context).load(data.highlightPhoto).into(viewHolder.highlightPhoto);

        StoryState.getInstance().getObservableStoriesMap().subscribe(stories -> {
            handleRenderObservableStories(stories, viewHolder);
        });

        viewHolder.selectedStoryBtn.setOnClickListener(v -> {
            Resources resources = context.getResources();
            viewHolder.selectedStoryBtn.setBackground(resources.getDrawable(R.drawable.murom_ic_underline_btn, null));
            viewHolder.allStoryBtn.setBackground(resources.getDrawable(R.color.transparent, null));
        });

        viewHolder.allStoryBtn.setOnClickListener(v -> {
            Resources resources = context.getResources();
            viewHolder.selectedStoryBtn.setBackground(resources.getDrawable(R.color.transparent, null));
            viewHolder.allStoryBtn.setBackground(resources.getDrawable(R.drawable.murom_ic_underline_btn, null));
        });

        viewHolder.storyListRecycler.setLayoutManager(new GridLayoutManager(context, 3));
    }

    @Override
    public int getItemCount() {return 1;}

    void handleRenderObservableStories(ArrayList<Schema.Story> storiesMap, ViewHolder viewHolder) {
        ArchiveStoryAdapter storyAdapter;
        ArrayList<ArchiveStoryAdapter.ArchiveStoryModel> storyModel = new ArrayList<ArchiveStoryAdapter.ArchiveStoryModel>();

        for (int i = 0; i < storiesMap.size(); i++) {
            ArchiveStoryAdapter.ArchiveStoryModel storyData = new ArchiveStoryAdapter.ArchiveStoryModel(storiesMap.get(i).id, storiesMap.get(i).url);
            storyModel.add(storyData);
        }

        storyAdapter = new ArchiveStoryAdapter(storyModel);
        viewHolder.storyListRecycler.setAdapter(storyAdapter);
    }

}
