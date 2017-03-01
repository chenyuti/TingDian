package com.logansoft.UIEngine.utils;

import com.logansoft.UIEngine.keyboard.AESEncodeDecode;

import android.text.TextUtils;

import hdz.base.Function;

import java.util.Map;
import java.util.Set;

public class EncryptBody {
    private static String checkKey ="";
    
    public static String getCheckKey(){
        return checkKey;
    }
    
    public static  byte[] encrypt( String contentes ){
    	if (TextUtils.isEmpty(contentes)) {
    		contentes="";
    	}
    	byte[] content = contentes.getBytes();
        return encrypt(content);
    }
    
    public static String parseParams(Map<String, String> params2) {
        StringBuilder sb = new StringBuilder();
        if (params2 != null) {
            Set<String> keys = params2.keySet();
            for (String key : keys) {
                sb.append(key + "=" + params2.get(key) + "&");
            }
        }

        String contentS = sb.toString();
        String sss=contentS;
        if (contentS.length()>0) {
            sss = contentS.substring(0, contentS.lastIndexOf("&"));
        }
      //  LogUtil.i(" 请求的参数  " +sss);
        return sss;
    }
    public static byte[] encrypt(byte[] input){
    	byte[] params=null;
    	try {
    		byte[] keyBytes = Function.MD5_GetByteS("AD_" + Configure.macAddress);            
    		params = AESEncodeDecode.AESEncode(input, keyBytes);

    		byte[] checkEnd = Configure.checkEnd.getBytes();
    		byte[] ByteArray = new byte[params.length + checkEnd.length];
    		System.arraycopy(params, 0, ByteArray, 0, params.length);
    		System.arraycopy(checkEnd, 0, ByteArray, params.length, checkEnd.length);

    		byte[] data = Function.MD5_GetByteS(ByteArray);
    		checkKey = StringUtil.byteToStr(data);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return params;
    }
    public static String getCheckKey(byte[] input){
    	byte[] checkEnd = Configure.checkEnd.getBytes();
		byte[] ByteArray = new byte[input.length + checkEnd.length];
		System.arraycopy(input, 0, ByteArray, 0, input.length);
		System.arraycopy(checkEnd, 0, ByteArray, input.length, checkEnd.length);
		byte[] data = Function.MD5_GetByteS(ByteArray);
		return StringUtil.byteToStr(data);
    }
}
