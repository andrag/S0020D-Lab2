package com.example.anders.laboration2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Anders on 2015-07-16.
 * This view is for fullscreen mode.
 *
 */
public class PropImageView extends ImageView {

    public PropImageView(Context c){
        super(c);
    }

    public PropImageView(Context c, AttributeSet a){
        super(c, a);
    }

    public PropImageView(Context c, AttributeSet a, int defStyle){
        super(c, a, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //F� tag i bilden h�r
        Drawable d = getDrawable();
        if(d != null){
            int w = MeasureSpec.getSize(widthMeasureSpec);
            int h = w * d.getIntrinsicHeight()/d.getIntrinsicWidth();
            setMeasuredDimension(w, h);
        }
        else super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
