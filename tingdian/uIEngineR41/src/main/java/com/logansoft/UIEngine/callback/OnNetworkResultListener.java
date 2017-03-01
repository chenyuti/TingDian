package com.logansoft.UIEngine.callback;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

/**
 * 网路请求的回调
 * 
 * @author zhangxing
 *
 * @param <T> 成功返回之后的数据格式
 */
public abstract class OnNetworkResultListener<T> implements Listener<T> ,ErrorListener{
    @Override
    public void onErrorResponse(VolleyError error) {
        error(error);
    }

    @Override
    public void onResponse(T response) {
        succeed(response);
    }
    
    public abstract void succeed(T response);
    public abstract void error(VolleyError error);
    
}
