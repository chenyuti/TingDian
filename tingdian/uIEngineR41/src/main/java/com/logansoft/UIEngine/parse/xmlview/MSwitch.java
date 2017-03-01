package com.logansoft.UIEngine.parse.xmlview;

import java.util.Map;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.view.Switch.Switch;

import android.graphics.Canvas;
import android.support.v4.util.ArrayMap;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MSwitch extends BaseView{


    private Switch mSwitch;

    public MSwitch(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
    }
    @Override
    protected void createMyView(){
        mView = mSwitch =new Switch(mContext){
    		@Override
    		public void draw(Canvas canvas) {
    			bdraw(canvas);
    			super.draw(canvas);
    			adraw(canvas);
    		}
    	};
        mSwitch.setClickable(true);
    }
    @Override
    public void setSize(String sizeString) {
    }
    
    @Override
    public void setOnClick(final String luaStr) {
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<String, Object> map=new ArrayMap<String, Object>();
                map.put("isOn", isChecked);
                executeLua(luaStr,map);
            }
        });
    }
    
    /**
     * 是否是打开的,打开的返回true,否则false
     * @return
     */
    public boolean isChecked(){
       return  mSwitch.isChecked();
    }
    
    public void setChecked(boolean isChecked){
        mSwitch.setChecked(isChecked);
    }
}
