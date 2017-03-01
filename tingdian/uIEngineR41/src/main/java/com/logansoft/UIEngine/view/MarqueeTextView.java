package com.logansoft.UIEngine.view;

import com.logansoft.UIEngine.utils.LogUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView {

    /** 是否停止滚动 */
    private boolean mStopMarquee;
    private String mText;
    private float mCoordinateX;
    private float mCoordinateX1;
    private float mTextWidth;
    private float textSize;
    
    public MarqueeTextView (Context context){
        super(context);
    }
    
    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setText(String text) {
        this.mText = text;
        mTextWidth = getPaint().measureText(mText);
         textSize = getPaint().getTextSize();
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 0);
    }
    
//    @Override
//    public void setText(CharSequence text, BufferType type) {
//        super.setText(text, type);
//        this.mText =text.toString();
//        mTextWidth = getPaint().measureText(mText);
//        if (mHandler.hasMessages(0))
//            mHandler.removeMessages(0);
//        mHandler.sendEmptyMessageDelayed(0, 0);
//    }
    
    @Override
    protected void onAttachedToWindow() {
        mStopMarquee = false;
        if (!TextUtils.isEmpty(mText))
            mHandler.sendEmptyMessageDelayed(0, 0);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mStopMarquee = true;
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
        super.onDetachedFromWindow();
    }
    
    public void startMarquee() {
        mHandler.hasMessages(0);
    }
    public void stopMarquee() {
        mHandler.removeMessages(0);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!TextUtils.isEmpty(mText)){
            canvas.drawText(mText, mCoordinateX, textSize, getPaint());
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 0:
                if (Math.abs(mCoordinateX) > mTextWidth) {
                     int w = getWidth();
                    mCoordinateX = w-(++mCoordinateX1);
                    invalidate();
                    if (!mStopMarquee) {
                        sendEmptyMessageDelayed(0, 30);
                    }
                } else {
                    mCoordinateX1 =0;
                    mCoordinateX -= 1;
                    invalidate();
                    if (!mStopMarquee) {
                        sendEmptyMessageDelayed(0, 30);
                    }
                }

                break;
            }
            super.handleMessage(msg);
        }
    };

}
