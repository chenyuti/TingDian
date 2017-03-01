
package com.logansoft.UIEngine.view;

import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.utils.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class RotateImage extends ImageView {

    private boolean isLoading = false;

    private AnimationDrawable animationDrawable;

    private ScaleType scaleType;

    public RotateImage(Context context) {
        super(context);
    }

    public RotateImage(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setLoading(boolean isLoading) {
    	if( this.isLoading==isLoading)
    		return;
        this.isLoading = isLoading;
        if (isLoading) {
        	scaleType= getScaleType();
        	setScaleType(ScaleType.CENTER_INSIDE);
        	setImageResource(R.drawable.loading);
        	animationDrawable = (AnimationDrawable) getDrawable();
        	animationDrawable.start();
        }else {
            if(animationDrawable!=null){
            	setScaleType(scaleType);
            	animationDrawable.stop();
                setImageDrawable(null);
                animationDrawable=null;
            }
        }
    }
}
