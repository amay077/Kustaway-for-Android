package info.kustaway.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import info.kustaway.KustawayApplication;

public class FontelloTextView extends TextView {

    public FontelloTextView(Context context) {
        super(context);
        init();
    }

    public FontelloTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        init();
    }

    public FontelloTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            return;
        }
        init();
    }

    private void init() {
        setTypeface(KustawayApplication.getFontello());
    }
}
