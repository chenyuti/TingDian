
package com.logansoft.UIEngine.parse.xmlview;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.utils.http.RequestManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MWebView extends BaseView {
    public static final String onLoadFinish = "onLoadFinish";

    public static final String onLoadBegin = "onLoadBegin";

    public static final String onLoadError = "onLoadError";

    public static final String WebTitleCallback = "WebTitleCallback";

    boolean isLoading = false;

    private String params;

    // 1.onLoadBegin 开始加载
    // 2.onLoadFinish 加载完成
    // 3.onLoadError 加载错误
    // 4.WebTitleCallback 更改页面标题
    public MWebView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
        mView = new WebView(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
    }
    @SuppressLint({
        "NewApi", "SetJavaScriptEnabled"	
    })
    @Override
    protected void parseView() {
    	super.parseView();
        this.defaultClickable=true;

        WebSettings setting = ((WebView)mView).getSettings();
        setting.setJavaScriptEnabled(true);
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            ((WebView)mView).setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        }
//        ((WebView)mView).setWebViewClient(new MyWebViewClient());
//        ((WebView)mView).setWebChromeClient(new MyWebClient());
//        ((WebView)mView).addJavascriptInterface(new JavaToLua(), "client");
//        ((WebView)mView).requestFocus();
//        loadUrl(attrMap.get("url"));
        final String url = attrMap.get("url");
		if (!StringUtil.isEmpty(url)) {
			mView.post(new Runnable() {
				@Override
				public void run() {
					((WebView) mView).loadUrl(url);
				}
			});
		}

    }

    /**
     * 加载页面, webView.loadUrl("http://www.xxxx.com"); 加载网络
     * webView.loadUrl("file:///android_asset/test.html"); 加载本地资源
     * webView.loadUrl("javascript:invokedByJava('java_obj')"); 调用 javascript 函数
     * 
     * @param url
     */
    public void loadUrl(String url) {
        if (!StringUtil.isEmpty(url)) {
        	if(!url.startsWith("javascript:") && !url.startsWith("file:///"))
        		url=RequestManager.getFullUrl(url);
        	final String fullURL=url;
            mView.post(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> additionalHttpHeaders =new ArrayMap<String, String>();
                    additionalHttpHeaders.put("resource", "android");
                    additionalHttpHeaders.put("client", "clientapp");
                    ((android.webkit.WebView)mView).loadUrl(fullURL, additionalHttpHeaders); 
                }
            });
        }
    }
    
    public void postUrl(final String url,final String params){
        this.params=params;
        LogUtil.i("------------webView postUrl "+params);
        if (!StringUtil.isEmpty(url)) 
        	loadUrl(url);
    }

    /**
     * @author Prosper.Z
     */
    public class JavaToLua {
        /**
         * javascript 调用
         * @param luaStr lua函数
         * @param value 返回的value
         */
        @JavascriptInterface
        public void clientCallBack(String luaStr) {
            LogUtil.i("--js回调:"+luaStr);
            try {
               boolean params = luaStr.startsWith("LUA:");
               if(params){
                   luaStr= luaStr.replace("LUA:", "");
                   executeLua(luaStr);
               }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override 
    public void setValue(String value){
    	this.value=value;
    	loadUrl(value);
    }
   

    /**
     * 回退
     */
    public void goBack() {
        ((android.webkit.WebView)mView).goBack();
    }

    /**
     * 往前
     */
    public void goForward() {
        ((android.webkit.WebView)mView).goForward();
    }
    
    /**
     * 判断是否可以向后
     */
    public void canGoBack() {
        ((android.webkit.WebView)mView).canGoBack();
    }
    /**
     * 判断是否可以向前跳转
     */
    public void canGoForward() {
        ((android.webkit.WebView)mView).canGoForward();
    }
    
    /**
     * 停止加载
     */
    public void stop() {
        ((android.webkit.WebView)mView).stopLoading();
    }

    public boolean isLoading() {
        return isLoading;
    }
    public class MyWebViewClient extends WebViewClient {
        
        @SuppressLint("NewApi")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            LogUtil.i("------------webView params "+params);
            if(!TextUtils.isEmpty(params)){
                String mParams = params ;
                params= null;
                DataOutputStream os=null;
                try {
                    URL mUrl=new URL(url);
                    HttpURLConnection connection= (HttpURLConnection)mUrl.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);  
                    connection.setRequestMethod("POST");
                    Map<String, String> header = RequestManager.getShareHeader();
                    for (Entry<String, String> e : header.entrySet()) {
                        connection.addRequestProperty(e.getKey(), e.getValue());
                    }
                    connection.addRequestProperty("resource", "android");
                    connection.addRequestProperty("client", "clientapp");
                     Map<String, List<String>> rp = connection.getRequestProperties();
                    
                    os=new DataOutputStream(connection.getOutputStream());
                    os.write(mParams.getBytes());
                    os.flush();
                    return new WebResourceResponse("text/html", connection.getContentEncoding(), connection.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    if (os!=null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            
            }
            return null;
        }
        
        @Override
        public void onPageFinished(android.webkit.WebView view, String url) {
        	if(attrMap==null){
        	 return;
        	} 
        	String luaStr = attrMap.get(onLoadFinish);
            if (!TextUtils.isEmpty(luaStr)) {
            	executeLua(luaStr);
            }
            isLoading = false;
            super.onPageFinished(view, url);
        }
        /**
         * 页面加载前调用
         */
        @Override
        public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
            isLoading = true;
            String luaStr = attrMap.get(onLoadBegin);
            if (!TextUtils.isEmpty(luaStr)) {
            	executeLua(luaStr);
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            view.stopLoading();
            view.clearView();
            //用javascript隐藏系统定义的404页面信息  
            view.loadUrl("javascript:document.body.innerHTML=\"\"");  
//            view.loadData("", "", Encoding.UTF_8.name());
        }

    }

    class MyWebClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            // 用Android组件替换
            new AlertDialog.Builder(mContext)
                    // .setTitle("JS提示")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    }).setCancelable(false).create().show();
            return true;
        }
    }

}
