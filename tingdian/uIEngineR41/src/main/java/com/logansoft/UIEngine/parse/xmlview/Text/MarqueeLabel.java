
package com.logansoft.UIEngine.parse.xmlview.Text;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.view.MarqueeTextView;

import android.graphics.Canvas;
import android.widget.TextView;
/**
 * 跑马灯效果的Label
 * @author Prosper Z
 *
 */
public class MarqueeLabel extends MLabel {
    public MarqueeLabel(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
    	mView=mTextView=new MarqueeTextView(mContext){
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
        ((TextView)mView).setSingleLine();
    }

    public void startMarquee(){((MarqueeTextView)mView).startMarquee();}
    public void stopMarquee(){((MarqueeTextView)mView).stopMarquee();}
}
