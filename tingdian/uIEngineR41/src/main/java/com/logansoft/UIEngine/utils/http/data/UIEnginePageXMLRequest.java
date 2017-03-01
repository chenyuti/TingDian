package com.logansoft.UIEngine.utils.http.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;

public class UIEnginePageXMLRequest extends UIEngineRequest<Document> {

	public UIEnginePageXMLRequest(int method, String url,Map<String, Object> params) {
		super(method, url, params);
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
		return doc;
	}
}
