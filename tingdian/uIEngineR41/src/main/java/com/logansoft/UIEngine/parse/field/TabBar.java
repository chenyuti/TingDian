
package com.logansoft.UIEngine.parse.field;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.Base.UIEngineColorParser;
import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.Base.UIEngineGroupView.UIEngineLayoutParams;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.BaseViewFactory;
import com.logansoft.UIEngine.parse.xmlview.GroupView;

import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class TabBar extends GroupView {
    protected static final String TABBAR_ITEM = "TabBarItem";
    protected static final String ATTR_LINESIZE = "lineSize";
    protected static final String ATTR_LINECOLOR = "lineColor";

    protected UIEngineGroupView tagview;
    protected UIEngineLayoutParams tabviewLayoutParams;
    protected ArrayList<Object> tags;
    protected BarItemView barItemView;

    public TabBar(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void parseView() {
    	super.parseView();
    	tagview=new UIEngineGroupView(mContext);
    	tabviewLayoutParams= new UIEngineLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    	tagview.setLayoutParams(tabviewLayoutParams);
    	tags=new ArrayList<Object>();
    	if(mView instanceof UIEngineGroupView)
    		((UIEngineGroupView)mView).defaultOrientation=UIEngineGroupView.VERTICAL;
    }
    
    @Override
    protected void parseSubView(){
    	NodeList nl=mElement.getChildNodes();
    	for(int nli=0;nli<nl.getLength();nli++){
    		Node node=nl.item(nli);
    		if(node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equals("group")){
    			Element group=(Element)node;
    			String field=group.getAttribute("field");
    			if("TabBarItem".equals(field) || "ToolBarItem".equals(field)){
    				barItemView=new BarItemView(baseFragment,this,group);
    			}
    			else{
        			NodeList tenl=node.getChildNodes();
        			for(int i=0;i<tenl.getLength();i++){
        				Node ten=tenl.item(i);
        				if(ten.getNodeType()==Node.ELEMENT_NODE)
        					tags.add(ten);
            		}
        		}
    		}
    	}	
    	changeTagPage(0);
    	initLayout();
    }

  
    protected void initLayout() {
    	ViewGroup mGroup=(ViewGroup)mView;
    	mGroup.addView(tagview);
    	View line = createLine();
    	if (line != null)
    		mGroup.addView(line);
    	if(barItemView!=null)
    		mGroup.addView(barItemView.getView());
    }

    protected View createLine() {
        String lineColor = attrMap.get(ATTR_LINECOLOR);
        String lineSize = attrMap.get(ATTR_LINESIZE);
        if (!TextUtils.isEmpty(lineSize)) {
            if (TextUtils.isEmpty(lineColor)) {
                lineColor = "0xff000000";
            }
            View line = new View(mContext);
            line.setBackgroundColor(UIEngineColorParser.getColor(lineColor));
            //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            UIEngineLayoutParams params = new UIEngineLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            String[] s = lineSize.split(",");
    		if(s!=null && s.length==2){
    			params.width = UIEngineLayoutParams.parseSingleSizeString(s[0],mContext,0);
    			params.height = UIEngineLayoutParams.parseSingleSizeString(s[1],mContext,1);
    		}
    		line.setLayoutParams(params);
            return line;
        }
        return null;
    }
    
    @Override
    public void setSize(String sizeString){
    	super.setSize(sizeString);
    	tabviewLayoutParams.setSizeString(
    			(mLayoutParams.width==LayoutParams.WRAP_CONTENT?"wrap":"fill")+","+
    			(mLayoutParams.height==LayoutParams.WRAP_CONTENT?"wrap":"fill"), mContext);
    }
    
    public void setTagIndex(int tag){
      if (barItemView.index == tag || tag>barItemView.items.size()) {
          return;
      }
      barItemView.setTagIndexByBar(tag);
      changeTagPage(tag);
    }
    public int tagIndex(){
    	return barItemView.index;
    }
        
    protected void changeTagPage(int index){
    	tagview.removeAllViews();
    	if(index>tags.size())
    		return;
    	Object tag=tags.get(index);
    	boolean needInit=false;
    	if(tag instanceof Element){
    		tag=BaseViewFactory.newInstance(baseFragment,this,(Element)tag);
    		tags.set(index, tag);
    		needInit=true;
    	}
    	if(tag instanceof BaseView){
    		final BaseView bv=(BaseView)tag;
    		Runnable r=new Runnable(){
				@Override
				public void run() {
		    		tagview.addView(bv.getView());
		    		//bv.getView().invalidate();
				}
    		};
            if(Looper.myLooper() != Looper.getMainLooper())
    			tagview.post(r);
            else
            	r.run();
    		if(needInit==true){
    			bv.executeLua(bv.getAttributes().get("initData"),null);
    		}
    	}
    }
   //warring! 
    public void onLowMemory(){
    	super.onLowMemory();
    	for(Object object:tags){
    		if(object instanceof BaseView)
    			((BaseView)object).onLowMemory();
    	}
    }
    public void viewWillAppear(){
    	super.viewWillAppear();
    	for(Object object:tags){
    		if(object instanceof BaseView)
    			((BaseView)object).viewWillAppear();
    	}
    }
    public void viewDidDisappear(){
    	super.viewDidDisappear();
    	for(Object object:tags){
    		if(object instanceof BaseView)
    			((BaseView)object).viewDidDisappear();
    	}
    }
    @Override
    public void Destory(){
    	Templates=null;
    	for(Object object:tags){
    		if(object instanceof BaseView)
    			((BaseView)object).Destory();
    	}
    	super.Destory();
    }
}
