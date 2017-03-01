package com.logansoft.UIEngine.view;

import com.logansoft.UIEngine.parse.xmlview.BaseView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.LinkedList;

public class PageLayout extends RelativeLayout {
    LinkedList<BaseView> basies;
    private int orientation = HORIZONTAL ; 
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int DEFAULT_ID = 100001;
    public static final int PARENT_ID = 100;
    public PageLayout(Context context) {
        super(context);
        basies = new LinkedList<BaseView>();
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    
    public void addView(BaseView view){
        View childView = view.getView();
        ViewGroup.LayoutParams params = childView.getLayoutParams();
        int size = basies.size();
        childView.setId(size+DEFAULT_ID);
        switch (orientation) {
            case HORIZONTAL: //横向排列
                if (params!=null&&(params instanceof LayoutParams)) {
                    if (size>0){
                        ((LayoutParams)params).addRule(RIGHT_OF,size+DEFAULT_ID-1);
                    }
                }
                break;
            case VERTICAL: //纵向排列
                if (params!=null&&(params instanceof LayoutParams)) {
                    if (size>0){
                        ((LayoutParams)params).addRule(BELOW,size+DEFAULT_ID-1);
                    }
                }
                break;
        }
        basies.addLast(view);
        addView(childView, params);
    }
    
    
}
