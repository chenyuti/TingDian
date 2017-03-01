package com.logansoft.scss.view;

import org.w3c.dom.Element;

import android.graphics.Canvas;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;

public class PieChart extends BaseView{
	public PieChart(BaseFragment fragment, GroupView parentView,
			Element element) {
		super(fragment, parentView, element);
		// TODO Auto-generated constructor stub
	}
	
    protected void createMyView(){
    	mView = new PirChartView(mContext){
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
	}

	public void updateView(Object dataMap) {
		// TODO Auto-generated method stub
		
	}
	
	public void setPer(String per1, String per2, String per3, String per4) {
		((PirChartView)mView).setPer(per1, per2, per3, per4);
	}
}