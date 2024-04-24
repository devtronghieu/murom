package com.example.murom.Recycler;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.CheckBox;
        import android.widget.ImageButton;

        import androidx.annotation.NonNull;
        import androidx.recyclerview.widget.RecyclerView;

        import com.bumptech.glide.Glide;
        import com.example.murom.R;

        import java.util.ArrayList;

public class ArchiveStoryAdapter extends RecyclerView.Adapter<ArchiveStoryAdapter.ViewHolder> {
    private Context context;
    private  final ArrayList<ArchiveStoryModel> localDataSet;
    private final ArchiveStoryCallback callback;
    public interface ArchiveStoryCallback{
        void handleSelectStory(String id);
        void handleUnselectStory(String id);
    }
    public  static class ArchiveStoryModel{
        private final String id;
        private final String imageUrl;
        private final boolean checkboxAppear;
        private final boolean isChecked;

        public ArchiveStoryModel(String id,String imageUrl, boolean checkboxAppear, boolean isChecked){
            this.id = id;
            this.imageUrl = imageUrl;
            this.checkboxAppear = checkboxAppear;
            this.isChecked = isChecked;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageButton storyImageButton;
        private final CheckBox checkBox;
        public ViewHolder(View view){
            super(view);
            storyImageButton = view.findViewById(R.id.story_button);
            checkBox = view.findViewById(R.id.checkbox);
        }
    }

    public ArchiveStoryAdapter(ArrayList<ArchiveStoryAdapter.ArchiveStoryModel> dataSet, ArchiveStoryCallback callback){
        localDataSet = dataSet;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        this.context= viewGroup.getContext();

        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.component_archive_story, viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position){
        ArchiveStoryModel data = localDataSet.get(position);
        Glide.with(this.context)
                .load(data.imageUrl)
                .centerCrop()
                .into(viewHolder.storyImageButton);

        viewHolder.checkBox.setChecked(data.isChecked);

        if (data.checkboxAppear) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    callback.handleSelectStory(data.id);
                } else {
                    callback.handleUnselectStory(data.id);
                }
            });
        }
    }
    @Override
    public int getItemCount(){ return localDataSet.size(); }
}
