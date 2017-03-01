
package com.logansoft.UIEngine.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;

import android.app.Activity;


public abstract class LuaProvider {
    private LuaState mLuaState;
    public LuaProvider(Activity context,final String luaUrl){
    	super(); 
    	mLuaState = LuaStateFactory.newLuaState(context);
    	mLuaState.openLibs();
        LuaStateFactory.insertLuaState(mLuaState);
        mLuaState.getGlobal("package");
    	mLuaState.pushString(Configure.luaRootPath+"/?.lua");
    	mLuaState.setField(-2,"path");
    	LuaBaseFunction.initFunction(mLuaState);
		LuaStorage.initFunction(mLuaState);
        // mHasLuaLoaded = false;
	    try {
	    	HashMap<String,Object> params=new HashMap<String,Object>();
	    	params.put(RequestOptions.REQUESTURL,luaUrl);
	    	params.put(RequestOptions.RESPONSETYPE, RequestOptions.RESPONSELUA);
	    	params.put(RequestOptions.REQUESTNET,"NetSelector:");
	    	params.put(RequestOptions.REQUESTLOCAL,"LocalSelector:");
	    	RequestListener requestListener=new RequestListener(){
	    		@Override
	    		public void DidLoad(Object result,final Map<String,Object>responseLuaArgs,int callBackType){
	    			if(callBackType==CacheDidUpdateCallBackType)return;
	    			if(result instanceof String){
	    				if(mLuaState.LdoString((String) result)!=0){
	    					LogUtil.e(mLuaState.toString(-1));
	    					LogUtil.e("error on file "+luaUrl);
	    				}
	    				didLoadLua(LuaProvider.this);
	                }
	    		}
	        };
	    	params.put(RequestManager.REQUESTLISTENER, requestListener);
	    	RequestManager.request(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    abstract protected void didLoadLua(final LuaProvider lp);   	
    
    public void callLua(String luaStr,ArrayList<Object> params) {
    	 if (mLuaState==null) {
             return;
         }
         String functionName = luaStr.substring(0, luaStr.indexOf("("));
         // 判断有无此方法
         boolean isFunction = mLuaState.getLuaObject(functionName).isFunction();
         if (isFunction) {
             mLuaState.getField(LuaState.LUA_GLOBALSINDEX, functionName);
             int paramsCount=0;
             if(params!=null){
            	 for (Object param:params) {
            		 mLuaState.pushJavaObject(param);
            	 }
            	 paramsCount=params.size();
             }
             mLuaState.pcall(paramsCount,0,0);
         } else {
             LogUtil.d(functionName + "没有实现该方法");
         }
    }
    
    /**
     * 调用lua脚本
     * 
     * @param basFragment
     * @param luaStr
     * @param objectMap
     */
    public void callLua(String luaStr,Map<String, Object> objectMap) {
        if (mLuaState==null) {
            return;
        }
        String functionName = luaStr.substring(0, luaStr.indexOf("("));
        String attrs = luaStr.substring(luaStr.indexOf("(") + 1, luaStr.indexOf(")"));
        String[] functionParams = null;
        if (StringUtil.isEmpty(attrs)) {

        } else if (attrs.contains(",")) {
            functionParams = attrs.split(",");
        } else {
            functionParams = new String[] {
                attrs
            };
        }
        // 判断有无此方法
        boolean isFunction = mLuaState.getLuaObject(functionName).isFunction();
        if (isFunction) {
            // 找到脚本内的函数
            mLuaState.getField(LuaState.LUA_GLOBALSINDEX, functionName);// 找到callAndroidApi函数
            if (functionParams != null) {
                for (int i = 0; i < functionParams.length; i++) {
                    // 参数1压栈
                    if (functionParams[i].equals("page")) {
                        mLuaState.pushJavaObject(objectMap.get("page"));
                    } else if (functionParams[i].equals("this")) {
                        mLuaState.pushJavaObject(objectMap.get("this"));
                    } else if (functionParams[i].matches("[{][\\w]+[}]")) {// {[\\w]+}
                    	// 匹配{xxx}g关键字
                        Object obj = objectMap.get(functionParams[i].replaceAll("[{}]", ""));
                        try {
                        	mLuaState.pushObjectValue(obj);
                        } catch (LuaException e) {
                        	e.printStackTrace();
                        }
                    }else if (functionParams[i].matches("'{1}.*'{1}")) {// 匹配
                        mLuaState.pushString(functionParams[i].replaceAll("'", ""));
                    } else {
                        if (functionParams[i].matches("[\\-]?[0-9]+([.]{1}[0-9]+)?")) {// 匹配整数或者小数
                            mLuaState.pushInteger(Integer.parseInt(functionParams[i]));
                        } else {
                            mLuaState.pushString(functionParams[i]);
                        }
                    }
                }
            } else {
                functionParams = new String[] {};
            }
            mLuaState.pcall(functionParams.length,0,0);
        } else {
            LogUtil.d(functionName + "没有实现该方法");
        }
    }
    
    
    static public Object toObjectDefault(Object luaObject){
    	if(luaObject instanceof LuaObject){
        	Object result=null;
			try {
				result=LuaProvider.toObject((LuaObject)luaObject);
			}
			catch (LuaException e){
				e.printStackTrace();
			}
	    	return result;
		}
    	else
    		return luaObject;
    }
   
    static public Object toObject(LuaObject luaObject) throws LuaException{
//     	 final public static Integer LUA_TNONE     = new Integer(-1);
//    	  final public static Integer LUA_TNIL      = new Integer(0);
//    	  final public static Integer LUA_TBOOLEAN  = new Integer(1);
//    	  final public static Integer LUA_TLIGHTUSERDATA = new Integer(2);
//    	  final public static Integer LUA_TNUMBER   = new Integer(3);
//    	  final public static Integer LUA_TSTRING   = new Integer(4);
//    	  final public static Integer LUA_TTABLE    = new Integer(5);
//    	  final public static Integer LUA_TFUNCTION = new Integer(6);
//    	  final public static Integer LUA_TUSERDATA = new Integer(7);
//    	  final public static Integer LUA_TTHREAD   = new Integer(8);
    	switch(luaObject.type()){
    	case -1://None
    	case 0://Nil
    		return null;
    	case 1://Boolean
    		return luaObject.getBoolean();
    	case 2://LightUserData
    		return luaObject.getObject();
    	case 3://Number
    		return luaObject.getNumber();
    	case 4://String
    		return luaObject.getString();
    	case 5://Table
    		LuaState L=luaObject.getLuaState();
    		HashMap<Object, Object> map=new HashMap<Object, Object>();
    		ArrayList<Object> array=new ArrayList<Object>();
    		synchronized (L)
    		{
    			luaObject.push();
        		L.pushNil();
        		while (L.next(-2)>0)  
        		{   
        			Object key=toObject(L.getLuaObject(-2));  
        			Object value=toObject(L.getLuaObject(-1));
        			if(key!=null){
        				if(key instanceof Number)
        					array.add(value);
        				else
        					map.put(key, value);
        			}
        			L.pop(1);  
        		}
        		L.pop(1);
    		}
    		if(array.size()>0)
    			return array;
    		else
    			return map;
       	case 6://Function
    	case 7://UserData
    	case 8://Thread
    		return luaObject.getObject();
    	}
    	return null;
    }
    public LuaObject getToLuaObject(Object object) throws LuaException{
    	if(mLuaState==null)
    		return null;
    	pustToLuaObject(object);
		LuaObject luaResult=mLuaState.getLuaObject(-1);
		mLuaState.pop(-1);
		return luaResult;
    }
    public void pustToLuaObject(Object object) throws LuaException{
    	if(object instanceof Integer){
    		mLuaState.pushInteger(((Integer)object).intValue());
    	}else if(object instanceof Double || object instanceof Float){
    		mLuaState.pushNumber(((Number)object).doubleValue());
    	}else if(object instanceof ArrayList){
    		mLuaState.newTable();
        	int i=1;
        	for(Object childObject:(ArrayList)object){
        		mLuaState.pushInteger(i);
        		pustToLuaObject(childObject);
        		mLuaState.setTable(-3);
        	}
    	}
    	else if(object instanceof Map){
    		mLuaState.newTable();
        	Map<String,Object> objectMap=(Map<String,Object>)object;
    		Iterator<String> iterator=objectMap.keySet().iterator();
        	while (iterator.hasNext()) {
        		String key = iterator.next();
        		mLuaState.pushString(key);
        		pustToLuaObject(objectMap.get(key));
        		mLuaState.setTable(-3);
        	}
    	}
    	else if(object instanceof JSONObject){
    		mLuaState.newTable();
    		JSONObject jsonobject=(JSONObject) object;
    		Iterator<String> iterator=jsonobject.keys();
        	while (iterator.hasNext()) {
        		String key = iterator.next();
        		try {
            		Object value=jsonobject.get(key);
            		mLuaState.pushString(key);
					pustToLuaObject(value);
					mLuaState.setTable(-3);
				} catch (JSONException e) {
					//e.printStackTrace();
				}
        	}
    	}
    	else if(object instanceof JSONArray){
    		mLuaState.newTable();
    		JSONArray jsonArray=(JSONArray) object;
    		int j=1;
        	for(int i=0;i<jsonArray.length();i++){
				try {
	        		Object childObject = jsonArray.get(i);
	        		mLuaState.pushInteger(j);
	        		pustToLuaObject(childObject);
	        		mLuaState.setTable(-3);
	        		j++;
				} catch (JSONException e) {
//					e.printStackTrace();
				}
        	}
    	}
    	else {
    		mLuaState.pushObjectValue(object);
    	}
    }
    
    public void close(){
    	mLuaState.close();
    	mLuaState=null;
    }
    
    static public class luaProviderObject{
    	private Object object;
    	public luaProviderObject(Object object){
    		this.object=object;
    	}
    	public void pushToLuaState(LuaState luaState){
    		try {
				pushToLuaState(luaState,this.object);
			} catch (LuaException e) {
				e.printStackTrace();
			}
    	}
    	private void pushToLuaState(LuaState luaState,Object object) throws LuaException{
    		if(object instanceof Integer){
    			luaState.pushInteger(((Integer)object).intValue());
        	}else if(object instanceof Double || object instanceof Float){
        		luaState.pushNumber(((Number)object).doubleValue());
        	}else if(object instanceof ArrayList){
        		luaState.newTable();
            	int i=1;
            	for(Object childObject:(ArrayList)object){
            		luaState.pushInteger(i);
            		pushToLuaState(luaState,childObject);
            		luaState.setTable(-3);
            	}
        	}
        	else if(object instanceof Map){
        		luaState.newTable();
            	Map<String,Object> objectMap=(Map<String,Object>)object;
        		Iterator<String> iterator=objectMap.keySet().iterator();
            	while (iterator.hasNext()) {
            		String key = iterator.next();
            		luaState.pushString(key);
            		pushToLuaState(luaState,objectMap.get(key));
            		luaState.setTable(-3);
            	}
        	}
        	else if(object instanceof JSONObject){
        		luaState.newTable();
        		JSONObject jsonobject=(JSONObject) object;
        		Iterator<String> iterator=jsonobject.keys();
            	while (iterator.hasNext()) {
            		String key = iterator.next();
            		try {
                		Object value=jsonobject.get(key);
                		luaState.pushString(key);
                		pushToLuaState(luaState,value);
    					luaState.setTable(-3);
    				} catch (JSONException e) {
    					//e.printStackTrace();
    				}
            	}
        	}
        	else if(object instanceof JSONArray){
        		luaState.newTable();
        		JSONArray jsonArray=(JSONArray) object;
        		int j=1;
            	for(int i=0;i<jsonArray.length();i++){
    				try {
    	        		Object childObject = jsonArray.get(i);
    	        		luaState.pushInteger(j);
    	        		pushToLuaState(luaState,childObject);
    	        		luaState.setTable(-3);
    	        		j++;
    				} catch (JSONException e) {
//    					e.printStackTrace();
    				}
            	}
        	}
        	else {
        		luaState.pushObjectValue(object);
        	}
    	}
    }
}
