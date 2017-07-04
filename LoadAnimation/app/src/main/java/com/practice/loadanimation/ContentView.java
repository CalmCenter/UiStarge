package com.practice.loadanimation;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;

/**
 * Created by Administrator on 2017/6/15.
 */

public class ContentView extends AppCompatImageView {

    public ContentView(Context context) {
        super(context);
        setScaleType(ScaleType.FIT_XY);
        setImageResource(R.mipmap.content);
    }

}
