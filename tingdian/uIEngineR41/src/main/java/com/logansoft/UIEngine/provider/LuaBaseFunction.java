package com.logansoft.UIEngine.provider;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logansoft.UIEngine.parse.XmlParser;

import android.util.Log;

public class LuaBaseFunction {
	static protected void initFunction(final LuaState luaState){
		try {
			JavaFunction jf=new JavaFunction(luaState){
				@Override
				public int execute() throws LuaException {
					luaState.pushBoolean(false);
					return 1;
				}
			};
			jf.register("isIOS");
			
			jf=new JavaFunction(luaState){
				@Override
				public int execute() throws LuaException {
					luaState.pushBoolean(true);
					return 1;
				}
			};
			jf.register("isAndroid");
			
			jf=new JavaFunction(luaState){
				@Override
				public int execute() throws LuaException {
					Object object=luaState.getLuaObject(-1);
					if(object instanceof LuaObject){
						try {
							Log.d("LogUtil-debug",LuaProvider.toObject((LuaObject) object).toString());
						} catch (LuaException e) {
							e.printStackTrace();
						}
					}else if(object instanceof Document){
						Log.d("LogUtil-debug",XmlParser.DocumentToString(((Document)object).getDocumentElement()));
					}    	
					else if(object instanceof Element){
						Log.d("LogUtil-debug",XmlParser.DocumentToString((Element)object));
					}
					else if(object==null)
						Log.d("LogUtil-debug","null");
					else
						Log.d("LogUtil-debug",object.toString());
					return 0;
				}
			};
			jf.register("log");
			
			jf=new JavaFunction(luaState){
				@Override
				public int execute() throws LuaException {
					Object object=luaState.getLuaObject(-1);
					if(object instanceof LuaObject){
						try {
							object=LuaProvider.toObject((LuaObject) object);
						} catch (LuaException e) {
							e.printStackTrace();
						}
					}
					luaState.pushString(object.getClass().getSimpleName());
					return 1;
				}
			};
			jf.register("getClassName");
		
		} catch (LuaException e) {
			e.printStackTrace();
		}
	}
}
