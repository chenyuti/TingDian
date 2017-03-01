package com.logansoft.UIEngine.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class BorderScrollView  extends ScrollView {
    private OnBorderListener onBorderListener;
    private View             contentView;
    
    public BorderScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public BorderScrollView (Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public BorderScrollView (Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    
    
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // TODO Auto-generated method stub
        super.onScrollChanged(l, t, oldl, oldt);
        doOnBorderListener();
    }
    
    public void setOnBorderListener(final OnBorderListener onBorderListener) {
        this.onBorderListener = onBorderListener;
        if (onBorderListener == null) {
            return;
        }

        if (contentView == null) {
            contentView = getChildAt(0);
        }
    }

    /**
     * OnBorderListener, Called when scroll to top or bottom
     * 
     * @author  Trinea 2013-5-22
     */
    public static interface OnBorderListener {

        /**
         * Called when scroll to bottom
         */
        public void onBottom();

        /**
         * Called when scroll to top
         */
        public void onTop();
    }

    private void doOnBorderListener() {
        if (contentView != null && contentView.getMeasuredHeight() <= getScrollY() + getHeight()) {
            if (onBorderListener != null) {
                onBorderListener.onBottom();
            }
        } else if (getScrollY() == 0) {
            if (onBorderListener != null) {
                onBorderListener.onTop();
            }
        }
    }
}
