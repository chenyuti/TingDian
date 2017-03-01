package com.logansoft.UIEngine.parse.UIEntity;

import java.util.Map;

public class UnLoadLua {
    String luaStr;
    Map<String, Object> map;
    public UnLoadLua(String luaStr, Map<String, Object> map) {
        this.luaStr = luaStr;
        this.map = map;
    }
    public String getLuaStr() {
        return luaStr;
    }
    public Map getMap() {
        return map;
    }
 
}
