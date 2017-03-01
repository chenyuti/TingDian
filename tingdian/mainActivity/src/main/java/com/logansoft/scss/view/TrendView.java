package com.logansoft.scss.view;


import com.logansoft.UIEngine.utils.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class TrendView extends View{

	private double max= 100;
	private double curr=0;
	private Paint paint;
	private Paint textPaint;
	private Path path;
	private int viewWidth;
	private int viewHeight;
	
	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getCurr() {
		return curr;
	}

	public void setCurr(double curr) {
		this.curr = curr;
		invalidate();// 实时更新进度
	}

	public TrendView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
     paint=new Paint();
     paint.setColor(Color.parseColor("#8fc320"));
     textPaint=new Paint();
     textPaint.setColor(Color.parseColor("#000000"));
     textPaint.setTextSize(dip2px(getContext(), 14));
    }
	
	@SuppressLint("NewApi")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		viewWidth=getWidth();
		viewHeight=getHeight()-dip2px(getContext(), 15);
		int currHeight=(int) (viewHeight*curr/max);
		float textWidth = textPaint.measureText(curr+"");
		textPaint.setTextSize(dip2px(getContext(), 14));
		
		float textWidth2 = textPaint.measureText(curr+"");
		float xDis=(viewWidth-textWidth2)/2;
		if (curr!=0){
			canvas.drawText(curr+"", xDis, getHeight()-currHeight-dip2px(getContext(), 5), textPaint);
			canvas.drawRect(0+viewWidth/8,getHeight()-currHeight,viewWidth-viewWidth/8,getHeight(), paint);
		}
	
		
	}

	
	public void setPaintColor(String color){
		paint.setColor(Color.parseColor(color));
	}
	public void setTextColor(String color){
		textPaint.setColor(Color.parseColor(color));
	}
	
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	
	
}
