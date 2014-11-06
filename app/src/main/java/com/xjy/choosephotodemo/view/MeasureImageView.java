package com.xjy.choosephotodemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MeasureImageView extends ImageView {

    public MeasureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MeasureImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);

    }
}
