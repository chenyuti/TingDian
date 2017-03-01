package com.logansoft.UIEngine.keyboard;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * ��ㄤ��瑙ｅ�充��浜�涓������т欢�����跺�������т欢灞���х��瀵硅薄锛�
 *       渚�濡�hidden���绛� droplistitem���绛� 
 *       杩�浜����绛惧�ㄨВ�����舵����存�ュ��������锛���垮����������т欢锛� ��惰�����绠＄�����com.citicbank.javascript.data; HiddenWrapper绫讳腑
 * @author bincom
 *
 */
public class SimpleObj {
	/**
	 * 灞���у�����map
	 */
	public HashMap<String,String> map;
	public SimpleObj()
	{
		map =new HashMap<String,String>();
	}
	
	public SimpleObj(String nameid)
	{
		map =new HashMap<String,String>();
		map.put("nameid", nameid);
	}
	
	public void setkey(String name,String key)
	{
		map.put(name, key);
	}
	
	public String getkey(String name)
	{
		String temp = map.get(name);
		if(temp == null)
			return "";
		return temp;
//	  return	map.get(name);
	}
	
	public void destory()
	{
		if(map!=null)
		map.clear();
		map = null;
//		postData = null;
//		display = null;
	}
//	/**
//	 * 椤甸�㈡��浜ゆ�剁�ㄥ��
//	 * @return
//	 */
// 	public int getPostDataLength() {
// 	      if(display.addPostData(
// 	    		  getkey(MapProperty.NAMEID),
// 	    		  getkey(MapProperty.VALUE)))
// 	      {
// 	    	  return 1;
// 	      }else
// 	      {
// 	    	  return 0;
// 	      }
// 	}

	public String getText() {
		// TODO Auto-generated method stub
		String temp = map.get("text");
		if(temp == null)
			return "";
		return temp;
	}
	
	public String getValue() {
		// TODO Auto-generated method stub
		//return map.get(MapProperty.VALUE);
		String temp = map.get("value");
		if(temp == null)
			return "";
		return temp;
	}
	
	
	public void setText(String text) {
		// TODO Auto-generated method stub
		map.put("text",text);
	}
	
	public void setValue(String value) {
		// TODO Auto-generated method stub
		 map.put("value",value);
	}

// 	public void writeToStream(OutputStream stream) {
// 		if(postData==null)
// 			return;
// 		try {
// 			stream.write(postData);
// 		} catch (IOException e) {
//
// 		}
// 	}
 	
// 	protected byte[] postData;//���瑕�post�����版��瀛�������

}
