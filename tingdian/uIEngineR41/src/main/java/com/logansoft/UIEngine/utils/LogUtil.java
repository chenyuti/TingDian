
package com.logansoft.UIEngine.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class LogUtil {
    private static boolean logGate = true;
    protected static String specSysbom="   ";

    private static final String TAG = "LogUtil-";

    /**
     * 打开
     * 
     * @param b
     */
    public static void openGate(boolean b) {
        logGate = b;
    }

    public static void i(String text) {
        if (logGate) {
            Log.i(TAG + "info", buildMessage(text));
        }
    }

    public static void d(String text) {
        if (logGate) {
        	String temp=buildMessage(text);
        	if(temp.getBytes().length<4000)
        		Log.d(TAG+"debug", temp);
        	else{
        		String[] temps=temp.split("\n");
        		for(String t :temps){
            		Log.d(TAG+"debug", t);
        		}
        	}
        }
    }

    public static void e(String text) {
        if (logGate) {
            Log.e(TAG + "errer", buildMessage(text));
        }
    }

    public static void v(String text) {
        if (logGate) {
            Log.v(TAG + "verbose", buildMessage(text));
        }
    }

    public static void w(String string) {
        if (logGate) {

            Log.w(TAG + "warn", buildMessage(string));
        }
    }

    public static void i(String tag, String text) {
        if (logGate) {
            Log.i(TAG+tag, buildMessage(text));
        }
    }

    public static void d(String tag, String text) {
        if (logGate) {
        	String temp=buildMessage(text);
        	if(temp.getBytes().length<4000)
        		Log.d(TAG+tag, temp);
        	else{
        		String[] temps=temp.split("\n");
        		for(String t :temps){
            		Log.d(TAG+tag, t);
        		}
        	}
        }
    }

    public static void e(String tag, String text) {
        if (logGate) {
            Log.e(TAG+tag, buildMessage(text));
        }
    }

    public static void v(String tag, String text) {
        if (logGate) {
            Log.v(TAG+tag, buildMessage(text));
        }
    }

    public static void w(String tag, String text) {
        if (logGate) {
            Log.w(TAG+tag, buildMessage(text));
        }
    }

    public static boolean getGate(){
    	return logGate;
    }
    /**
     * Building Message
     * 
     * @param msg The message you would like logged.
     * @return Message String
     */
    private static String buildMessage(String msg) {
        StackTraceElement caller = new Throwable().fillInStackTrace().getStackTrace()[2];

        return new StringBuilder().append(" [").append(caller.getFileName()).append(".")
                .append(caller.getMethodName()).append("#").append(caller.getLineNumber() + "]")
                .append(msg).toString();
    }
    public static String logArray(List<Object> array){
    	return logArray(array,"");
    }
    public static String logArray(List<Object> array,String spec){
    	 if (array.isEmpty()) {
             return spec+"[]";
         }
         StringBuilder buffer = new StringBuilder(array.size() * 16);
         buffer.append(spec);
         buffer.append('[');
         Iterator<?> it = array.iterator();
         while (it.hasNext()) {
             Object next = it.next();
             if (next != array) {
            	 if(next instanceof Map)
     				buffer.append("\n").append(LogUtil.logMap((Map<String, Object>) next,spec+specSysbom));
     			else if(next instanceof List)
     				buffer.append("\n").append(LogUtil.logArray((List<Object>) next,spec+specSysbom));
     			else if(next instanceof String){
    				buffer.append("\"");
    				buffer.append(next);
    				buffer.append("\"");
    			}
     			else
     				buffer.append(next);
             } else {
                 buffer.append("(this Collection)");
             }
             if (it.hasNext()) {
                 buffer.append(", ");
             }
             else if(next instanceof Map || next instanceof List){
     			buffer.append("\n"+spec);
     		}
         }
         buffer.append(']');
         return buffer.toString(); 
    }
    public static String logMap(Map<String,Object> map){
    	return logMap(map,"");
    }
    public static String logMap(Map<String,Object> map,String spec){
    	if (map.isEmpty()) {
    		return spec+"{}";
    	}
    	StringBuilder buffer = new StringBuilder(map.size() * 28);
    	buffer.append(spec);
    	buffer.append('{');
    	Iterator<Map.Entry<String,Object>> it = map.entrySet().iterator();
    	while (it.hasNext()) {
    		Map.Entry<String,Object> entry = it.next();
    		Object key = entry.getKey();
    		if (key != map) {
    			buffer.append(key);
    		} else {
    			buffer.append("(this Map)");
    		}
    		buffer.append('=');
    		Object value = entry.getValue();
    		if (value != map) {
    			if(value instanceof Map)
    				buffer.append("\n").append(LogUtil.logMap((Map<String, Object>) value,spec+specSysbom));
    			else if(value instanceof List)
    				buffer.append("\n").append(LogUtil.logArray((List<Object>) value,spec+specSysbom));
    			else if(value instanceof String){
    				buffer.append("\"");
    				buffer.append(value);
    				buffer.append("\"");
    			}
    			else
    				buffer.append(value);
    		} else {
    			buffer.append("(this Map)");
    		}
    		if (it.hasNext()) {
    			buffer.append(", ");
    		}
    		else if(value instanceof Map || value instanceof List){
    			buffer.append("\n"+spec);
    		}
    	}
    	buffer.append('}');
    	return buffer.toString();
    }
}
