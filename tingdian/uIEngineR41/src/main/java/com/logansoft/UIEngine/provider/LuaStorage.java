package com.logansoft.UIEngine.provider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;

import com.logansoft.UIEngine.keyboard.AESEncodeDecode;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StreamUtil;

import android.app.Application;
import android.content.Context;
import hdz.base.Function;

public class LuaStorage {
	static private Application mContext;
	final static private storage tempStorage;
	static private storage localStorage;
	static private String lsf;

	static {
		tempStorage=new storage();
	}
	public static void initStorageService(Application context){
		localStorage=new storage(){
			public void putAll(Map<String,Object> map){
				super.putAll(map);
				save();
			}
			public void removeKeys(ArrayList<String> keys){
				super.removeKeys(keys);
				save();
			}
			public void clear(){
				super.clear();
				save();
			}
			public void save(){
				FileOutputStream outStream=null;
				try{
					JSONObject object=new JSONObject(storages);
					byte[] kb = Function.MD5_GetByteS("storage" + Configure.macAddress);
					byte[] sb=object.toString().getBytes();
					sb = AESEncodeDecode.AESEncode(sb, kb);
					outStream=mContext.openFileOutput(lsf,Context.MODE_PRIVATE);
					outStream.write(sb);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally{
					if(outStream!=null)
						try {
							outStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}
		};

		byte[] kb = Function.MD5_GetByteS("storage" + Configure.macAddress);
		lsf=Function.MD5_GetString(AESEncodeDecode.AESEncode("storage".getBytes(), kb));
		mContext=context;
		FileInputStream inStream=null;
		try {
			inStream = context.openFileInput(lsf);
			byte[] ls=StreamUtil.toByteArray(inStream);
			ls = AESEncodeDecode.AESDecode(ls, kb);
			String js=new String(ls);
			JSONObject jsonobject=new JSONObject(js);
			localStorage.putAll(JSONtoMap(jsonobject));
		} catch (Exception e) {
		}
		finally{
			if(inStream != null)
				try {
					inStream.close();
				} catch (IOException e) {
				}
		}
	}
	
	static protected void initFunction(final LuaState luaState){
		try {
			JavaFunction jf=new JavaFunction(luaState){
				@Override
				public int execute() throws LuaException {
					luaState.pushObjectValue(tempStorage);
					return 1;
				}
			};
			jf.register("getTempStorage");
			
			jf=new JavaFunction(luaState){
				@Override
				public int execute() throws LuaException {
					luaState.pushObjectValue(localStorage);
					return 1;
				}
			};
			jf.register("getLocalStorage");
		
		} catch (LuaException e) {
			e.printStackTrace();
		}
	}
	
	
	static private HashMap<String,Object> JSONtoMap(JSONObject jsonObject){
		HashMap<String,Object> result=new HashMap<String,Object>();
		Iterator<String> iterator=jsonObject.keys();
		try {
			while (iterator.hasNext()) {
				String key = iterator.next();
				Object value=jsonObject.get(key);
				if(value instanceof JSONObject)
					value=JSONtoMap((JSONObject) value);
				else if(value instanceof JSONArray)
					value=JSONtoArray((JSONArray) value);
				result.put(key, value);
			}
		} catch (JSONException e) {
		}
		return result;
	}
	static private ArrayList<Object> JSONtoArray(JSONArray jsonArray){
		ArrayList<Object> result=new ArrayList<Object>();
		try {
        	for(int i=0;i<jsonArray.length();i++){
        		Object value = jsonArray.get(i);
        		if(value instanceof JSONObject)
					value=JSONtoMap((JSONObject) value);
				else if(value instanceof JSONArray)
					value=JSONtoArray((JSONArray) value);
        		result.add(value);
        	}
		} catch (JSONException e) {
    	}
		return result;
	}
	
	static protected class storage{
		final protected HashMap<String,Object> storages;
		public storage(){
			storages=new HashMap<String,Object>();
		}
		public LuaProvider.luaProviderObject get(String key){
			return new LuaProvider.luaProviderObject(storages.get(key));
		}
		public Object getObject(String key){
			return storages.get(key);
		}
		public void put(String key,Object value){
			if(value instanceof LuaObject){
				try {
					value=LuaProvider.toObject((LuaObject) value);
				} catch (LuaException e) {
					e.printStackTrace();
				}
			}
			storages.put(key, value);
		}
		public void remove(String key){
			storages.remove(key);
		}
		public LuaProvider.luaProviderObject getValues(LuaObject object){
			try {
				Object keys=LuaProvider.toObject(object);
				if(keys instanceof ArrayList)
					return new LuaProvider.luaProviderObject(getValues((ArrayList<String>)keys));
				else 
					LogUtil.e("storage getValues error,kes=",keys.toString());
			} catch (LuaException e) {
				e.printStackTrace();
			}
			return null;
		}
		public Map getValues(ArrayList<String> keys){
			HashMap<String,Object> values=new HashMap<String,Object>();
			for(String key:keys){
				values.put(key,storages.get(key));
			}
			return values;
		}
		public void putAll(LuaObject object){
			try {
				Object map=LuaProvider.toObject(object);
				if(map instanceof Map)
					putAll((Map<String,Object>) map);
				else 
					LogUtil.e("storage putAll error,map=",map.toString());
			} catch (LuaException e) {
				e.printStackTrace();
			}
		}
		public void putAll(Map<String,Object> map){
			storages.putAll(map);
		}
		public void removeKeys(LuaObject object){
			try {
				Object keys=LuaProvider.toObject(object);
				if(keys instanceof ArrayList)
					removeKeys((ArrayList<String>) keys);
				else 
					LogUtil.e("storage removeKeys error,keys=",keys.toString());
			} catch (LuaException e) {
				e.printStackTrace();
			}
		}
		public void removeKeys(ArrayList<String> keys){
			for(String key:keys)
				storages.remove(key);
		}
		public void clear(){
			storages.clear();
		}
	}
}
