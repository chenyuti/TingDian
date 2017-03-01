
package com.logansoft.UIEngine.parse.xmlview.Text;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineDrawable;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;

import android.graphics.Canvas;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.widget.EditText;

public class MTextView extends MTextField {
    public MTextView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
        mView=mTextView=new EditText(mContext){
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
		defaultTextAlignmentX=Gravity.LEFT;
    	defaultTextAlignmentY=Gravity.TOP;
    	defaultMaxLines=Integer.MAX_VALUE;

        if (attrMap.containsKey(ATTR_INPUTTYPE)) {
            inputType = attrMap.get(ATTR_INPUTTYPE);
        }
        if (INPUTTYP_PASSWORD.equals(inputType)) {//系统密码控件
        	mTextView.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else if (INPUTTYP_NUMBER.equals(inputType)) {
            ((EditText)mTextView).setRawInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            mTextView.setKeyListener(new DigitsKeyListener(false, true));
        }
    }  
    @Override
    protected void setClearButton(UIEngineDrawable imageDrawable){
    	
    }
}
