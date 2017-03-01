package com.logansoft.UIEngine.parse.field.adapterGroup.ImageSlider;

import java.util.HashMap;
import java.util.Map;

import com.logansoft.UIEngine.Base.UIEngineColorParser;
import com.logansoft.UIEngine.Base.UIEngineDrawable;
import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;

public class pageControl extends View {
	protected Object normalIndicator;
	protected Object highlightIndicator;
	protected Bitmap.Config ARGBMode;
	
	protected String controlSize;
	protected int csw,csh;
	protected String controlMargin;
	protected int cmx,cmy;
	protected String controlGravity;
	protected int gravity;
	protected String controlSpace;
	protected int space;
	
	protected int pageNumber,currentPage;
	
	public pageControl(Context context) {
		super(context);
		csw=csh=DisplayUtil.dip2px(getContext(),10);
		cmx=DisplayUtil.dip2px(getContext(),5);
		cmy=DisplayUtil.dip2px(getContext(),10);
    	ARGBMode=Config.RGB_565;
	}
	public void setControlSize(String controlSize){
		this.controlSize=controlSize;
		if (!TextUtils.isEmpty(controlSize)) {
			String[] s = controlSize.split(",");
			if(s!=null && s.length==2){
				try{
					csw= DisplayUtil.dip2px(getContext(),Float.parseFloat(s[0]));
					csh= DisplayUtil.dip2px(getContext(),Float.parseFloat(s[1]));
				}catch (Exception e){e.printStackTrace();}
			}
		}else{
			csw=csh=DisplayUtil.dip2px(getContext(),10);
		}
	}
	
	public void setControlMargin(String controlMargin){
		this.controlMargin=controlMargin;
		if(controlMargin!=null && controlMargin.length()!=0){
        	String[] s = controlMargin.split(",");
        	if(s!=null && s.length==2){
        		try{
        			cmx=DisplayUtil.dip2px(getContext(),Integer.parseInt(s[0]));
        			cmy=DisplayUtil.dip2px(getContext(),Integer.parseInt(s[1]));
        		}catch (Exception e) { e.printStackTrace();}
        	}
    	}else{
			cmx=DisplayUtil.dip2px(getContext(),5);
			cmy=DisplayUtil.dip2px(getContext(),10);
		}
	}
	public void setControlGravity(String controlGravity){
		this.controlGravity=controlGravity;
		if(controlGravity!=null && controlGravity.length()!=0){
			gravity=0;
			if(controlGravity.contains(GlobalConstants.ATTR_TOP))
				gravity|=UIEngineGroupView.GravityTop;
			else
				gravity|=UIEngineGroupView.GravityBottom;
			if(controlGravity.contains(GlobalConstants.ATTR_LEFT))
				gravity|=UIEngineGroupView.GravityLeft;
			else if(controlGravity.contains(GlobalConstants.ATTR_RIGHT))
				gravity|=UIEngineGroupView.GravityRight;
			else
				gravity|=UIEngineGroupView.GravityCenter_horizontal;
		}
		else
			gravity=UIEngineGroupView.GravityCenter_horizontal|UIEngineGroupView.GravityBottom;
	}
	public void setControlSpace(String controlSpace){
		space=DisplayUtil.dip2px(getContext(),5f);
		if(controlSpace!=null && controlSpace.length()!=0){
			try{
				space=DisplayUtil.dip2px(getContext(),Float.parseFloat(controlSpace));
			}catch(Exception e){e.printStackTrace();}
		}
	}
	public void setNormalIndicator(Object normalIndicator){
		this.normalIndicator=normalIndicator;
		if(normalIndicator instanceof String){
			int color=UIEngineColorParser.getColor((String)normalIndicator);
			if(color!=-2)
				this.normalIndicator=(Integer)color;
			else {
				setImageIndicator((String)normalIndicator,UIEngineDrawable.StateNormal);
			}
		}
	}
	public Object getNormalIndicator(){
		return normalIndicator;
	}
	public void setHighlightIndicator(Object highlightIndicator){
		this.highlightIndicator=highlightIndicator;
		if(highlightIndicator instanceof String){
			int color=UIEngineColorParser.getColor((String)highlightIndicator);
			if(color!=-2)	
				this.highlightIndicator=(Integer)color;
			else {
				setImageIndicator((String)highlightIndicator,UIEngineDrawable.StateSelected);
			}
		}
	}
	public Object getHighlightIndicator(){
		return highlightIndicator;
	}
	public void setCurrentPage(int CurrentPage){
		if(this.currentPage!=CurrentPage)
			invalidate();
		currentPage=CurrentPage;
	}
	public void setPageNumber(int pageNumber){
		if(this.pageNumber!=pageNumber)
			invalidate();
		this.pageNumber=pageNumber;
	}
	protected Size getMeasureSize(){
		Size rect=new Size();
		if(pageNumber<2)
			return rect;
		if(controlSize!=null && controlSize.length()!=0){
			rect.height=csh;
			rect.width=(csw+space)*pageNumber-space;
		}
		else{
			float th=0;
			if(normalIndicator instanceof Bitmap){
				Bitmap indicator=(Bitmap)normalIndicator;
				th=DisplayUtil.dip2px(getContext(),indicator.getHeight()/2);
				rect.width=DisplayUtil.dip2px(getContext(),indicator.getWidth()/2*(pageNumber-1));
			}else{
				th=csh;rect.width=csw*(pageNumber-1);
			}
			if(highlightIndicator instanceof Bitmap){
				Bitmap indicator=(Bitmap)highlightIndicator;
				int th2=DisplayUtil.dip2px(getContext(),indicator.getHeight()/2);
				th=th2>th?th2:th;
				rect.width+=DisplayUtil.dip2px(getContext(),indicator.getWidth()/2);
			}else{
				th=csh>th?csh:th;rect.width+=csw;
			}
			rect.width+=((pageNumber-1)*space);
			rect.height=th;
		}
		return rect;
	}
	@Override
	public void draw(Canvas canvas) {
		//super.draw(canvas);
		if(pageNumber<2)
			return;
		int width=this.getMeasuredWidth(),height=this.getMeasuredHeight();
		Size size=getMeasureSize();
		Rect rect=new Rect();
		if((gravity&UIEngineGroupView.GravityTop)==UIEngineGroupView.GravityTop){
			rect.top=cmy;rect.bottom=(int)(size.height+cmy);
		}else{
			rect.top=(int)(height-size.height-cmy);rect.bottom=height-cmy;
		}
		if((gravity&UIEngineGroupView.GravityLeft)==UIEngineGroupView.GravityLeft){
			rect.left=cmx;rect.right=(int)(cmx+size.height);			
		}else if((gravity&UIEngineGroupView.GravityRight)==UIEngineGroupView.GravityRight){
			rect.left=(int)(width-size.width-cmx);rect.right=width-cmx;
		}else{
			rect.left=(int)((width-size.width)/2);rect.right=height-rect.left;
		}
		int y=rect.top;
		Paint paint=new Paint();
		paint.setAntiAlias(true);
		Paint paint2=new Paint();
		paint2.setAntiAlias(true);
		for(int i=0;i<pageNumber;i++){
			Object indicator=(i==currentPage)?highlightIndicator:normalIndicator;
			if(indicator instanceof Bitmap){
				Bitmap bitmap=(Bitmap)indicator;
				if(controlSize!=null && controlSize.length()!=0){
					rect.top=y+(int)(size.height-csh)/2;rect.bottom=rect.top+csh;
					rect.right=rect.left+csw;
				}else{
					int imagew=DisplayUtil.dip2px(getContext(),bitmap.getWidth()/2);
					int imageh=DisplayUtil.dip2px(getContext(),bitmap.getHeight()/2);
					rect.top=y+(int)(size.height-imageh)/2;rect.bottom=rect.top+imageh;
					rect.right=rect.left+imagew;
				}
				canvas.drawBitmap(bitmap,null,rect, paint2);
			}
			else if(indicator instanceof Integer){
				rect.top=y+(int)(size.height-csh)/2;rect.bottom=rect.top+csh;
				rect.right=rect.left+csw;
				paint.setColor((int)indicator);
				canvas.drawCircle(rect.left+csw/2,rect.top+csh/2,csh/2,paint);
			}
			rect.left=rect.right+space;
		}
		
	}
	protected void setImageIndicator(String imageURL,int state){
		if(imageURL.endsWith(".png") ||imageURL.endsWith(".jpg") ||imageURL.endsWith(".jpeg")){
			Builder builder=getBuilder();
            RequestListener rl=new RequestListener(){            
            	@Override
            	public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
	    			if(callBackType==CacheDidUpdateCallBackType)return;
            		int state =(Integer)params.get(RequestManager.IMAGEREQUESTSTATE);
            		if(!(result instanceof Bitmap))return;
            		Bitmap loadedImage=(Bitmap)result;
                  	if(state==UIEngineDrawable.StateNormal){
                  		setNormalIndicator(loadedImage);
                  	}else if(state==UIEngineDrawable.StateSelected){
                  		setHighlightIndicator(loadedImage);
                  	}
            	}
            };
            HashMap<String,Object> params=new HashMap<String,Object>();
            params.put(RequestManager.REQUESTCALLBACK,this);
            params.put(RequestOptions.REQUESTURL,imageURL);
            params.put(RequestOptions.RESPONSETYPE,"IMAGE");
            params.put(RequestManager.REQUESTLISTENER,rl);
            params.put(RequestManager.IMAGEREQUESTSTATE,state);
            params.put(RequestManager.IMAGEREQUESTOPTIONS,builder.build());
            RequestManager.request(params);
		}
	}
	public Builder getBuilder() {
		Builder builder = new DisplayImageOptions.Builder();
		builder.resetViewBeforeLoading(true);
		// 设置下载的图片是否缓存在内存中
		builder.cacheInMemory(true);
		// 设置下载的图片是否缓存在SD卡中
		builder.cacheOnDisk(true);
		builder.bitmapConfig(ARGBMode);
		builder.displayer(new FadeInBitmapDisplayer(300));
		builder.imageScaleType(ImageScaleType.EXACTLY);
		return builder;
	}
    static public class Size{
    	public float width;
    	public float height;
    	public Size(){}
    	public Size(float width,float height){
    		this.width=width;this.height=height;
    	}
    }

}
