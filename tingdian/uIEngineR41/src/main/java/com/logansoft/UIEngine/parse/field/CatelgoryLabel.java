
package com.logansoft.UIEngine.parse.field;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.adapterGroup.adapter.XBaseAdapter;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.Text.MLabel;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.DisplayUtil;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CatelgoryLabel extends MLabel implements OnClickListener {

    private XBaseAdapter adapter;

    private static final String DEFAULT_TAG = "不限";

    LinearLayout mTagContainer;

    private Tag mCurrentTag = null; // 用来保存当前点击中的tag

    private boolean isLoadingTag = false;

//    ArrayList<Tag> mTagList ;

    ArrayList<TextView> mTagsViews = new ArrayList<TextView>();

    public CatelgoryLabel(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }

    @Override
    protected void createMyView(){
        mView = mTagContainer = new LinearLayout(mContext){
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
        mTagContainer.setOrientation(LinearLayout.VERTICAL);
        mTagsViews =new ArrayList<TextView>();
        final ArrayList<Tag> mTagList =new ArrayList<Tag>();
        mTagList.add(new Tag("三星"));
        mTagList.add(new Tag("华为"));
        mTagList.add(new Tag("苹果"));
        mTagList.add(new Tag("酷派"));
        mTagList.add(new Tag("诺基亚"));
        mTagList.add(new Tag("TCL"));
        mTagList.add(new Tag("小米"));
        mTagList.add(new Tag("SONY"));
        mTagList.add(new Tag("OPPO"));
        mTagList.add(new Tag("天语"));
        mTagList.add(new Tag("中兴"));
        mTagList.add(new Tag("大唐电信"));
        mTagList.add(new Tag("HTC"));
        mTagList.add(new Tag("摩托罗拉"));
        mTagList.add(new Tag("黑莓"));
        mTagList.add(new Tag("LG"));
        mTagList.add(new Tag("朵唯"));
        mTagList.add(new Tag("金立"));
        mTagList.add(new Tag("海尔"));
        mTagList.add(new Tag("联想"));
        mTagList.add(new Tag("天语"));
        mTagList.add(new Tag("海信"));
        Handler mHandler =new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                addTag(mTagList);
            }
        },100);
    }
    
//    @Override
//    public void updateView(Object dataMap) {
//        List<Tag> mTagList=new ArrayList<Tag>();
//        addTag(mTagList);
//    }
    
    public class Tag {
        String title;
        public Tag(String title) {
            this.title = title;
        }
        public String getTitle() {
            return title;
        }
    }

    public void addTag(List<Tag> mTagList) {
        mTagsViews.clear();
        LinearLayout mContainer = new LinearLayout(mContext);
        mTagContainer.addView(mContainer, getContainerLineParams());
        mTagContainer.measure(0, 0);

        // 获取当前View的宽度
        int width = getContainerWidth();
        int dw = 0;

        TextView allT = getTagAllView();
        mTagsViews.add(allT);
        mContainer.addView(allT, getTextParams());
        allT.setText(DEFAULT_TAG);
        allT.measure(0, 0);
        allT.setTag(DEFAULT_TAG);
        allT.setOnClickListener(this);
        dw += allT.getMeasuredWidth();
        for (int i = 0; i < mTagList.size(); i++) {
            TextView textView = getTagView();
            mContainer.addView(textView, getTextParams());
            mTagsViews.add(textView);
            textView.setText(mTagList.get(i).getTitle());
            textView.measure(0, 0);
            textView.setTag(mTagList.get(i));
            textView.setOnClickListener(this);
            if (dw + textView.getMeasuredWidth() >= width) {
                // 移除添加好的文本框
                mContainer.removeView(textView);
                // 重新添加一个View
                mContainer = new LinearLayout(mContext);
                mTagContainer.addView(mContainer, getContainerLineParams());
                mContainer.measure(0, 0);
                mContainer.addView(textView, getTextParams());
                dw = textView.getMeasuredWidth();
            } else {
                dw += textView.getMeasuredWidth();
            }
        }
        updataTags();
    }

    private void updataTags() {
        for (int i = 0; i < mTagsViews.size(); i++) {
            TextView textView = mTagsViews.get(i);
            if (mCurrentTag == null) {
                if (DEFAULT_TAG.equals(textView.getText())) {
                    textView.setEnabled(false);
                } else {
                    textView.setEnabled(true);
                }
            } else {
                if (mCurrentTag.getTitle().equals(textView.getText())) {
                    textView.setEnabled(false);
                } else {
                    textView.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        // super.onClick(v);
        if (v.getTag() instanceof Tag) {
            Tag mTag = (Tag)v.getTag();
            mCurrentTag = mTag;
            updataTags();
        } else if (DEFAULT_TAG == v.getTag()) {
            mCurrentTag = null;
            updataTags();
        }
    }

    private RelativeLayout.LayoutParams getContainerLineParams() {
        return new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private int getContainerWidth() {
        return (int)Math.round(Configure.screenWidth * 0.7);
    }

    private TextView getTagAllView() {
        TextView textView = new TextView(mContext);
        textView.setTextSize(16);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(10, 0, 10, 0);
        return textView;
    }

    private TextView getTagView() {
        TextView textView = new TextView(mContext);
        textView.setTextSize(16);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(20, 0, 20, 0);
      //  UIEngineDrawable background=new UIEngineDrawable(mContext);
      //  textView.setBackground(background);
        
      //  setBackground(textView);
        return textView;
    }

    private LayoutParams getTextParams() {
        return new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, getTagHeihgt());
    }

    private int tagheight = -1;

    /*
     * 获取tag的高度
     */
    private int getTagHeihgt() {
        if (tagheight == -1) {
            tagheight = DisplayUtil.px2dip(mContext, 50);
        }
        return tagheight;
    }

}
