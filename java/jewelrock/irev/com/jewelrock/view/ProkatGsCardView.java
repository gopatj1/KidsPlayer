package jewelrock.irev.com.jewelrock.view;

import android.content.Context;
import android.util.AttributeSet;

import carbon.widget.FrameLayout;

public class ProkatGsCardView extends FrameLayout {

    private int mAspectRatioWidth;
    private int mAspectRatioHeight;

    public ProkatGsCardView(Context context)
    {
        super(context);
    }

    public ProkatGsCardView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    public ProkatGsCardView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedAspectRatioFrameLayout);

        mAspectRatioWidth = 5;//a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspectRatioWidth, 4);
        mAspectRatioHeight = 7;//a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspectRatioHeight, 3);

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