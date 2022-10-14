package jewelrock.irev.com.jewelrock.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import carbon.widget.RecyclerView;

public class MyRecyclerView extends RecyclerView {
    public MyRecyclerView(Context context) {
        super(context);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean requestCancelDisallowInterceptTouchEvent = getScrollState() == SCROLL_STATE_SETTLING;
        boolean consumed = super.onInterceptTouchEvent(e);
        final int action = e.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            if (requestCancelDisallowInterceptTouchEvent) {
                getParent().requestDisallowInterceptTouchEvent(false);
                // stop scroll to enable child view get the touch event
                stopScroll();
                // not consume the event
                return false;
            }
        }

        return consumed;
    }
}