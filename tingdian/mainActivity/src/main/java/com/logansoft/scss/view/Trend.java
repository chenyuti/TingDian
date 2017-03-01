package com.logansoft.scss.view;

import java.text.DecimalFormat;

import org.w3c.dom.Element;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;

public class Trend extends BaseView {
	private double progress;
	private double MaxProgress;
	private double currProgress;
	private DecimalFormat df;
	private static final int PROGRESS= 0X0003;

	public Trend(BaseFragment fragment, GroupView parentView, Element element) {
		 super(fragment, parentView, element);
		 // TODO Auto-generated constructor stub
	}
	 
    protected void createMyView(){
    	mView = new TrendView(mContext){
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
		// TODO Auto-generated method stub
		super.parseView();
		df= new DecimalFormat("######0.00");   
	}

	public void updateView(Object dataMap) {
		// TODO Auto-generated method stub
	}
	
	private Handler handler = new Handler() {
		@Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:
                    progress=progress+MaxProgress/50;
                    if (progress <= currProgress) {
                    	if(mView!=null) {
                    		((TrendView)mView).setCurr(Double.parseDouble(df.format(progress)));
    	            	 	sendEmptyMessageDelayed(PROGRESS, 50);
                    	}
                    } else {
                     	if (mView!=null) {
                     	 	((TrendView)mView).setCurr(currProgress);
     	                }
                    }
                break;
            }
        }
    };
	    
	public double getMaxProgress() {
		return MaxProgress;
	}

	public void setMaxProgress(String maxProgress) {
		MaxProgress = Double.parseDouble(maxProgress);
		((TrendView)mView).setMax(MaxProgress);
	}

	public double getCurrProgress() {
		return currProgress;
	}

	public void setCurrProgress(String currProgress) {
		this.currProgress = Double.parseDouble(currProgress);
		handler.sendEmptyMessageDelayed(PROGRESS, 100);
	}
    
	public void setColor(String color) {
		((TrendView)mView).setPaintColor(color);
	}
	
	public void setTextColor(String color){
		((TrendView)mView).setTextColor(color);
	}
}