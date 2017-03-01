
package com.logansoft.UIEngine.parse.xmlview;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.view.ProgressView;

import android.graphics.Canvas;

/**
 * @author Prosper.Z
 */
public class MProgress extends BaseView {
    public static final String ATTR_GROUP_FOREGROUND_COLOR = "fgColor";

    public static final String ATTR_GROUP_MAX_COUNT = "maxCount";

    public static final String ATTR_GROUP_CURRENT_COUNT = "currentCount";

    public MProgress(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }

    @Override
    protected void createMyView(){
    	 String bgColor = attrMap.get(ATTR_BACKGROUND);
         String fgColor = attrMap.get(ATTR_GROUP_FOREGROUND_COLOR);
         String borderColor = attrMap.get(ATTR_GROUP_BORDER_COLOR);
         String maxCount = attrMap.get(ATTR_GROUP_MAX_COUNT);
         String currentCount = attrMap.get(ATTR_GROUP_CURRENT_COUNT);
         if (maxCount != null) {
             mView = new ProgressView(mContext, bgColor, borderColor, fgColor, maxCount,currentCount){
         		@Override
         		public void draw(Canvas canvas) {
         			bdraw(canvas);
        			super.draw(canvas);
        			adraw(canvas);
         		}
         	};
         } else {
             mView = new ProgressView(mContext, bgColor, borderColor, fgColor){
         		@Override
        		public void draw(Canvas canvas) {
         			bdraw(canvas);
        			super.draw(canvas);
        			adraw(canvas);
        		}
        	};
         }
    }
    @Override 
    public void setValue(String value){
    	ProgressView pv=(ProgressView)mView;
    	pv.setMaxCount(0);
    	pv.setCurrentCount(0);
    	if(value!=null && value.length()!=0){
    		String[] data = value.split(",");
    		if(data.length==2){
    			float currentCount = Float.parseFloat(data[0]);
        		float MaxCount = Float.parseFloat(data[1]);
        		pv.setMaxCount(MaxCount);
        		pv.setCurrentCount(currentCount);
    		}
    	}
    }
    @Override
    public String getValue(){
    	ProgressView pv=(ProgressView)mView;
    	return ""+pv.getCurrentCount()+pv.getMaxCount();

    }
}
