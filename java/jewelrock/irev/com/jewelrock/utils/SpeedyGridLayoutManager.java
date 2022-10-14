package jewelrock.irev.com.jewelrock.utils;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

public class SpeedyGridLayoutManager extends GridLayoutManager {

    // scrolling speed of recyclerView.smoothScrollToPosition(position)
    private static final float MILLISECONDS_PER_INCH = 5000f; //default is 25f (bigger = slower)

    public SpeedyGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }
    public SpeedyGridLayoutManager(Context context, int spanCount,
                             @RecyclerView.Orientation int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return super.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}