package com.logansoft.UIEngine.utils.http.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;

public class UIEngineXMLRequest extends UIEngineRequest<Document> {
	public UIEngineXMLRequest(int method, String url,Map<String,Object> params) {
		super(method,url,params);
	}

	@Override
	protected Document parseResponse(byte[] data) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder;
	    ByteArrayInputStream byteStream=null;
		Document doc=null;
		try {
			builder = factory.newDocumentBuilder();
			byteStream=new ByteArrayInputStream(data);
			doc=builder.parse(byteStream);
			doc.normalize();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			if(Configure.DEBUG_MODE==true){
				try {
					LogUtil.e(new String(data,"UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			}
		}finally{
			if(byteStream!=null)
				try {
					byteStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		if(doc!=null ){
			//if(cacheResponse!=true){
				Element rootElement=doc.getDocumentElement();
				if(rootElement!=null){
					NodeList resultCodeNodes=rootElement.getElementsByTagName("resultCode");
					if(resultCodeNodes.getLength()>0){
						Node resultCode=resultCodeNodes.item(0);
						if(Node.ELEMENT_NODE == resultCode.getNodeType()){
							String resultCodeValue=resultCode.getTextContent();
							try{
								Integer code=Integer.valueOf(resultCodeValue);
								responseLuaArgs.put("resultCode",code);
							}
							catch(Exception e){
								responseLuaArgs.put("resultCode",resultCodeValue);
							}
						}
					}
					NodeList resultMsgNodes=rootElement.getElementsByTagName("resultMsg");
					if(resultMsgNodes.getLength()>0){
						Node resultMsg=resultMsgNodes.item(0);
						if(Node.ELEMENT_NODE == resultMsg.getNodeType()){
							responseLuaArgs.put("resultMsg",resultMsg.getTextContent());
						}
					}
					NodeList dataNodes = rootElement.getElementsByTagName(GlobalConstants.XML_DATA);
					if(dataNodes.getLength()>0){
						Node dataNode=dataNodes.item(0);
						if(Node.ELEMENT_NODE == dataNode.getNodeType()){
							Element dataElement=(Element)dataNode;
							String cache = dataElement.getAttribute(GlobalConstants.ATTR_CACHE);
							if (GlobalConstants.BOOLEANTRUE.equals(cache)) {
								writeCache(data);
							}
						}
					}
					
				}
			//}
		}
		return doc;
	}
}
