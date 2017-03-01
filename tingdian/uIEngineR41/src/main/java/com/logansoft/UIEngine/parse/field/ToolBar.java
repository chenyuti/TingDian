package com.logansoft.UIEngine.parse.field;


import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;

import android.view.View;
import android.view.ViewGroup;

public class ToolBar extends TabBar {
    public static final String TOOLBAR_ITEM ="ToolBarItem";

    public ToolBar(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
     
    @Override
    protected void initLayout() {
    	ViewGroup mGroup=(ViewGroup)mView;
    	mGroup.addView(barItemView.getView());
    	View line = createLine();
    	if (line != null)
    		mGroup.addView(line);
    	mGroup.addView(tagview);
    }

}
