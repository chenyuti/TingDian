package com.logansoft.UIEngine.utils.http;

import java.util.Map;

public class RequestListener {
	public static int NetCallBackType=0;
	public static int LocalCallBackType=1;
	public static int CacheDidUpdateCallBackType=2;
	
	protected Map<String,Object> params;
	public void DidCanceled(){
		
	}
	public void exception(Exception e){
		
	}
	public void DidLoad(Object result,Map<String,Object>responseLuaArgs,int callBackType){
		
	}
}