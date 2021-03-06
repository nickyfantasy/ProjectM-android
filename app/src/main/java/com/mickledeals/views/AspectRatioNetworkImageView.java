package com.mickledeals.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.mickledeals.R;

/**
 * Created by Nicky on 12/11/2014.
 */
public class AspectRatioNetworkImageView extends com.android.volley.toolbox.NetworkImageView {

    private float mRatio = 9f / 14;//0.6428

    public AspectRatioNetworkImageView(Context context) {
        super(context);
    }

    public AspectRatioNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.AspectRatioNetworkImageView, 0, 0);
        mRatio = a.getFloat(R.styleable.AspectRatioNetworkImageView_ratio, mRatio);
        a.recycle();
    }

    public AspectRatioNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * mRatio)); //Snap to width
    }

    public void setRatio(float ratio) {
        mRatio = ratio;
    }

    public float getRatio() {
        return mRatio;
    }
}