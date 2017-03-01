package com.logansoft.UIEngine.parse.field.adapterGroup.adapter;

import com.logansoft.UIEngine.parse.xmlview.BaseView;

import android.content.Context;
import android.widget.LinearLayout;

public class adapterItemView extends LinearLayout {
	private BaseView bv;
	public adapterItemView(Context context) {
		super(context);
	}
	public void setBaseView(BaseView b){
		bv=b;
		this.setTag(bv);
		this.removeAllViews();
		if(b!=null)
			this.addView(b.getView());
	}
	public BaseView getBaseView(){
		return bv;
	}
	public void onLowMemory(){
		if(bv!=null)bv.onLowMemory();
	}
	public void viewWillAppear(){
		if(bv!=null)bv.viewWillAppear();
	}
    public void viewDidDisappear(){
		if(bv!=null)bv.viewDidDisappear();
    }
    public void Destory(){
    	this.setTag(null);
		if(bv!=null)bv.Destory();	
    }
}
