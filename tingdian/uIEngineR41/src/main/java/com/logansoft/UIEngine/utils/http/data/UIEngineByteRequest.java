package com.logansoft.UIEngine.utils.http.data;

import java.util.Map;

public class UIEngineByteRequest extends UIEngineRequest<byte[]> {
	public UIEngineByteRequest(int method, String url,Map<String, Object> params) {
		super(method, url, params);
	}

	@Override
	protected byte[] parseResponse(byte[] data) {
		return data;
	}

}
