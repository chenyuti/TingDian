package com.logansoft.scss.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class PirChartView extends View {
	private Context context;
	private Paint paint1, paint2, paint3, paint4;
	private static final int PROGRESS = 0X0008;
	private static final int RESTART = 0X0009;
	
	private int maxProgress = 100;
	private int currentProgress = 0;
	private float per1, per2, per3, per4;

	public PirChartView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.paint1 = new Paint();
		this.paint1.setAntiAlias(true); // 消除锯齿
		this.paint1.setStyle(Paint.Style.STROKE); // 绘制空心圆
		paint1.setColor(Color.parseColor("#C00280"));
		paint2 = new Paint();
		paint2.setAntiAlias(true); // 消除锯齿
		paint2.setStyle(Paint.Style.STROKE); // 绘制空心圆
		paint2.setColor(Color.parseColor("#FFA717"));

		paint3 = new Paint();
		paint3.setAntiAlias(true); // 消除锯齿
		paint3.setStyle(Paint.Style.STROKE); // 绘制空心圆
		paint3.setColor(Color.parseColor("#C9E401"));

		paint4 = new Paint();
		paint4.setAntiAlias(true); // 消除锯齿
		paint4.setStyle(Paint.Style.STROKE); // 绘制空心圆
		paint4.setColor(Color.parseColor("#57CAFF"));
        
        per1 = 0;
		per2 = 0;
		per3 = 0;
		per4 = 0;
	}

	public void setPer(String per1,String per2,String per3,String per4){
		this.per1=Float.parseFloat(per1);
		this.per2=Float.parseFloat(per2);
		this.per3=Float.parseFloat(per3);
		this.per4=Float.parseFloat(per4);
		if (currentProgress!=0){
			if (currentProgress==maxProgress){
				setCurrentProgress(0);
				handler.sendEmptyMessageDelayed(PROGRESS, 50);
			}else{
				setCurrentProgress(maxProgress);
				handler.sendEmptyMessageDelayed(RESTART, 500);
				handler.sendEmptyMessageDelayed(PROGRESS, 1000);
			}
		}else{
			handler.sendEmptyMessageDelayed(PROGRESS, 50);
		}
		
	}
	
	public int getMaxProgress() {
		return maxProgress;
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	public int getCurrentProgress() {
		return currentProgress;
	}

	public void setCurrentProgress(int currentProgress) {
		this.currentProgress = currentProgress;
		invalidate();// 实时更新进度
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (currentProgress==0){
			return;
		}
		int center = getWidth() / 2;
		int ringWidth = center*4/5;
		paint1.setStrokeWidth(center/5);
        paint2.setStrokeWidth(center/5);
        paint3.setStrokeWidth(center/5);
        paint4.setStrokeWidth(center/5);
		
		float per = (int) (((float) currentProgress / (float) maxProgress) * 100);
		RectF oval = new RectF(center - ringWidth, center - ringWidth, center
				+ ringWidth, center + ringWidth);
		if (per < per1) {
			canvas.drawArc(oval, 270, 360 * currentProgress / maxProgress,
					false, paint1); 
		} else if (per < (per1 + per2)) {
			canvas.drawArc(oval, 270, 360 * per1 / 100, false, paint1); 
			canvas.drawArc(oval, 360 * per1 / 100 - 90, 360 * (per - per1) / 100, false, paint2);
		} else if (per < (per1 + per2 + per3)) {
			canvas.drawArc(oval, 270, 360 * per1 / 100, false, paint1); 
			canvas.drawArc(oval, 360 * per1 / 100 - 90, 360 * per2 / 100,
					false, paint2); 
			canvas.drawArc(oval, 360 * (per1 + per2) / 100 - 90, 360 * (per
					- per1 - per2) / 100, false, paint3); 
		} else if (per < (per1 + per2 + per3 + per4)) {
			canvas.drawArc(oval, 270, 360 * per1 / 100, false, paint1); 
			canvas.drawArc(oval, 360 * per1 / 100 - 90, 360 * per2 / 100,
					false, paint2); 
			canvas.drawArc(oval, 360 * (per1 + per2) / 100 - 90,
					360 * per3 / 100, false, paint3); 
			canvas.drawArc(oval, 360 * (per1 + per2 + per3) / 100 - 90,
					360 * (per - per1 - per2 - per3) / 100, false, paint4); 
		} else if (per == 100) {
			canvas.drawArc(oval, 270, 360 * per1 / 100, false, paint1); 
			canvas.drawArc(oval, 360 * per1 / 100 - 90, 360 * per2 / 100,
					false, paint2); 
			canvas.drawArc(oval, 360 * (per1 + per2) / 100 - 90,
					360 * per3 / 100, false, paint3); 
			canvas.drawArc(oval, 360 * (per1 + per2 + per3) / 100 - 90,
					360 * per4 / 100, false, paint4); 
		}

		// super.onDraw(canvas);
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case PROGRESS:
				currentProgress += 5;
				if (currentProgress < 100) {
					sendEmptyMessageDelayed(PROGRESS, 50);
				}
				invalidate();
				break;
			case RESTART:
				currentProgress = 0;
				break;
			}
		}
	};
}
