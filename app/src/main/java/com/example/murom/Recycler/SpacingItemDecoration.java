package com.example.murom.Recycler;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int horizontalSpacingInPx;
    private final int verticalSpacingInPx;

    public SpacingItemDecoration(int horizontalSpacingInPx, int verticalSpacingInPx) {
        this.horizontalSpacingInPx = horizontalSpacingInPx;
        this.verticalSpacingInPx = verticalSpacingInPx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        // Skip the first item
        if (position >= parent.getChildCount()) {
            outRect.top = verticalSpacingInPx;
        }

        // Apply horizontal spacing to all items
        outRect.left = horizontalSpacingInPx;
        outRect.right = horizontalSpacingInPx;
    }
}
