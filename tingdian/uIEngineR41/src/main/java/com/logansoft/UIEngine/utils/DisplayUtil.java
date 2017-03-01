package com.logansoft.UIEngine.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * dp、sp 转换为 px 的工具类
 * 
 * @author fxsky 2012.11.12
 *
 */
public class DisplayUtil {
	/**
	 * 将px值转换为dip或dp值，保证尺寸大小不变
	 * 
	 * @param pxValue
	 * @param scale
	 *            （DisplayMetrics类中属性density）
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将dip或dp值转换为px值，保证尺寸大小不变
	 * 
	 * @param dipValue
	 * @param scale
	 *            （DisplayMetrics类中属性density）
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
//	  return  (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context
//                .getResources().getDisplayMetrics());
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 * 
	 * @param pxValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 * 
	 * @param spValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
	/**
	 * 获取当前分辨率下指定单位对应的像素大小（根据设备信息）
	 * px,dip,sp -> px
	 * 
	 * Paint.setTextSize()单位为px
	 * 
	 * 代码摘自：TextView.setTextSize()
	 * 
	 * @param unit  TypedValue.COMPLEX_UNIT_*
	 * @param size
	 * @return
	 */
	public static float getRawSize(Context context,int unit, float size) {
	       Context c = context;
	       Resources r;

	       if (c == null)
	           r = Resources.getSystem();
	       else
	           r = c.getResources();
	        
	       return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
	}
}