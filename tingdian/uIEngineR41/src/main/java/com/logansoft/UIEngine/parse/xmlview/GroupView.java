
package com.logansoft.UIEngine.parse.xmlview;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.XmlParser;
import com.logansoft.UIEngine.provider.LuaProvider;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;

import android.graphics.Canvas;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout.LayoutParams;

public class GroupView extends BaseView {
    protected ArrayList<BaseView> childBaseViews;
    protected ViewGroup groupView;
    public static final String ATTR_RES="res";
    public Map<String, Element> Templates;
    protected Map<String,BaseView> idViewMap;

    public GroupView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
        parseTemplates();
        parseSubView();
    }
	@Override
    protected void createMyView(){
    	mView=new UIEngineGroupView(mContext){
			@Override
    		public void draw(Canvas canvas) {
				bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
    }
    @Override
    protected void parseView(){
    	super.parseView();
    	if(mView instanceof ViewGroup)
    		groupView=(ViewGroup) mView;
    	if(rootView==this)
    		idViewMap=new HashMap<String,BaseView>();
    	mLayoutParams.defaultHeight=LayoutParams.MATCH_PARENT;
    	mLayoutParams.defaultWidth=LayoutParams.MATCH_PARENT;
    	if(mView instanceof UIEngineGroupView)
    		((UIEngineGroupView)mView).defaultOrientation=UIEngineGroupView.HORIZONTAL;
    }
    @Override
    protected void parseAttributes() {
        super.parseAttributes();
        setOrientation(attrMap.get(GlobalConstants.ATTR_ORIENTATION));
    }
    protected void parseTemplates(){
        NodeList groupNodes = mElement.getChildNodes();//(GlobalConstants.XML_TEMPLATES);
        for (int i=0;i<groupNodes.getLength();i++) {
            Node groupNode = groupNodes.item(i);
            if(groupNode.getNodeType()==Node.ELEMENT_NODE){
            	Element childElement = (Element)groupNode;
            		if(childElement.getNodeName().equals(GlobalConstants.XML_TEMPLATES)){
            			String style = childElement.getAttribute(GlobalConstants.ATTR_STYLE);
            			if (StringUtil.isEmpty(style)) 
            				style = GlobalConstants.ATTR_STYLE_DEFAULT;
            			putTemplates(style, childElement);
            	}
            }
        }
    }
    protected void parseSubView(){
        XmlParser.getParser().parseChildElement(baseFragment, this, mElement);
        setResXML(attrMap.get(ATTR_RES));
    }
    
   
    
    public void addView(BaseView view) {
    	final View childView = view.getView();
        if (childBaseViews == null) {
            childBaseViews = new ArrayList<BaseView>();
        }

       	int childId = ++baseFragment.baseId;
       	childView.setId(childId);
       	view.setmLayoutId(childId);
        childBaseViews.add(view);
        Runnable r=new Runnable() {
            @Override
            public void run() {
            	groupView.addView(childView);
//            	refreshBackground(mView);
            }
        };
        if(Looper.myLooper() != Looper.getMainLooper())
        	baseFragment.mHandler.post(r);
        else
        	r.run();
    }
   
    public void setResXML(String url){
    	if(StringUtil.isEmpty(url))
    		return;
        RequestListener requestListener=new RequestListener(){
        	@Override
        	public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
    			if(callBackType==CacheDidUpdateCallBackType)return;
        		if (result instanceof byte[]) {
        			InputStream is=null;
        			try {
        				is=new ByteArrayInputStream((byte[]) result);
        				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        				DocumentBuilder builder = factory.newDocumentBuilder();
        				Document doc = builder.parse(is);
        				NodeList elements = doc.getChildNodes();
        				Element mElement = (Element)elements.item(0);
        				BaseView v=BaseViewFactory.newInstance(baseFragment,GroupView.this,mElement);
        				if(v!=null)
        					addView(v);
        			} catch (Exception e) {
        				e.printStackTrace();
        			}finally{
        				try {
        					is.close();
        				} catch (IOException e) {
        					e.printStackTrace();
        				}
        			}
        		}
        	}
        };
        HashMap<String,Object> params=new HashMap<String,Object>();
        params.put(RequestOptions.RESPONSETYPE, RequestOptions.RESPONSEPAGE);
        params.put(RequestOptions.REQUESTURL,url);
        params.put(RequestManager.REQUESTLISTENER, requestListener);
        RequestManager.request(params);
    }

    public void setOrientation(String orientation) {
    	if(mView instanceof UIEngineGroupView)
    		((UIEngineGroupView)mView).setOrientation(orientation);
    }
    public ArrayList<BaseView> getChildViews(){return childBaseViews;}
    public BaseView getChildAtIndex(int index) {return childBaseViews.get(index);}

    public Map<String, Element> getTemplates(){return Templates;}   
    public void putTemplates(String key, Element value) {
        if (Templates == null)
            Templates = new HashMap<String, Element>();
        NodeList nl=value.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
        	Node ni=nl.item(i);
        	if(ni.getNodeType()==Node.ELEMENT_NODE){
        		Element result=(Element)ni;
        		result.setAttribute("rootElement", "true");
                Templates.put(key,result);
        		return;
        	}
        }
    }

    @Override
    public BaseView getElementById(String id){
    	if (!StringUtil.isEmpty(id) && id.equals(mId))
            return this;
    	if(rootView==this)
    		return idViewMap.get(id);
    	else if(rootView!=null)
    		return rootView.getElementById(id);
    	else 
    		return null;
    }

    public void putIdView(BaseView baseView){
    	if(idViewMap!=null && baseView!=null && baseView.getViewId()!=null)
    		idViewMap.put(baseView.getViewId(), baseView);
    }
    public void removeIdView(BaseView baseView){
    	if(idViewMap!=null && baseView!=null && baseView.getViewId()!=null)
    		idViewMap.remove(baseView.getViewId());
    }
    
    public void removeView(BaseView view){
         int index = childBaseViews.indexOf(view);
         removeView(index);
    }
    
    /**
     * 通过 位置 移除View
     * @param index
     */
    public void removeView(int index) {
    	groupView.removeViewAt(index);
        childBaseViews.remove(index);
    }
    
   
    public void removeView(String id){
        if(TextUtils.isEmpty(id)){
            return ;
        }
        for (BaseView view:childBaseViews) {
            if (id.equals(view.mId)) {
            	groupView.removeView(view.getView());
                childBaseViews.remove(view);
            }
        }
    }
    
    public void removeAllViews() {
    	if(!(mView instanceof AdapterView))
    		groupView.removeAllViews();
    	if(childBaseViews!=null )
    		childBaseViews.clear();
    	if(entity!=null)
    		entity.clearTemplateData();
    }
    public List<UIEngineEntity> getTemplateData(){
    	if(entity!=null){
    		return entity.getTemplateData();
    	}
    	return null;
    }
    public int getTempateDataSize(){
    	if(entity!=null){
    		return entity.getTemplateDataSize();
    	}
    	return 0;
    }
    
    public void addTemplateData(Object entities){
    	if(entities instanceof LuaObject){
			try {
				entities=LuaProvider.toObject((LuaObject)entities);
			} catch (LuaException e) {
				e.printStackTrace();
			}
		}
    	if(entities instanceof List && Templates!=null && Templates.size()!=0){
    		String TemplatesDatasStyle=entity.getTemplateDataStyle();
    		if(TemplatesDatasStyle==null || TemplatesDatasStyle.length()==0)
    			TemplatesDatasStyle = GlobalConstants.ATTR_STYLE_DEFAULT;
    		for(Object object:(List<?>)entities){
    			if(object instanceof UIEngineEntity){
    				UIEngineEntity subEntity=(UIEngineEntity)object;
    				String tempStyle=subEntity.getStyle();
    				if(tempStyle==null || tempStyle.length()==0){
    					tempStyle=TemplatesDatasStyle;
    				}
    				Element template=Templates.get(tempStyle);
    				if(template!=null){
    					BaseView bv = BaseViewFactory.newInstance(baseFragment,this,template);
    					addView(bv);
    					bv.upDateEntity(object);
    				}
    			}
    		}
    	}
    }
    public void clearTemplateData() {
        if (childBaseViews!=null) {
        	ArrayList<BaseView> temp=new ArrayList<BaseView>();
        	for(BaseView bv:childBaseViews){
        		if(bv.rootView==bv){
                	groupView.removeView(bv.mView);
                	temp.add(bv);
                }
        	}
        	childBaseViews.removeAll(temp);
        }
    }
    public void reloadTemplateData(){
    	if(entity instanceof UIEngineEntity){
    		//List<UIEngineEntity> list=entity.getTemplateData();
    		//if(list.size()==0)
    		clearTemplateData();
    		//else if (childBaseViews!=null) {
    			//待完成
    		addTemplateData(entity.getTemplateData());
    	}
    }
    @Override
    public void onLowMemory(){
    	super.onLowMemory();
    	if(childBaseViews!=null){
    		for(BaseView bv:childBaseViews){
    			bv.onLowMemory();
    		}
    	}
    }
    @Override
    public void viewWillAppear(){
    	super.viewWillAppear();
    	if(childBaseViews!=null){
    		for(BaseView bv:childBaseViews){
    			bv.viewWillAppear();
    		}
    	}
    }
    @Override
    public void viewDidDisappear(){
    	super.viewDidDisappear();
    	if(childBaseViews!=null){
    		for(BaseView bv:childBaseViews){
    			bv.viewDidDisappear();
    		}
    	}
    }
    @Override
    public void Destory(){
    	Templates=null;
    	if(childBaseViews!=null){
    		for(BaseView v:childBaseViews){
    			v.Destory();
    		}
    	}
    	removeAllViews();
    	super.Destory();
    }
}
