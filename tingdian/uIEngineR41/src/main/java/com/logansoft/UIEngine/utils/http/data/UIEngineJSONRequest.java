package com.logansoft.UIEngine.utils.http.data;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.LogUtil;

public class UIEngineJSONRequest extends UIEngineRequest<JSONObject>{

	public UIEngineJSONRequest(int method, String url,Map<String, Object> params) {
		super(method, url, params);
	}

	@Override
	protected JSONObject parseResponse(byte[] data) {
		String dataString=null;
		try {
			dataString=new String(data,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			dataString=new String(data);
		}
		JSONObject result=null;
		if(dataString!=null){
			try {
				result=new JSONObject(new JSONTokener(dataString){
					@Override
				    public Object nextValue() throws JSONException {
						Object result=super.nextValue();
						if(result ==JSONObject.NULL)
							return null;
						return result;
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
				if(Configure.DEBUG_MODE==true){
					LogUtil.e(dataString);
				}
			}
		}
		if(result!=null){
			//if(cacheResponse!=true){
				String resultCodeValue=result.optString("resultCode");
				try{
					Integer code=Integer.valueOf(resultCodeValue);
					responseLuaArgs.put("resultCode",code);
				}
				catch(Exception e){
					responseLuaArgs.put("resultCode",resultCodeValue);
				}
				String resultMsg=result.optString("resultMsg");
				responseLuaArgs.put("resultMsg",resultMsg);	
			//}
		}
		return result;
	}


}
