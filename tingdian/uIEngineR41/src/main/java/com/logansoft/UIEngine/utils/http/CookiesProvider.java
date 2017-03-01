package com.logansoft.UIEngine.utils.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;

import android.net.Uri;

public class CookiesProvider {
	private static HashMap<String,cookiesMap> cookiesMap;
	static{
		cookiesMap=new HashMap<String,cookiesMap>();
	}
	
	public static void setCookies(String cookies,String url){
        Uri uri = Uri.parse(url);
        String key=uri.getHost();
		if(StringUtil.isEmpty(key))
			return;
		cookiesMap uriCookiesMap=cookiesMap.get(key);
		if(uriCookiesMap==null){
			uriCookiesMap=new cookiesMap();
			cookiesMap.put(key, uriCookiesMap);
		}
		uriCookiesMap.setCookie(cookies,key);
	}
    public static String getCookies(String url){
    	Uri uri = Uri.parse(url);
    	String key=uri.getHost();
 		if(StringUtil.isEmpty(key))
 			return null;
 		cookiesMap uriCookiesMap=cookiesMap.get(key);
		if(uriCookiesMap==null || uriCookiesMap.values.size()==0)
			return null;
		StringBuilder result=new StringBuilder();
		HashMap<String,String> rm=new HashMap<String,String>();
		uriCookiesMap.getCookieString(rm);
		String[] paths = uri.getPath().split("/");
		cookiesMap mp=null;

		for(String path:paths){
			if(path.equals(""))
				continue;
			mp=uriCookiesMap.pathValues.get(path);
			if(mp==null)
				break;
			mp.getCookieString(rm);
		}

		Set<String> keySet = rm.keySet();
		Iterator<String> iterator = keySet.iterator();
		while(iterator.hasNext()){
			String ck=iterator.next();
			result.append(ck).append("=").append(rm.get(ck)).append("; ");
		}
	//	LogUtil.e("url "+url);
	//	LogUtil.e("get cookie "+result.toString());
		return result.toString();
	}
    
    static class cookiesMap{
    	HashMap<String,cookieValue> values;
    	HashMap<String,cookiesMap> pathValues;
    	public cookiesMap(){
    		values=new HashMap<String,cookieValue>();
    		pathValues=new HashMap<String,cookiesMap>();
    	}
    	protected void setCookie(String cookieString,String domain){
    		cookieValue cookie=new cookieValue(cookieString);
    		if(domain.equalsIgnoreCase(cookie.domain) || cookie.domain==null){
    			if(cookie.paths.length==0)
    				values.put(cookie.name, cookie);
    			else{
//					LogUtil.e("url "+domain);
//    				LogUtil.e("set cookie "+cookieString);
    				cookiesMap mp=this;
    				for(String path: cookie.paths){
//        				LogUtil.e("cookie path "+path);
    					if(path.equals(""))
    						continue;
    					cookiesMap mp2=mp.pathValues.get(path);
    					if(mp2==null){
    						mp2=new cookiesMap();
    						mp.pathValues.put(path, mp2);
    					}
    					mp=mp2;
    				}
    				mp.values.put(cookie.name, cookie);
    			}
    		}
    	}
    	protected void getCookieString(HashMap<String,String> result){
    		Set<String> keySet = values.keySet();
    		Iterator<String> iterator = keySet.iterator();
    		while(iterator.hasNext()){
    			String ck=iterator.next();
    			result.put(ck, values.get(ck).value);
    		}
    	}
    }
    static class cookieValue{
    	String name;
    	String value;
    	String[] paths;
    	String expires;
    	String domain;
    	
    	cookieValue(String cookieString){
    		String[] cs = cookieString.split("; ");
    		for (int i = 0; i < cs.length; i++) {
    			String[] ss=cs[i].split("=");
    			if(ss.length==2){
    				if(i==0){
        				name=ss[0];value=ss[1];
    				}
        			else if("domain".equalsIgnoreCase(ss[0]))
    					domain=ss[1];
    				else if("path".equalsIgnoreCase(ss[0]))
    					paths=ss[1].split("/");
    				else if("expires".equalsIgnoreCase(ss[0]))
    					expires=ss[1];
    			}
    		}
    	}
    }
}
