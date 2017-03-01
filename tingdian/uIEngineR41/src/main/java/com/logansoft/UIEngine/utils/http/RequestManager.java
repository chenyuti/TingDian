
package com.logansoft.UIEngine.utils.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.logansoft.UIEngine.keyboard.AESEncodeDecode;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.FileUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StreamUtil;
import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.utils.http.data.UIEngineRequest;
import com.logansoft.UIEngine.utils.images.manage.ImageCacheManager;
import com.logansoft.UIEngine.utils.images.manage.ImageCacheManager.CacheType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.view.View;
import hdz.base.Function;

public class RequestManager {
	public enum RequestType{
		RequestTypePage,
		RequestTypeXML,
		RequestTypeImage,
		RequestTypeLua,
		RequestTypeJson,
		RequestTypeConfig
	};
	public final static String REQUESTCALLBACK="Delegate";
	public final static String IMAGEREQUESTTARGETWIDTH="ImageRequestTargetWidth";//int
	public final static String IMAGEREQUESTTARGETHEIGHT="ImageRequestTargetHeight";//int
	public final static String IMAGEREQUESTOPTIONS="ImageRequestOptions";//将要删除 options
	public final static String IMAGEREQUESTSTATE="ImageRequestState";
	public final static String REQUESTLISTENER="RequestListener";
	
    private static RequestQueue mRequestQueue;

    private static ImageLoader mImageLoader;

    private static Map<String, String> globalShareHeader;

    private static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 10;

    private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;

    private static int DISK_IMAGECACHE_QUALITY = 100; // PNG is lossless so
                                                      // quality is ignored but
                                                      // must be provided

    // public static Map<String, String> upFileParams;
    

    private RequestManager() {
        // no instances
    }

    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        globalShareHeader=new HashMap<String,String>();
        // int memClass = ((ActivityManager)
        // context.getSystemService(Context.ACTIVITY_SERVICE))
        // .getMemoryClass();
        // // Use 1/8th of the available memory for this memory cache.
        // int cacheSize = 1024 * 1024 * memClass / 8;
        ImageCacheManager imageCacheManager = ImageCacheManager.getInstance();
        imageCacheManager.init(context, "Images", DISK_IMAGECACHE_SIZE,
                DISK_IMAGECACHE_COMPRESS_FORMAT, DISK_IMAGECACHE_QUALITY, CacheType.NULL);
        mImageLoader = imageCacheManager.getImageLoader();
    }

    /**
     * 清除缓存/图片缓存
     * 
     * @param context
     */
    public void clearCache(Context context) {
        ImageCacheManager.getInstance().clearCache();
    }

    public static Map<String, String> getShareHeader() {
        return new HashMap<String,String>(globalShareHeader);
    }
    public static void clearShareHeader() {
    	globalShareHeader.clear();
    }
    public static void removerShareHeader(String key) {
    	globalShareHeader.remove(key);
    }
    public static void putShareHeader(String key, String value) {
        globalShareHeader.put(key, value);
    }
    public static void putAllShareHeader(Map<String, String> header) {
    	globalShareHeader.putAll(header);
    }
    

    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
    }

    public static void cancelAll(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache}
     * which effectively means that no memory caching is used. This is useful
     * for images that you know that will be show only once.
     * 
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }   
    public static final String deal(String url){
    	while(true){
    		int st=url.indexOf("{");
    		int et=url.indexOf("}");
    		if(st>=0 && et>st){
    			url=deal2(url,st,et);
    		}
    		else
    			break;
    	}
		return url;

    }
    private static final String deal2(String url,int st,int et){
		String tag=url.substring(st+1,et);
		String value=Configure.getTagReplace(tag);
		if(value==null)value="";
		url=url.replace("{"+tag+"}", value);
		return url;
    }
    public final static String getFullUrl(String url){
    	String result=deal(url);
    	Uri uri = Uri.parse(result);
    	String uriScheme=uri.getScheme();
    	if (!result.startsWith(GlobalConstants.URI_SCHEME_HTTP)
    			&& !GlobalConstants.URI_SCHEME_HTTPS.equals(uriScheme)){
     		return (GlobalConstants.ROOT_URL+result);
    	}
    	return result;
    }
    public static final void request(Map<String,Object> params){
    	String RequestURL=(String)params.get(RequestOptions.REQUESTURL);
    	LogUtil.e("最开始的url====="+RequestURL);
    	if(StringUtil.isEmpty(RequestURL))
    		return;
    	RequestURL=deal(RequestURL);
    	params.put(RequestOptions.REQUESTORIGINALURL, RequestURL);
    	String typeString=(String)params.get(RequestOptions.RESPONSETYPE);

    	RequestType type=RequestType.RequestTypeXML;
    	if(RequestOptions.RESPONSEIMAGE.equals(typeString))
    		type=RequestType.RequestTypeImage;
    	else if(RequestOptions.RESPONSELUA.equals(typeString))
    		type=RequestType.RequestTypeLua;
    	else if(RequestOptions.RESPONSEPAGE.equals(typeString))
    		type=RequestType.RequestTypePage;
    	else if(RequestOptions.RESPONSEJSON.equals(typeString))
    		type=RequestType.RequestTypeJson;
      	else if(RequestOptions.RESPONSECONFIG.equals(typeString))
    		type=RequestType.RequestTypeConfig;
    	
        Uri uri = Uri.parse(RequestURL);
        LogUtil.e("开始转化的url====="+uri);
        
        String uriScheme=uri.getScheme();
        LogUtil.e("uriScheme====="+uriScheme);
        String fullURL=RequestURL;
        String Authority=uri.getAuthority();
        String fileName=Authority!=null?(Authority+uri.getPath()):uri.getPath();
        
        RequestListener RL=(RequestListener)params.get(REQUESTLISTENER);
      	RL.params=params;
        boolean local=false;
        if (RequestURL.startsWith(GlobalConstants.URI_SCHEME_RES)
        	||RequestURL.startsWith(GlobalConstants.URI_SCHEME_LOCAL)){
        	String localPath="";
        	local=true;
        	switch(type){
        		case RequestTypeXML:
        		case RequestTypePage:
        			localPath=Configure.xmlRootPath;break;
        		case RequestTypeImage:break;
        		case RequestTypeJson:
        			localPath=Configure.jsonRootPath;break;
        		case RequestTypeLua:
        			localPath=Configure.luaRootPath;break;
        		case RequestTypeConfig:
        			localPath=Configure.filesPath+"/config";break;
        		default:break;
        	}
        	if(type!=RequestType.RequestTypeImage){
        		InputStream input = null;
        		Object response=null;
        		try {
        			File file = new File(localPath,Function.MD5_GetString(fileName));
        			input = FileUtil.readFileFromSDCard(file);
        			byte[] content = StreamUtil.toByteArray(input);
        			input.close();
        			byte[] decodeContent = AESEncodeDecode.AESDecode(content, Function.MD5_GetByteS("AD_"+Configure.macAddress));
        			input = new ByteArrayInputStream(decodeContent);

        			if(type==RequestType.RequestTypeLua){
        				response=StringUtil.getStringFromStream(input, "utf-8");
        				RL.DidLoad(response,new HashMap<String,Object>(),RequestListener.LocalCallBackType);
        			}
        			else if(type==RequestType.RequestTypeJson){
        				response=StringUtil.getStringFromStream(input, "utf-8");
        				response=new JSONObject(new JSONTokener((String)response){
        					@Override
        				    public Object nextValue() throws JSONException {
        						Object result=super.nextValue();
        						if(result ==JSONObject.NULL)
        							return null;
        						return result;
        					}
        				});
        				RL.DidLoad(response,new HashMap<String,Object>(),RequestListener.LocalCallBackType);
        			}
        			else if(type==RequestType.RequestTypePage){
        				response=StreamUtil.toByteArray(input);
        				RL.DidLoad(response,new HashMap<String,Object>(),RequestListener.LocalCallBackType);
        			}
        			else if(type==RequestType.RequestTypeXML){
        				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        				DocumentBuilder builder;
        				Document doc=null;
        				builder = factory.newDocumentBuilder();
        				doc=builder.parse(input);
        				doc.normalize();
        				RL.DidLoad(doc, new HashMap<String,Object>(),RequestListener.LocalCallBackType);
        			}
        			
        		} catch (Exception e){
        			if(Configure.DEBUG_MODE)
        				LogUtil.e(localPath+"/"+Function.MD5_GetString(fileName));
        			e.printStackTrace();
        			RL.exception(e);
        		}
        	finally{
        		if(input!=null)
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
        	}
        	}

    	} else if (!RequestURL.startsWith(GlobalConstants.URI_SCHEME_HTTP)
    			&& !GlobalConstants.URI_SCHEME_HTTPS.equals(uriScheme)) {
            String getTypeString="";
            String baseURL=GlobalConstants.ROOT_URL;
            LogUtil.e("111baseURL==="+baseURL);
    		switch(type){
    			case RequestTypePage:
    				LogUtil.i("RequestTypePage");
    				baseURL=GlobalConstants.TEMPLATE_URL;
    				getTypeString="getpage";break;
    			case RequestTypeLua:
    				baseURL=GlobalConstants.TEMPLATE_URL;//??
    				getTypeString="getLua";break;
        		case RequestTypeConfig:
        			baseURL= GlobalConstants.CONFIG_URL;
    				getTypeString="getConfig";break;
    			default:
    				break;
    		}
    		
    		StringBuilder sb=new StringBuilder(baseURL);
    		if(type==RequestType.RequestTypePage || type==RequestType.RequestTypeLua || type==RequestType.RequestTypeConfig)
    			sb.append("filename=").append(RequestURL).append("&type=").append(getTypeString);
    		else
    			sb.append(RequestURL);
    		fullURL=sb.toString();
    		LogUtil.e("最终的url"+fullURL);
    	}
        

        if(local==true && type!=RequestType.RequestTypeImage)
        	return;
  
        if(type!=RequestType.RequestTypeImage){
        	BaseRequestListener brl=new BaseRequestListener(RL);
        	params.put(REQUESTLISTENER, brl);
        	UIEngineRequest<?> request =UIEngineRequest.newRequest(fullURL,params);
        	int SOCKET_TIMEOUT = 60 * 1000;
        	RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT,0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        	request.setRetryPolicy(retryPolicy);
        	RequestManager.addRequest(request, "BaseFragment");
        }
        else{
        	boolean netRequest=true;
        	if(uriScheme==null ){
        		fullURL = GlobalConstants.ROOT_URL + RequestURL;
        	}
        	else if(GlobalConstants.URI_SCHEME_RES.equals(uriScheme) ){
        		fullURL = Scheme.ASSETS.wrap(Configure.PAGE_IMAGES_PATH + "/" + fileName);
        		netRequest=false;
        	}else if(GlobalConstants.URI_SCHEME_LOCAL.endsWith(uriScheme)){
        		fullURL=Scheme.FILE.wrap(fileName);
        		netRequest=false;
        	}
        	else if(!"http".equals(uriScheme) && ! "https".equals(uriScheme)){
        		fullURL = GlobalConstants.ROOT_URL + RequestURL;
        	}
     	


//            ImageSize isize=null;
//            if(params.containsKey(IMAGEREQUESTTARGETHEIGHT) && params.containsKey(IMAGEREQUESTTARGETWIDTH)){
//            	int targetHeight=(Integer)params.get(IMAGEREQUESTTARGETHEIGHT);
//            	int targetWidth=(Integer)params.get(IMAGEREQUESTTARGETWIDTH);
//            	//LogUtil.i(""+targetWidth+"x"+targetHeight);
//            	isize=new ImageSize(targetWidth,targetHeight);
//            }
          //  else


            DisplayImageOptions options =(DisplayImageOptions) params.get(IMAGEREQUESTOPTIONS);
            MyImageLoadingListener listener = new MyImageLoadingListener(RL,netRequest);

            if (options == null) {
    			options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY).build();
    		}
            if(fullURL.contains(".9."))
            	options = new DisplayImageOptions.Builder().cloneFrom(options).imageScaleType(ImageScaleType.NONE).build();
          
          
        	Object callback=params.get(REQUESTCALLBACK);
        	if(callback!=null){
        		  int loadImgaeState=0;
                  if(params.containsKey(IMAGEREQUESTSTATE))
                  	loadImgaeState=(Integer) params.get(IMAGEREQUESTSTATE);
        	      requestNonViewAware imageAware = new requestNonViewAware(fullURL,(View)callback,loadImgaeState);
                  com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(fullURL, imageAware, options, listener);
        	}
        	else{
        		   //requestImageViewAware imageAware = new requestImageViewAware((ImageView)callback,loadImgaeState);
        		com.nostra13.universalimageloader.core.ImageLoader.getInstance().loadImage(fullURL, listener);
        	}
        }
    }

    public static class BaseRequestListener extends RequestListener{
    	private RequestListener listener;
    	
    	public BaseRequestListener(RequestListener listener){
    		this.listener=listener;
    	}
    	@Override
    	public void DidCanceled(){
    		listener.DidCanceled();
    	}
    	@Override
    	public void exception(Exception e){
    		listener.exception(e);
    	}
    	@Override
    	public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
    		listener.DidLoad(result,responseLuaArgs,callBackType);
    	}
    }

}
