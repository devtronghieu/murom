package com.example.murom.Recycler;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;

        import androidx.annotation.NonNull;
        import androidx.recyclerview.widget.RecyclerView;

        import com.bumptech.glide.Glide;
        import com.example.murom.R;

        import java.util.ArrayList;

public class ArchiveStoryAdapter extends RecyclerView.Adapter<ArchiveStoryAdapter.ViewHolder> {
    private Context context;
    private  final ArrayList<ArchiveStoryModel> localDataSet;
    public  static class ArchiveStoryModel{
        private final String id;
        private final String imageUrl;

        public ArchiveStoryModel(String id,String imageUrl){
            this.id = id;
            this.imageUrl = imageUrl;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageButton storyImageButton;
        public ViewHolder(View view){
            super(view);
            storyImageButton = view.findViewById(R.id.story_button);
        }
    }

    public ArchiveStoryAdapter(ArrayList<ArchiveStoryAdapter.ArchiveStoryModel> dataSet){
        localDataSet = dataSet;
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
    }
    @Override
    public int getItemCount(){ return localDataSet.size(); }
}
