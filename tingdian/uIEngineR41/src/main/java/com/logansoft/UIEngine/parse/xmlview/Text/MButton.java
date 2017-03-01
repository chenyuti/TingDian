
package com.logansoft.UIEngine.parse.xmlview.Text;


import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;

import android.graphics.Canvas;
import android.view.Gravity;
import android.view.animation.Animation;
import android.widget.Button;

public class MButton extends MLabel {
    public MButton(BaseFragment baseFragment,GroupView parentView,Element mElement) {
        super(baseFragment,parentView,mElement);
    }
    @Override
    protected void createMyView(){
        mView=mTextView=new Button(mContext){
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
    	defaultTextAlignmentY=Gravity.CENTER_VERTICAL;
		defaultTextAlignmentX=Gravity.CENTER_HORIZONTAL;
		defaultMaxLines=1;
    }
}
