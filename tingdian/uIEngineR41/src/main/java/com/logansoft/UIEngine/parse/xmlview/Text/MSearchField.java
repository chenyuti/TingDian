
package com.logansoft.UIEngine.parse.xmlview.Text;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineDrawable;
import com.logansoft.UIEngine.Base.UIEngineGroupView.UIEngineLayoutParams;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.DisplayUtil;

import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MSearchField extends MTextField {
	
    public MSearchField(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void parseAttributes(){
    	super.parseAttributes();
    	parseImage(leftImageDrawable,UIEngineDrawable.StateNormal,attrMap.get(ATTR_IMAGE));
    	parseImage(leftImageDrawable,UIEngineDrawable.StatePressed,attrMap.get(ATTR_PRESSIMAGE));
    	parseImage(leftImageDrawable,UIEngineDrawable.StateSelected,attrMap.get(ATTR_SELECTEDIMAGE));
    	parseImage(leftImageDrawable,UIEngineDrawable.StateDisabled,attrMap.get(ATTR_DISABLEIMAGE));

    	Drawable[] drawables=mTextView.getCompoundDrawables();
    	drawables[0]=leftImageDrawable;
    	if (attrMap.containsKey(ATTR_IMAGE_SIZE)) {
    		String size = attrMap.get(ATTR_IMAGE_SIZE);
    		String[] s = size.split(",");
         	int width = UIEngineLayoutParams.parseSingleSizeString(s[0],mContext,0);
         	int height = UIEngineLayoutParams.parseSingleSizeString(s[1],mContext,1);
            leftImageDrawable.setBounds(0, 0, width, height);
    	}
    	else
            leftImageDrawable.setBounds(0, 0, DisplayUtil.dip2px(mContext, 20), DisplayUtil.dip2px(mContext, 20));
    	mTextView.setCompoundDrawables(drawables[0],drawables[1],drawables[2],drawables[3]);   
    
        
        mTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mTextView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {// 搜索框
                    if (attrMap.containsKey("onEditorAction")) {
                    	executeLua(attrMap.get("onEditorAction"));
                    }
                }
                return true;
            }
        });
        mTextView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mView.performClick();
                        break;
                }
                return false;
            }
        });

    }
}
