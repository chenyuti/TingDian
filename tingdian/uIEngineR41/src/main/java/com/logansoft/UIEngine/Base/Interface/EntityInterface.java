package com.logansoft.UIEngine.Base.Interface;

import java.util.Map;

public interface EntityInterface {
	public void setSelected(boolean selected);
	public boolean isSelected();
	public void setValue(String value);
	public String getValue();
	public void setValueForKey(String key,String value);
	public String getValueForKey(String key);
	public void setAttributes(Object attributeMap);
	public Map<String,String> getAttributes();
	public String getAttribute(String key);
}
