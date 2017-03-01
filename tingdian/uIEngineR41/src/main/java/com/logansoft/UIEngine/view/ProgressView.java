package com.logansoft.UIEngine.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ProgressView extends View {
	/** 背景色 */
	private int bgColor=Color.GRAY;
	/** 边框色 */
	private int borderColor=Color.GRAY;
	/** 前景色 */
	private int fgColor=Color.GRAY;
	/** 进度条最大值 */
	private float maxCount;
	/** 进度条当前值 */
	private float currentCount;
	/** 画笔 */
	private Paint mPaint;
	private int mWidth, mHeight;

	public enum COLOR {
		RED("red", Color.RED), BLACK("black", Color.BLACK), WHITE("white",
				Color.WHITE), YELLOW("yellow", Color.YELLOW), BLUE("blue",
				Color.BLUE), GREEN("green", Color.GREEN), MAGENTA("magenta",
				Color.MAGENTA), GRAY("gray", Color.GRAY), LTGRAY("ltgray",
				Color.LTGRAY), CYAN("cyan", Color.CYAN), CLEAR("clear",
				Color.TRANSPARENT);

		private String colorStr;

		private int colorInt;

		private COLOR(String color, int c) {
			colorStr = color;
			colorInt = c;
		}

		public String getColorStr() {
			return colorStr;
		}

		public int getColor() {
			return colorInt;
		}

		/**
		 * 通过颜色名获取颜色值
		 * 
		 * @param color
		 * @return
		 */
		public static int getColor(String color) {
			int c1 = -2;
			for (COLOR c : COLOR.values()) {
				if (c.getColorStr().equalsIgnoreCase(color)) {
					return c1 = c.getColor();
				}
			}
			if (c1 == -2) {
				try {
					c1 = Color.parseColor(color);
				} catch (Exception e) {
				}
			}
			return c1;
		}

	}

	public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ProgressView(Context context,String bgColor,String borderColor,String fgColor,String maxCount,String currentCount) {
		super(context);
		initView(context,bgColor,borderColor,fgColor);
		this.currentCount=Float.parseFloat(currentCount);
		this.maxCount=Float.parseFloat(maxCount);
	}
	public ProgressView(Context context,String bgColor,String borderColor,String fgColor) {
		super(context);
		initView(context,bgColor,borderColor,fgColor);
	}

	private void initView(Context context,String bgColor,String borderColor,String fgColor) {
		this.bgColor=COLOR.getColor(bgColor);
		this.borderColor=COLOR.getColor(borderColor);
		this.fgColor=COLOR.getColor(fgColor);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		int round = mHeight / 2;
		mPaint.setColor(borderColor);
		RectF rectBg = new RectF(0, 0, mWidth, mHeight);
		canvas.drawRoundRect(rectBg, round, round, mPaint);
		mPaint.setColor(bgColor);
		RectF rectBlackBg = new RectF(2, 2, mWidth - 2, mHeight - 2);
		canvas.drawRoundRect(rectBlackBg, round, round, mPaint);
		float section = currentCount / maxCount;
		RectF rectProgressBg = new RectF(3, 3, (mWidth - 3) * section,
				mHeight - 3);
		mPaint.setColor(fgColor);
		canvas.drawRoundRect(rectProgressBg, round, round, mPaint);
	}

	private int dipToPx(int dip) {
		float scale = getContext().getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
	}

	/***
	 * 设置最大的进度值
	 * 
	 * @param maxCount
	 */
	public void setMaxCount(float maxCount) {
		this.maxCount = maxCount;
	}

	/***
	 * 设置当前的进度值
	 * 
	 * @param currentCount
	 */
	public void setCurrentCount(float currentCount) {
		this.currentCount = currentCount > maxCount ? maxCount : currentCount;
		invalidate();
	}

	public float getMaxCount() {
		return maxCount;
	}

	public float getCurrentCount() {
		return currentCount;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		if (widthSpecMode == MeasureSpec.EXACTLY
				|| widthSpecMode == MeasureSpec.AT_MOST) {
			mWidth = widthSpecSize;
		} else {
			mWidth = 0;
		}
		if (heightSpecMode == MeasureSpec.AT_MOST
				|| heightSpecMode == MeasureSpec.UNSPECIFIED) {
			mHeight = dipToPx(15);
		} else {
			mHeight = heightSpecSize;
		}
		setMeasuredDimension(mWidth, mHeight);
	}
}
