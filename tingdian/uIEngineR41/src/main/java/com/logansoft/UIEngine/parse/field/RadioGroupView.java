package com.logansoft.UIEngine.parse.field;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.XmlParser;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.Text.MButton;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;

import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;


public class RadioGroupView extends MButton {

    public RadioGroupView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
    	mView=new LinearLayout(mContext){
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
    	LinearLayout ll=(LinearLayout) mView;
        /* 设置横向,默认为横向,如果有设置就为纵向 */
     //   a.setOrientation(attrMap.containsKey(GlobalConstants.ATTR_ORIENTATION) ? LinearLayout.VERTICAL
     //                   : LinearLayout.HORIZONTAL);

        NodeList childNodes = mElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
           Node itemNode = childNodes.item(i);
            if (itemNode.getNodeType()==Node.ELEMENT_NODE) {
                Element element = (Element)itemNode;
                HashMap<String, String> attr = XmlParser.getParser().parseElementAttr(element);
                if (GlobalConstants.XML_ITEM.equals(element.getNodeName())) {
                    CheckBox c=new CheckBox(mContext);
                    c.setTextColor(0xff000000);
                    LayoutParams params=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    //setLayoutGravity(params, attr.get(ATTR_GRAVITY));
                    //setSize(c, params,attr);
                    if (attr.containsKey(ATTR_LABEL)) {
                        c.setText(attr.get(ATTR_LABEL));
                    }
                    if (attr.containsKey(ATTR_FONT_SIZE)) {
                        String b = attr.get(ATTR_FONT_SIZE);
                        c.setTextSize(Integer.parseInt(b));
                    }
                    if (attr.containsKey(ATTR_FONT_COLOR)) {
                        String b = attr.get(ATTR_FONT_COLOR);
                        c.setTextColor(Color.parseColor(b));
                    }
                    c.setMinimumHeight(DisplayUtil.dip2px(mContext, 20));
                    c.setMinimumWidth(DisplayUtil.dip2px(mContext, 20));
//                   c.setPadding(ss.getIntrinsicWidth(), 0, DisplayUtil.dip2px(mContext, 30), 0);
                   ll.addView(c);
                    c.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                            }else {
                            }
                        }
                    });
                    
//                    if ("".equals(App.isSaveUserName)) {
//                        if ("YES".equals(attr.get(GlobalConstants.ATTR_SELECTED))) {
//                            c.setChecked(true);
//                        }else {
//                            c.setChecked(false);
//                        }
//                    }else  if ("0".equals(App.isSaveUserName)) {
//                         c.setChecked(false);
//                    }else  if ("1".equals(App.isSaveUserName)) {
//                         c.setChecked(true);
//                     }
                    c.setLayoutParams(params);
                }
                
            }
        }
        
    }

}
