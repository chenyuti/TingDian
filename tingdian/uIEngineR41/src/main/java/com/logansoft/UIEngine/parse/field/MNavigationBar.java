package com.logansoft.UIEngine.parse.field;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.Configure;

import android.app.Activity;
import android.graphics.Rect;

public class MNavigationBar extends GroupView {
	private int statusBarHeight;
	public MNavigationBar(BaseFragment fragment,GroupView parentView,Element element) {
		super(fragment,parentView,element);
	}
	@Override
	protected void parseView(){
		super.parseView();
		if(Configure.translucent_Status()) {
    		Rect frame = new Rect();
    		((Activity)mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
    		statusBarHeight = frame.top;
    		mLayoutParams.fixHeight=statusBarHeight;
       	}
    	else
    		statusBarHeight=0;
	}
	@Override
	public void setPadding(String padding){
		mPadding=getPaddingOrMargin(padding);
		mView.setPadding(mPadding[0], mPadding[1]+statusBarHeight, mPadding[2], mPadding[3]);
	}
}
