package com.logansoft.UIEngine.utils.http.data;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class UIEngineStringRequest extends UIEngineRequest<String> {

	public UIEngineStringRequest(int method, String url,Map<String,Object> params) {
		super(method,url,params);
	}

	@Override
	protected String parseResponse(byte[] data) {
		try {
			return new String(data,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			return new String(data);
		}
	}
}
