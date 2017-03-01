package com.logansoft.UIEngine.parse.xmlview;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineColorParser;
import com.logansoft.UIEngine.fragment.BaseFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.view.View;

public class MLine extends BaseView {
    static final String STYLE_LINE="line";
    static final String STYLE_DASH="dash";
    static final String STYLE="lineStyle";
    private String lineStyle =STYLE_LINE;
    public MLine(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
    	mView = new Line(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
    }
    @Override
	protected void parseView() {
    	super.parseView();
        lineStyle = attrMap.get(STYLE);
		String lineColor=attrMap.get("lineColor");
        if(!TextUtils.isEmpty(lineColor))
        	attrMap.put(ATTR_BACKGROUND, lineColor);
        if (STYLE_DASH.equals(lineStyle)) {
		    lineStyle = STYLE_DASH;
//		    GradientDrawable gd=new GradientDrawable();
//		    gd.setShape(GradientDrawable.LINE);
//		    String background=attrMap.get("background");
//            gd.setStroke(DisplayUtil.dip2px(mContext, 1f), COLOR.getColor(background), DisplayUtil.dip2px(mContext,2), DisplayUtil.dip2px(mContext, 2));
//		    mView.setBackgroundDrawable(gd);
		    mView.setBackgroundDrawable(null);
		}else {
		    lineStyle = STYLE_LINE;
		}
	}
    
    class Line extends View{
        private Paint p;
        private DashPathEffect effects;

        @SuppressLint("NewApi")
        public Line(Context context) {
            super(context);
            if(STYLE_DASH.equals(lineStyle)){
                if(VERSION_CODES.HONEYCOMB<=VERSION.SDK_INT){
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                 p = new Paint(Paint.ANTI_ALIAS_FLAG);  
                 p.setStyle(Style.STROKE);  
                 String background=attrMap.get("background");
                 effects = new DashPathEffect(new float[] {4, 4, 4, 4}, 1);  
                 String lineColor=attrMap.get("lineColor");
                 if(!TextUtils.isEmpty(lineColor)){
                	  background=lineColor;
                 }
                 p.setColor(UIEngineColorParser.getColor(background)); 
            }
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            lineStyle = attrMap.get(STYLE);
            if(STYLE_DASH.equals(lineStyle)){
                p.setPathEffect(effects);  
               p.setStrokeWidth(getHeight());  
               canvas.drawLine(0, 0, getWidth(), 0, p); 
            }else {
                super.onDraw(canvas);
            }
        }
        
    }


   
}
