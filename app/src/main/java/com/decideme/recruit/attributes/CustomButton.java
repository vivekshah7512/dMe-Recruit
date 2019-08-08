package com.decideme.recruit.attributes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.decideme.recruit.R;


public class CustomButton extends Button {


    private Context mContext;
    private String mFont;

    public CustomButton(Context context) {
        super(context, null);
        mContext = context;
        init();
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomTextview,
                0, 0);
        try {
            mFont = a.getString(R.styleable.CustomTextview_fontName);
        } finally {
            a.recycle();
        }
        init();
    }

    public void init() {
        if (mFont != null) {
            Typeface tf = null;

            if (mFont.equals("Roboto-Regular.ttf")) {
                tf = Typeface.createFromAsset(mContext.getAssets(), Constant.font_regular);
            } else if (mFont.equals("Roboto-Bold.ttf")) {
                tf = Typeface.createFromAsset(mContext.getAssets(), Constant.font_bold);
            } else if (mFont.equals("Roboto-Light.ttf")) {
                tf = Typeface.createFromAsset(mContext.getAssets(), Constant.font_light);
            }

            setTypeface(tf, Typeface.NORMAL);
        }
    }


}
