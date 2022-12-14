package jewelrock.irev.com.jewelrock.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Юрий on 27.02.2017.
 */

public class FixedAspectRatioFrameLayout extends FrameLayout{


        private int mAspectRatioWidth;
        private int mAspectRatioHeight;

        public FixedAspectRatioFrameLayout(Context context)
        {
            super(context);
        }

        public FixedAspectRatioFrameLayout(Context context, AttributeSet attrs)
        {
            super(context, attrs);

            init();
        }

        public FixedAspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);

            init();
        }

    private void init() {
        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedAspectRatioFrameLayout);

        mAspectRatioWidth = 16;//a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspectRatioWidth, 4);
        mAspectRatioHeight = 9;//a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspectRatioHeight, 3);

       // a.recycle();
    }
    // **overrides**

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);

        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedHeight = originalWidth * mAspectRatioHeight / mAspectRatioWidth;

        int finalWidth, finalHeight;

        if (calculatedHeight > originalHeight) {
            finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight;
            finalHeight = originalHeight;
        } else {
            finalWidth = originalWidth;
            finalHeight = calculatedHeight;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }

}
