
package com.logansoft.UIEngine.parse.field.adapterGroup.ImageSlider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.adapterGroup.AdapterGroup;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.BaseViewFactory;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.view.AotoViewPager.AutoScrollViewPager;

import android.graphics.Canvas;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class ImageSlider extends AdapterGroup {
    public static final String ATTR_PAGE_CHANGE = "onPageChange";

    int xlocation,ylocation;

    // 循环间隔
    private int interval = 4000;
    protected AutoScrollViewPager viewPager;
    private String pageChangeLua;
    
    private boolean isCycle;
    protected pageControl _pageControl;
    
    public ImageSlider(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
    	mView = new RelativeLayout(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
    }
    protected void createViewPager(){
        viewPager = new AutoScrollViewPager(mContext);
    }
    @Override
    protected void parseView() {
    	super.parseView();
    	createViewPager();
    	_pageControl=new pageControl(mContext);
        _pageControl.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
    	_pageControl.setControlSize(attrMap.get("controlSize"));
    	_pageControl.setControlMargin(attrMap.get("controlMargin"));
    	_pageControl.setControlSpace(attrMap.get("controlSpace"));
    	_pageControl.setControlGravity(attrMap.get("controlGravity"));
    	_pageControl.setNormalIndicator(attrMap.get("controlBackground"));
        _pageControl.setHighlightIndicator(attrMap.get("selectedControlBackground"));
        ((RelativeLayout)mView).addView(viewPager);
        ((RelativeLayout)mView).addView(_pageControl);
        getLuaListener();
    	initViewPager();
    }
    @Override
    protected void parseAttributes(){
    	super.parseAttributes();
    	viewPager.setLayoutParams(new RelativeLayout.LayoutParams(mLayoutParams));
    }
    @Override
	public AdapterInterface createAdapter() {
		return new ViewpagerAdapter();
	}
    @Override
  	public void setAdapter(AdapterInterface adapter){
        viewPager.setAdapter((ViewpagerAdapter)adapter);
    }
    @Override
    public void setARGB(String modeString){
    	super.setARGB(modeString);
    	_pageControl.ARGBMode=this.ARGBMode;
    }
    private void initViewPager() {
        setInterval();
        setCycle();
        if (interval != 0) {
            viewPager.setInterval(interval);
        }
//        viewPager.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_NONE);
     
        viewPager.setOffscreenPageLimit(0);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
            	_pageControl.setCurrentPage(position);
                if (!TextUtils.isEmpty(pageChangeLua)) {
                    // add Lua callback
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("position", position);
                    executeLua(pageChangeLua, map);
                }
            }
        });
        setAutoScroll();
        String a = attrMap.get("allowScroll");
        if (!TextUtils.isEmpty(a)) {
            boolean allow = Boolean.parseBoolean(a);
            setAllowScroll(allow);
        }
    }
    
    public void setOffscreenPageLimit(int num){viewPager.setOffscreenPageLimit(num);}
    
    private void setAutoScroll() {
        String a = attrMap.get("autoScroll");
        if (!TextUtils.isEmpty(a)) {
            boolean auto = Boolean.parseBoolean(a);
            if (auto) {
                startAutoScroll();
            }
        }
    }
    
    private void setCycle() {
        String a = attrMap.get("cycle");
        if (!TextUtils.isEmpty(a)) {
            isCycle =Boolean.parseBoolean(a);
            viewPager.setCycle(isCycle);
        }
    }
    private void setInterval() {
        String a = attrMap.get("interval");
        if (!TextUtils.isEmpty(a)) {
            interval = Integer.parseInt(a);
        }
    }

    public int getCount(){return ((PagerAdapter) adapter).getCount();}
    public int getCurrentPage(){return _pageControl.currentPage;}
	public void setCurrentPage(int currentPage){_pageControl.setCurrentPage(currentPage);}

    @Override
	public void reloadTemplateData(){
    	super.reloadTemplateData();
    	if(adapter!=null && entity!=null){
    		_pageControl.setPageNumber(((ViewpagerAdapter)adapter).getCount());
    		if(entity.getPageIndex()==1)
    			_pageControl.setCurrentPage(0);
    	}
	}
    
    public void setScrollDurationFactor(double a){viewPager.setScrollDurationFactor(a);}
    public void stopAutoScroll(){viewPager.stopAutoScroll();}
    public void startAutoScroll(){viewPager.startAutoScroll();}
    private void getLuaListener(){pageChangeLua = attrMap.get(ATTR_PAGE_CHANGE);}
    public void setAllowScroll(boolean allow){viewPager.setSCROLL_ALLOW(allow);}
    
    @Override
    public void onLowMemory(){
    	super.onLowMemory();
    	for(int i=0;i<viewPager.getChildCount();i++){
    		View v=viewPager.getChildAt(i);
    		Object bo=v.getTag();
    		if(bo instanceof BaseView)
    			((BaseView)bo).onLowMemory();
    	}
    }
    @Override
    public void viewWillAppear(){
    	super.viewWillAppear();
    	for(int i=0;i<viewPager.getChildCount();i++){
    		View v=viewPager.getChildAt(i);
    		Object bo=v.getTag();
    		if(bo instanceof BaseView)
    			((BaseView)bo).viewWillAppear();
    	}
    }
    @Override
    public void viewDidDisappear(){
    	super.viewDidDisappear();
    	for(int i=0;i<viewPager.getChildCount();i++){
    		View v=viewPager.getChildAt(i);
    		Object bo=v.getTag();
    		if(bo instanceof BaseView)
    			((BaseView)bo).viewDidDisappear();
    	}
    }
    @Override
    public void Destory(){    	
    	viewPager.stopAutoScroll();
    	for(int i=0;i<viewPager.getChildCount();i++){
    		View v=viewPager.getChildAt(i);
    		Object bo=v.getTag();
    		if(bo instanceof BaseView)
    			((BaseView)bo).Destory();
    		v.setTag(null);
    	}
    	viewPager=null;
    	super.Destory();
    }
    
    public class ViewpagerAdapter extends PagerAdapter implements AdapterInterface{
        @Override
        public void destroyItem(ViewGroup container, int position, Object object){
        	LogUtil.e("destory item "+position);
        	if(object instanceof BaseView){
        		((BaseView)object).getView().setTag(null);
        		((ViewPager)container).removeView(((BaseView)object).getView());
        	}
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
        	if(entity!=null){
        		List<UIEngineEntity> templateData=entity.getTemplateData();
        		if(position<templateData.size()){
        			UIEngineEntity itemEntity=templateData.get(position);
        			String style=itemEntity.getStyle();
        			if(style==null)
        				style=GlobalConstants.ATTR_STYLE_DEFAULT;
            		Element template = Templates.get(style);
            		if (template!=null) {
            			BaseView childView = BaseViewFactory.newInstance(baseFragment,ImageSlider.this,template);
            			childView.upDateEntity(itemEntity);
            			container.addView(childView.getView());
            			childView.getView().setTag(childView);
            			return childView;
            		}
        		}
        	}
        	return container;
        }

        @Override
        public int getCount(){
        	if(entity!=null)
        		return entity.getTemplateData().size();
        	return 0;
        }

        @Override
    	public int getItemPosition(Object object) {
        	if(entity!=null){
        		if(object instanceof BaseView){
        			int i =entity.getTemplateData().indexOf(((BaseView)object).getEntity());
        			if(i!=-1)
        				return i;
        		}
        	}
    		return PagerAdapter.POSITION_NONE;
    	}
        @Override
        public boolean isViewFromObject(View arg0, Object arg1){
        	if(arg1 instanceof BaseView)
        		return arg0 == ((BaseView)arg1).getView();
        	return false;        
        }   
    }
}
