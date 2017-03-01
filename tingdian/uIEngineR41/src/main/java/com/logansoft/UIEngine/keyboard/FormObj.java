package com.logansoft.UIEngine.keyboard;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cyt on 2017/2/28.
 */

public class FormObj extends SimpleObj {
    public HashMap<String,SimpleObj> form;

    public FormObj(){
        super();
        form = new HashMap<>();
    }

    public FormObj(String nameId){
        if(nameId.equals("DIPLAYFORM"))
        {
            form = new HashMap<String,SimpleObj>();
        }
    }

    @Override
    public void destory()
    {
        super.destory();
        for(Map.Entry<String,SimpleObj> item:form.entrySet())
        {
            item.getValue().destory();

        }
        form.clear();
        form = null;
    }
    public void destoryform() {
        for(Map.Entry<String,SimpleObj> item:form.entrySet())
        {
            item.getValue().destory();
        }
        form.clear();
    }
}
