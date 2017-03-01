package com.logansoft.UIEngine.Base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.StateSet;;


//要参考ColorDrawable、StateListDrawable继续改
public class UIEngineDrawable extends Drawable {
	public final static int StateNormal=0;
	public final static int StatePressed=1;
	public final static int StateSelected=2;
	public final static int StateDisabled=3;
	
	public int defaultBackgroundColor=Color.TRANSPARENT;
	public int defaultBorderColor=0xff010101;
    private Paint mFillPaint;
    private Paint mStrokePaint;

	static final private int[][]stateSets={{android.R.attr.state_enabled},{android.R.attr.state_pressed},
										   {android.R.attr.state_selected},{-android.R.attr.state_enabled}};
	private int[] stateSetCache;
	private Bitmap[] stateBitmap=new Bitmap[4];
	private String[] stateImageURL=new String[4];
	private Object[] stateDrawableRes=new Object[4];
	private int state;
	private int cornerRadius=0;
	private float borderWidth=0;
	private int borderColor=-2;
    private int alpha=255;
    static DisplayMetrics matrics;
    
   
	public UIEngineDrawable(Context context){
		super();
		matrics=context.getResources().getDisplayMetrics();
	}
	public UIEngineDrawable(UIEngineDrawable drawable){
		this.matrics=drawable.matrics;
		this.state=drawable.state;
		this.cornerRadius=drawable.cornerRadius;
		this.borderColor=drawable.borderColor;
		this.borderWidth=drawable.borderWidth;
		this.alpha=drawable.alpha;
		this.stateImageURL=drawable.stateImageURL.clone();
		this.stateBitmap=drawable.stateBitmap.clone();
		this.defaultBackgroundColor=drawable.defaultBackgroundColor;
		this.defaultBorderColor=drawable.defaultBorderColor;
		this.stateDrawableRes=drawable.stateDrawableRes.clone();
	}
	@Override
	public boolean isStateful() {
		return true;
	}
	public Object getDrawableResForState(int state){
		if(state>3 || state<0)
			return null;
		else
			return stateDrawableRes[state];
	}
	public void setDrawableResForState(int state,Object drawableRes){
		if(state>3 || state<0)
			return;
		if(drawableRes instanceof Bitmap){
			Bitmap bitmap=(Bitmap) drawableRes;
			stateBitmap[state]=bitmap;
			drawableRes=new BitmapShader(bitmap,TileMode.CLAMP,TileMode.CLAMP);
		}
		else{
			stateBitmap[state]=null;
			stateImageURL[state]=null;
		}
		
		if(stateDrawableRes[state]!=drawableRes){
			stateDrawableRes[state]=drawableRes;
			if(stateSetCache!=null)
				onStateChange(stateSetCache);
        	invalidateSelf();	
        }
	}
	public void setDrawableForState(int state,Drawable drawable){setDrawableResForState(state,drawable);}
	public void setColorForState(int state,Integer Color){setDrawableResForState(state,Color);}
	public void setBitmapForState(int state,Bitmap bitmap){setDrawableResForState(state,bitmap);}
	
	public void setImageURLForState(int state,String ImageURLString){
		stateImageURL[state]=ImageURLString;
	}
	public String getImageURLForState(int state){
		return stateImageURL[state];
	}

	public void setCornerRadius(String cornerRadiusString,Context context){
		if(cornerRadiusString==null || cornerRadiusString.length()==0)
			cornerRadius=0;
		else
			cornerRadius=UIEngineGroupView.UIEngineLayoutParams.parseSingleMarginOrPaddingString(cornerRadiusString, context, 0);
	}
	public int getCornerRadius(){return cornerRadius;}
	
	public void setBorderWidth(String borderWidhtString,Context context){
		if(borderWidhtString==null || borderWidhtString.length()==0)
			borderWidth=0;
		else
			borderWidth=(context.getResources().getDisplayMetrics().density)*(Float.parseFloat(borderWidhtString));
	}
	
	public float getBorderWidth(){return borderWidth;}
	
	public void setBorderColorString(String borderWidthString){
		borderColor=UIEngineColorParser.getColor(borderWidthString);
	}
	public int getBorderColor(){return borderColor;}
	
	@Override
    protected boolean onStateChange(int[] stateSet) {
		stateSetCache=stateSet;
		for(int state=stateSets.length-1;state>=0;state--){
			if(StateSet.stateSetMatches(stateSets[state], stateSet)){
				if(state==this.state)
					return false;
				if(state!=StateNormal && (stateDrawableRes[state]==null || stateDrawableRes[state]==(Integer)(-2)))
					return false;
				this.state=state;
		        invalidateSelf();
				return true;
			}
		}			
		return super.onStateChange(stateSet);
	}

	@Override
	public void draw(Canvas canvas) {
		mFillPaint=new Paint();
		
		Object drawableRes=stateDrawableRes[state];
		if(state==StateNormal &&(drawableRes==null || drawableRes==(Integer)(-2)))
			drawableRes=(Integer)defaultBackgroundColor;

		
		Rect r=new Rect(getBounds());
		RectF rf=new RectF(r);
		float bw=borderWidth/2;
    	if(bw>0){
    		r.left+=bw;r.top+=bw;r.right-=bw;r.bottom-=bw;
    		rf.left+=bw;rf.top+=bw;rf.right-=bw;rf.bottom-=bw;
    	}
		if(drawableRes instanceof Drawable){
			Drawable drawable=(Drawable)drawableRes;
			drawable.setAlpha(alpha);
			drawable.setBounds(r);
			drawable.draw(canvas);
		}
		else{
			if(drawableRes instanceof BitmapShader){
				mFillPaint.setAntiAlias(true);  
				Matrix matrix=new Matrix();
				float drawWidth=r.width();
				float drawHeight=r.height();
				matrix.setScale(drawWidth/(float)stateBitmap[state].getScaledWidth(matrics),
	    			drawHeight/(float)stateBitmap[state].getScaledHeight(matrics));
				((BitmapShader)drawableRes).setLocalMatrix(matrix);
				mFillPaint.setShader((BitmapShader)drawableRes); 
			}
			else if(drawableRes instanceof Integer){
				int color=(Integer) drawableRes;
				mFillPaint.setAntiAlias(false);
				if(color==-2)
					color=defaultBackgroundColor;
				if(alpha!=255){
					int oa=color>>>24;
					oa=oa*alpha>>8;
					color=(color<< 8 >>> 8)|(oa<<24);
				}
				mFillPaint.setColor(color);
			}	
			if(cornerRadius==0)
				canvas.drawRect(r,mFillPaint);
			else{
				mFillPaint.setAntiAlias(true);  
				canvas.drawRoundRect(rf,cornerRadius,cornerRadius, mFillPaint);  
			}
		}
	
        if(borderWidth>0){
        	mStrokePaint=new Paint();
        	mStrokePaint.setAlpha(alpha);
        	mStrokePaint.setStyle(Paint.Style.STROKE);
        	mStrokePaint.setAntiAlias(true);  
        	
        	mStrokePaint.setStrokeWidth(borderWidth);
        	if(borderColor!=-2)
        		mStrokePaint.setColor(borderColor);
        	else
        		mStrokePaint.setColor(defaultBorderColor);
        	
        	if(cornerRadius==0)
        		canvas.drawRect(r,mStrokePaint);
        	else
            	canvas.drawRoundRect(rf,cornerRadius,cornerRadius, mStrokePaint);  
        }
        else
        	mStrokePaint=null;
	}
	
	public void setAlphaString(String alphaString){
		int tempalpha=255;
		if (alphaString!=null && alphaString.length()!=0) {
            float alphaF = Float.parseFloat(alphaString);
            tempalpha=(int)(alphaF*255);
        }
		setAlpha(tempalpha);
	}
	@Override
	public void setAlpha(int alpha) {
		alpha=alpha>255?255:alpha;
		alpha=alpha<0?0:alpha;
		if(this.alpha!=alpha){
			this.alpha=alpha;
	        invalidateSelf();
		}
	}
	public int getAlpha(){return alpha;}
	public int getALPHA(){return alpha;}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getOpacity() {
		Object drawableRes=stateDrawableRes[state];
		if(drawableRes instanceof Integer){
			Integer Color=(Integer)drawableRes;
			switch (Color >>> 24) {
	          case 255:
	              return PixelFormat.OPAQUE;
	          case 0:
	              return PixelFormat.TRANSPARENT;
	          default:return PixelFormat.TRANSLUCENT;
			}
		}
		if(alpha==255)
             return PixelFormat.OPAQUE;
        if(alpha==0)
             return PixelFormat.TRANSPARENT;
        return PixelFormat.TRANSLUCENT;
	}
	
	@Override
	public int getIntrinsicWidth(){
		Object drawableRes=stateDrawableRes[state];
		if(drawableRes!=null && drawableRes instanceof Drawable )
			return ((Drawable)drawableRes).getIntrinsicWidth();
		if(stateBitmap[state]!=null){
			return stateBitmap[state].getScaledWidth(matrics); 
		}
		return super.getIntrinsicWidth();
	}

	@Override
	public int getIntrinsicHeight(){
		Object drawableRes=stateDrawableRes[state];
		if(drawableRes!=null && drawableRes instanceof Drawable )
			return ((Drawable)drawableRes).getIntrinsicHeight();
		if(stateBitmap[state]!=null){
			return stateBitmap[state].getScaledHeight(matrics);
		}
		return super.getIntrinsicHeight();
	}
	  
}
