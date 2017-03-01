package com.logansoft.UIEngine.utils.http.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.logansoft.UIEngine.keyboard.AESEncodeDecode;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.EncryptBody;
import com.logansoft.UIEngine.utils.FileUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.StreamUtil;
import com.logansoft.UIEngine.utils.cache.ACache;
import com.logansoft.UIEngine.utils.http.CookiesProvider;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;

import hdz.base.Function;

public abstract class UIEngineRequest<T> extends Request<T> {
	static final int RESPONSETYPESTRING=0;
	static final int RESPONSETYPEXML=1;
	static final int RESPONSETYPEJSON=2;

    private final Map<String, String> headers;
    private final RequestListener requestListener;
    private final Object requestParams;
    private final ArrayList<ArrayList<String>> postFilePathArray;
    private final Map<String,Object> params;
    protected final boolean cacheResponse;
    private String cacheHash;
    private boolean hasLoadedCache;
    private final boolean encrypt;
    private boolean POSTFROMDATA;
    protected Map<String,Object> responseLuaArgs;
    protected byte[] body;
    private boolean hasBodyData;
    private static boolean encode;
    private static ArrayList<String> ignoreKeys;
    
    {
    	ignoreKeys=new ArrayList();
    }
    public static void addIgnoreKey(String key){
    	if(!ignoreKeys.contains(key))
    		ignoreKeys.add(key);
    }
    public static void removeIgnoreKey(String key){
    	ignoreKeys.remove(key);
    }
    public static ArrayList getIgnoreKeys(){
    	return ignoreKeys;
    }
    public static void setEncode(boolean encode){
    	UIEngineRequest.encode=encode;
    }
    
    public static UIEngineRequest newRequest(String url,Map<String,Object> params){
    	String responseType=(String)params.get(RequestOptions.RESPONSETYPE);
    	String method=(String)params.get(RequestOptions.METHOD);
    	int Method=Request.Method.GET;
    	if(method!=null){
    		if(method.equals(RequestOptions.METHOD_POST)||method.equals(RequestOptions.METHOD_JSON))Method=Request.Method.POST;
         	else if(method.equals(RequestOptions.METHOD_FORM))Method=Request.Method.FORM;
    	}
    	if(Method==Request.Method.GET){
    		Object requestParams=params.get(RequestOptions.REQUESTPARAMS);
    		if(requestParams instanceof Map){
    			Map<String,String> rp=(Map<String,String>)requestParams;
    			url=url+"?"+appendURLParams(rp,true);
    		}
    	}
    	if(RequestOptions.RESPONSEXML.equals(responseType))
    		return new UIEngineXMLRequest(Method,url,params);
    	else if(RequestOptions.RESPONSESTRING.equals(responseType))
    		return new UIEngineStringRequest(Method,url,params);
    	else if(RequestOptions.RESPONSEBYTE.equals(responseType))
    		return new UIEngineByteRequest(Method,url,params);
    	else if(RequestOptions.RESPONSELUA.equals(responseType))
    		return new UIEngineByteRequest(Method,url,params);
    	else if(RequestOptions.RESPONSEPAGE.equals(responseType))
    		return new UIEnginePageXMLRequest(Method,url,params);
    	else if(RequestOptions.RESPONSEJSON.equals(responseType))
    		return new UIEngineJSONRequest(Method,url,params);
    	else if(RequestOptions.RESPONSECONFIG.equals(responseType))
    		return new UIEngineStringRequest(Method,url,params);
    	else
    		return new UIEngineXMLRequest(Method,url,params);    	
    }
    
	public UIEngineRequest(int method,String url,Map<String,Object> params) {
		super(method,url,null);
		this.requestListener=(RequestListener)params.get(RequestManager.REQUESTLISTENER);
		this.params=params;
        this.headers=RequestManager.getShareHeader();
        this.requestParams=params.get(RequestOptions.REQUESTPARAMS);
        if(this instanceof UIEngineJSONRequest && this.requestParams instanceof Map){
        	((Map)this.requestParams).put("responseType", "json");
        }
        this.cacheResponse=GlobalConstants.BOOLEANTRUE.equals(params.get(RequestOptions.CACHERESPONSE));
        HashMap<String,String> requestheanders=(HashMap<String,String>)params.get(RequestOptions.REQUESTHEADERS);
        if(requestheanders!=null)
        	headers.putAll(requestheanders);
        if(this instanceof UIEngineJSONRequest ){
        	headers.put("responseType","json");
        }
        encrypt=params.get(RequestOptions.REQUESTENCODE)==null?
        		encode:GlobalConstants.BOOLEANTRUE.equals(params.get(RequestOptions.REQUESTENCODE));
        if (encrypt) 
        	headers.put("ENCRYPTTYPE","AES");
        if(getMethod()==Request.Method.FORM	){
        	POSTFROMDATA=true;
        	headers.put("Connection", "Keep-Alive");
        	headers.put("Charset", "UTF-8");
        	Object temp=params.get("PostFilePaths");
        	if(temp instanceof ArrayList){
            	postFilePathArray=(ArrayList<ArrayList<String>>)temp;
        	}else{
        		postFilePathArray=null;
        		params.remove("PostFilePaths");
        	}
        }else
            postFilePathArray=null;
        parseBody();
		responseLuaArgs=new HashMap<String,Object>();
        readCache();
        
	}
	public void putHeader(String key,String value){
		headers.put(key, value);
	}
	public void putAllHeader(HashMap<String,String> headers){
		this.headers.putAll(headers);
	}
	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		HashMap<String,String> result=new HashMap<String,String>(headers);
		String cookies=CookiesProvider.getCookies(getUrl());
		if(cookies!=null){
			result.put("Cookie",cookies);
		}
		return result;
	}
	@Override
    protected Map<String, String> getParams() throws AuthFailureError {
		if(requestParams instanceof Map)
			return (Map)requestParams;
        return null;
    }

	protected void parseBody(){		
		if(RequestOptions.METHOD_JSON.equals(this.params.get(RequestOptions.METHOD))){
			if(requestParams instanceof Map ){
				JSONObject result=new JSONObject((Map)requestParams);
				String bodyString=result.toString();
				try {
					body=bodyString.getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					body=bodyString.getBytes();
				}
			}
//			else if(requestParams instanceof List){
//			}
		}
		else if(requestParams instanceof Map){
			Map<String, String> params;
			try {
				params = getParams();
				if (params != null && params.size() > 0) {
					body=encodeParameters(params, getParamsEncoding());
				}
			} catch (AuthFailureError e) {
				e.printStackTrace();
			}
		}
		else if(requestParams instanceof String){
			try {
				body=((String)requestParams).getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				body=((String)requestParams).getBytes();
			}
		}
		else if(requestParams instanceof byte[]){
			body=(byte[])requestParams;
		}
		else if(requestParams instanceof File){
			InputStream input = FileUtil.readFileFromSDCard((File) requestParams);
			try {
				body = StreamUtil.toByteArray(input);
			} catch (IOException e) {
				e.printStackTrace();
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
		if(body!=null && encrypt){
			body=EncryptBody.encrypt(body);
			headers.put("CHECKKEY", EncryptBody.getCheckKey(body));
		}
	}
	protected void readCache(){
		if(params.get(RequestOptions.REQUESTLOCAL)!=null){
			if(cacheHash==null) hashCacheID();
			ACache acache = ACache.get(Configure.cacheDir, "data");
			byte[] cache = acache.getAsBinary(cacheHash);
			if(cache!=null){
				putHeader("cache",Function.MD5_GetString(cache));
				T responseResult=parseResponse(cache);
				if(responseResult!=null){
					hasLoadedCache=true;
					responseLuaArgs.put("response",responseResult);
					requestListener.DidLoad(responseResult,responseLuaArgs,RequestListener.LocalCallBackType);
				}
			}
    	}
	}
	@Override
	public byte[] getBody() throws AuthFailureError {
		return body;
	}

	@Override
	public Part[] getFormParts() throws AuthFailureError{
		 Map<String, String> params=getParams();
         ArrayList<Part> partArray=new ArrayList<Part>();
         if (params!=null){
        	 for (String key : params.keySet()) {            	
        		 StringPart p= new StringPart(key, params.get(key).toString(),HTTP.UTF_8);
        		 partArray.add(p);
        	 }
         }
         ArrayList<ArrayList<String>> postFilePaths=getFilePathArray();
         if(postFilePaths!=null){
        	 for (ArrayList<String> pathSet:postFilePaths) {	
        		 if(pathSet.size()>=2){
        			 File file=new File(pathSet.get(1));
        			 FilePart part;
        			 try {
        				 String contentType = null;
        				 String fileName=file.getName();
        				 if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg"))
        					 contentType="image/jpeg";
        				 else if(fileName.endsWith(".png"))
        					 contentType="image/png";
        				 if(pathSet.size()==2)
        					 part = new FilePart(pathSet.get(0),file);
        				 else
        					 part = new FilePart(pathSet.get(0),pathSet.get(2),file);
        				 if(contentType!=null){
        					 part.setContentType(contentType);
        				 }
        				 partArray.add(part);
        			 } catch (FileNotFoundException e) {
        				 e.printStackTrace();
        			 }
        		 }
        	 }
         }
         Part[] parts = (Part[])partArray.toArray(new Part[partArray.size()]);
         return parts;
    }
	
	@Override
	public String getBodyContentType() {
		int method=getMethod();
		if(method==Request.Method.GET)
			return "text/html; charset=" + getParamsEncoding();
		else if(method==Request.Method.POST)
			return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
		else if(method==Request.Method.FORM)
				return "amultipart/form-data; boundary=--"+getParamsEncoding()+"--\r\n";
		return super.getBodyContentType();
	}	
	public ArrayList<ArrayList<String>> getFilePathArray(){
		return postFilePathArray;
	}
	 
	abstract protected T parseResponse(byte[] data);

	protected void writeCache(byte[] data){
		if(cacheHash==null) hashCacheID();
		ACache acache = ACache.get(Configure.cacheDir, "data");
		acache.put(cacheHash, data);
	}
	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		responseLuaArgs.put("headers",response.headers);
		responseLuaArgs.put("code",response.statusCode);
		byte[] result=response.data;
		if(result.length>0)
			hasBodyData=true;
		if (encrypt){
			byte[] keyBytes = Function.MD5_GetByteS("AD_" + Configure.macAddress);
			result = AESEncodeDecode.AESDecode(result, keyBytes);	            
		}
		if(cacheResponse && hasBodyData)
			writeCache(result);
		T responseResult=parseResponse(result);
		if(responseResult!=null)
			responseLuaArgs.put("response",responseResult);

		List<String> cookies=response.cookies;
		for(String cookie:cookies){
      	  CookiesProvider.setCookies(cookie, this.getUrl());
		}
		return (Response<T>) Response.success(responseResult,HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected void deliverResponse(T response){
		if(params.get(RequestOptions.REQUESTNET)!=null){
			if(!hasLoadedCache){
				if(response==null && hasBodyData==true)
					requestListener.exception(null);
				else
					requestListener.DidLoad(response,responseLuaArgs,RequestListener.NetCallBackType);
			}
			else{
				requestListener.DidLoad(response,responseLuaArgs,RequestListener.CacheDidUpdateCallBackType);
			}
		}
	}
	@Override
	public void deliverError(VolleyError error) {
		if(!hasLoadedCache)
			requestListener.exception(null);   
	}
	private void hashCacheID(){
		Object orul=params.get(RequestOptions.REQUESTORIGINALURL);
		String tempCache=getUrl();
		if(orul instanceof String)
			tempCache=(String)orul;
		ArrayList<String> tempIgnoreKeys=new ArrayList<String>(ignoreKeys);
		Object tiks2=params.get(RequestOptions.CACHEIGNOREKEYS);
		if(tiks2 instanceof ArrayList){
			for(String k:(ArrayList<String>)tiks2){
				if(!tempIgnoreKeys.contains(k))tempIgnoreKeys.add(k);
			}
		}
		if(tempIgnoreKeys.size()>0){
			String[] a1=tempCache.split("\\?");
			if(a1.length==2){
				String[] a2=a1[1].split("&");
				HashMap<String,String> pm=new HashMap<String,String>();
				for(String sp : a2){
					String[] pk=sp.split("=");
					if(pk.length==2 && !tempIgnoreKeys.contains(pk[0])){
						pm.put(pk[0], pk[1]);
					}	
				}
				if(pm.size()!=a2.length){
					tempCache=a1[0];
					if(pm.size()>0)
						tempCache+=("?"+appendURLParams(pm,false));
				}
			}
		}
		if(body!=null) {
    		if(requestParams instanceof Map){
    			HashMap<String,String> tm=new HashMap<String,String>((Map)requestParams);
    			for(String key :tempIgnoreKeys)
    				tm.remove(key);
    			if(tm.size()>0)
    				tempCache+=("?"+appendURLParams(tm,false));
    		}
    		else
    			tempCache=tempCache+Function.MD5_GetString(body);
		}
		cacheHash=Function.MD5_GetString(tempCache);
	}
	static public String appendURLParams(Map<String,String> params,boolean urlEncode){
		if(params.size()==0)
			return "";
		StringBuilder sb=new StringBuilder();
		try {
			for (String key : params.keySet()) {
				sb.append(urlEncode?URLEncoder.encode(key,"UTF-8"):key);
				sb.append("=");
				sb.append(urlEncode?URLEncoder.encode(params.get(key),"UTF-8"):params.get(key)).append("&");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.substring(0, sb.length()-1);
	}
}
