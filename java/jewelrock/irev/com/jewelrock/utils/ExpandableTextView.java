package jewelrock.irev.com.jewelrock.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import jewelrock.irev.com.jewelrock.R;

public class ExpandableTextView extends android.support.v7.widget.AppCompatTextView {
    private static final int DEFAULT_TRIM_LENGTH = 130;
    private static final String ELLIPSIS = "     еще...";

    private CharSequence originalText;
    private CharSequence trimmedText;
    private BufferType bufferType;
    private boolean trim = true;
    private int trimLength;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        this.trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH);
        typedArray.recycle();
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
    }

    private CharSequence getDisplayableText() {
        return trim ? trimmedText : originalText;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        trimmedText = getTrimmedText();
        bufferType = type;
        setText();
    }

    private CharSequence getTrimmedText() {
        if (originalText != null && originalText.length() > trimLength) {
            return new SpannableStringBuilder(originalText, 0, trimLength + 1).append(ELLIPSIS);
        } else {
            return originalText;
        }
    }

    public void callOnClick(TextView collapsText) {
        trim = !trim;
        setText();
        requestFocusFromTouch();
        if (originalText.length() > trimLength)
            collapsText.setVisibility(collapsText.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
}
