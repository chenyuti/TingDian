package com.logansoft.UIEngine.parse.field;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.BaseViewFactory;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.MImageButton;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

public class BarItemView extends GroupView {
	protected int index;
	protected ArrayList<BaseView> items;
	public BarItemView(BaseFragment fragment, GroupView parentView, Element element) {
		super(fragment, parentView, element);
	}
	
	@Override
    protected void parseSubView(){
		NodeList nl=mElement.getChildNodes();
		items=new ArrayList<BaseView>();
		int j=0;
		Element lineElement=null;
		if( mElement.getAttribute("lineSize")!=null && mElement.getAttribute("lineSize").length()!=0){
			lineElement =mElement.getOwnerDocument().createElement("item");
			lineElement.setAttribute("class", "Line");
			lineElement.setAttribute("size", mElement.getAttribute("lineSize"));
			String lineColor=mElement.getAttribute("lineColor");
			if(lineColor==null || lineColor.length()==0)
				lineColor="gray";
			lineElement.setAttribute("background",lineColor);
		}
		for(int i=0;i<nl.getLength();i++){
			Node n=nl.item(i);
			if(n.getNodeType()==Node.ELEMENT_NODE){
				Element itemElement=(Element)n;
		        String sclass = itemElement.getAttribute("class");
		        BaseView item = null;
		        if (TextUtils.isEmpty(sclass)) {
		        	itemElement.setAttribute("imageGravity", "top");
		            item = new MImageButton(baseFragment,this,itemElement);
		        } else {
		            item = BaseViewFactory.newInstance(baseFragment,this,itemElement);
		        }
		        if(j==0)
		        	item.setSelected(true);
		        View view = item.getView();
		        view.setOnClickListener(new MyOnClick(item,j));
		        addView(item);
		        items.add(item);
		        if(lineElement!=null && i<=(nl.getLength()-1)){
		        	BaseView line=BaseViewFactory.newInstance(baseFragment,this,lineElement);
			        addView(line);
		        }
		        j++;
			}
		}
		index=0;
	}

	protected void setTagIndexByBar(int i){
		items.get(index).setSelected(false);
		index=i;
		items.get(i).setSelected(true);
	}
	class MyOnClick implements OnClickListener {
		private BaseView baseView;
		final private int i;
		public MyOnClick(BaseView view,int i) {
			baseView = view;
			this.i=i;
		}
		@Override
		public void onClick(View v) {
			if (index ==i) {
				return;
			}
			items.get(index).setSelected(false);
			index = i;
			((TabBar)getParent()).changeTagPage(i);
			if (!TextUtils.isEmpty(baseView.getAttributes().get("onClick"))) {	
				baseView.setSelected(true);
				baseView.executeLua(baseView.getAttributes().get("onClick"),null);
			}					
		}
	}
}
