
package com.logansoft.UIEngine.parse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.keyboard.AESEncodeDecode;
import com.logansoft.UIEngine.parse.UIEntity.VersionInfo;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.BaseViewFactory;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.PageView;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.FileUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.Statistics;
import com.logansoft.UIEngine.utils.StreamUtil;

//import app.util.AESEncodeDecode;
import hdz.base.Function;

public class XmlParser {
    
    private static XmlParser sParser = new XmlParser();


    public static XmlParser getParser() {
        return sParser;
    }

    public PageView readXMLByDOM(String str, BaseFragment fragment) {
        PageView rootView = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(StreamUtil.stringToInStream(str));
            NodeList elements = doc.getChildNodes();
            Element mElement = (Element)elements.item(0);
            rootView = new PageView(fragment,null,mElement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    public String getStrFromInputSteam(InputStream in) throws IOException{ 
    	
        BufferedReader bf=new BufferedReader(new InputStreamReader(in,"UTF-8"));  
        //最好在将字节流转换为字符流的时候 进行转码  
        StringBuffer buffer=new StringBuffer();  
        String line="";  
        
        while((line=bf.readLine())!=null){  
            buffer.append(line);  
        }  
          
       return buffer.toString();  
   }  
    
    /**
     * 解析返回的数据XML
     * 
     * @param inStream
     * @return
     */
    public HashMap<String, Object> readDataByDOM(InputStream inStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inStream);
            doc.normalize();
            return readDataByDOM(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
        	try {
  				inStream.close();
  			} catch (IOException e1) {
  				e1.printStackTrace();
  			}
        }
        return null;
    }
    
    public HashMap<String, Object> readDataByDOM(Document doc) {
        Element rootElement = doc.getDocumentElement();
        if (rootElement == null) {
        	LogUtil.i("readDataByDOM rootElenment is null");
        	return null;
        }
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        NodeList dataNodes = rootElement.getElementsByTagName(GlobalConstants.XML_DATA);
        for (int i = 0; i < dataNodes.getLength(); i++) {
        	Node node = dataNodes.item(i);
        	if (GlobalConstants.XML_DATA.equals(node.getNodeName())) {
        		parserResultData((Element)node, dataMap);
        	}
        }
        return dataMap;
    }
    
    /**
     * 解析返回的数据
     * 
     * @param childNode
     * @param dataMap
     * @return 解析后的数据集合 HasMap<String,Object>
     */
    private HashMap<String, Object> parserResultData(Element childNode,
            HashMap<String, Object> dataMap) {
        HashMap<String, Object> map = null;
        HashMap<String, String> hiddenMap = null;
        /* 清除空节点 */
        childNode.normalize();
        /* 获取子节点列表 */
        NodeList groupNodes = childNode.getChildNodes();
        for (int a = 0; a < groupNodes.getLength(); a++) {
            Node groupNode = groupNodes.item(a);
            if (Node.ELEMENT_NODE == groupNode.getNodeType()) {
                Element element = ((Element)groupNode);
                HashMap<String, String> attr = parseElementAttr(element);
                if (map == null) {
                    map = new HashMap<String, Object>();
                }

                if (GlobalConstants.XML_ITEM.equals(groupNode.getNodeName())) {
                    String id = attr.get(BaseView.ATTR_ID);
                    map.put(id, attr);
                } else if (GlobalConstants.XML_LIST.equals(groupNode.getNodeName())) {
                    String id = attr.get(BaseView.ATTR_ID);
                    map.put(id, element);
                    /* 解析版本 */
                } else if (VersionInfo.VERSION_UPDATE_CONTENT.equals(groupNode.getNodeName())) {
                    VersionInfo.instance().setUpdateContent(parseUpdateContent(element));
                } else if (VersionInfo.VERSION_APP_COMPEL_UPDATA.equals(groupNode.getNodeName())) {
                    VersionInfo.instance().setAppCompelUpdata(
                            (groupNode.getTextContent() == "true" ? true : false));
                } else if (VersionInfo.VERSION_APP_UPDATE_TIME.equals(groupNode.getNodeName())) {
                    VersionInfo.instance().setAppUpdateTime(groupNode.getTextContent());
                } else if (VersionInfo.VERSION_APP_URL.equals(groupNode.getNodeName())) {
                    VersionInfo.instance().setAppUrl(groupNode.getTextContent());
                } else if (VersionInfo.VERSION_APP_VERSION.equals(groupNode.getNodeName())) {
                    VersionInfo.instance().setAppVersion(groupNode.getTextContent());
                } else if (GlobalConstants.XML_HIDDEN.equals(groupNode.getNodeName())) {
                    if (hiddenMap == null) {
                        hiddenMap = new HashMap<String, String>();
                    }
                    hiddenMap.put(element.getAttribute("id"), element.getAttribute("value"));
                }
            }
        }
        dataMap.put("data", map);
        dataMap.put("hidden", hiddenMap);
        return dataMap;
    }
    
    /**
     * 解析UI
     * 
     * @param inStream
     */
    public PageView readXMLByDOM(InputStream inStream, BaseFragment fragment) {
        long start=System.nanoTime();
        /* 获得构造器工厂 */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        PageView rootView = null;
        try {
            /* 获得构造器 */
            DocumentBuilder builder = factory.newDocumentBuilder();
            /* 构造器解析流,并获得DOM对象 */
            long start2=System.nanoTime();
        	LogUtil.d(Statistics.TAG,"start parse");

            Document doc = builder.parse(inStream);
            long end2=System.nanoTime();
           	LogUtil.d(Statistics.TAG,"DocumentBuilder.parse() cost:"+ (end2-start2)/1000000.0+"ms");
           	
//           	Document ddoc=builder.newDocument();
//            inStream.reset();
//            long start13=System.nanoTime(); 
//            createXMLWithString(inStream,ddoc,4);
//            long end13=System.nanoTime();
//            LogUtil.d(Statistics.TAG,"tinyXML-parse() cost:"+ (end13-start13)/1000000.0+"ms");
//            
           // ddoc.createElement(tagName)
            //Statistics.show();
            //showStatisticsDebugInfo();
           
           // ddoc.printXML();

            /* 去掉空白 */
            doc.normalize();// 去掉不必要的空白
            /* 通过 节点名称(content) 获取 节点名称为(content)的 所有节点 的列表 */
            NodeList nodes = doc.getElementsByTagName("page");
            if (nodes.getLength() > 1) {
                throw new IllegalStateException("It can't contain more than one content node.");
            }
            /* 获取节点 */
            Element rootElement = (Element)nodes.item(0);

            long end=System.nanoTime();
        	LogUtil.d(Statistics.TAG,"parse xml  cost:"+ (end-start)/1000000.0+"ms");


            rootView = new PageView(fragment,null,rootElement);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return rootView;
    }

    public InputStream getFileInputStream(String path, String fileName) {
    	FileInputStream is = null;
    	InputStream result=null;
        long start=System.nanoTime();
        try {
            File file = new File(path, fileName);
            LogUtil.d(Statistics.TAG," fileName ->"+file.getAbsolutePath());
            is = FileUtil.readFileFromSDCard(file);
            FileChannel channel=is.getChannel();
            long bytecount=channel.size();
            if(bytecount> Integer.MAX_VALUE)
				throw new Exception("could not read too large file");
            int byteCount=(int)bytecount;
            Statistics.showMemoryInfo();
            byte[] content =new byte[byteCount]; 	

            int offest=0;
            ByteBuffer byteBuffer=ByteBuffer.allocate(4096);
            long startread=System.nanoTime();
            while(channel.read(byteBuffer)!=-1){
            	byteBuffer.flip();
            	int limit=byteBuffer.limit();
            	
            	System.arraycopy(byteBuffer.array(),0,content,offest,limit);
            	offest+=limit;
            	byteBuffer.clear();
            }
			is.close();

            long endread=System.nanoTime();
			LogUtil.i(Statistics.TAG,"read "+(endread-startread)/1000000.0+"ms");
//            LogUtil.i(" 加密时的content " + new String(content));
            byte[] decodeContent = AESEncodeDecode.AESDecode(content,  Function.MD5_GetByteS("AD_" + Configure.macAddress));
            content=null;
            Statistics.showMemoryInfo();

            result = new ByteArrayInputStream(decodeContent);
   //         LogUtil.i("getFileInputStream() end");

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
        long end=System.nanoTime();
        Statistics.showMemoryInfo();
    	LogUtil.d(Statistics.TAG,"getFileInputStream cost:"+ (end-start)/1000000.0+"ms");

        return result;
    }

    public void checkUpdate(Document doc){
    	doc.normalize();
    	NodeList dataNodes = doc.getElementsByTagName(GlobalConstants.XML_DATA);
    	for (int i = 0; i < dataNodes.getLength(); i++) {
    		Node node = dataNodes.item(i);
            NodeList groupNodes = node.getChildNodes();
            for (int a = 0; a < groupNodes.getLength(); a++) {
                Node groupNode = groupNodes.item(a);
                if (Node.ELEMENT_NODE == groupNode.getNodeType()) {
                    Element element = ((Element)groupNode);
                    if (VersionInfo.VERSION_UPDATE_CONTENT.equals(groupNode.getNodeName())) {
                        VersionInfo.instance().setUpdateContent(parseUpdateContent(element));
                    } else if (VersionInfo.VERSION_APP_COMPEL_UPDATA.equals(groupNode.getNodeName())) {
                        VersionInfo.instance().setAppCompelUpdata((groupNode.getTextContent() == "true" ? true : false));
                    } else if (VersionInfo.VERSION_APP_UPDATE_TIME.equals(groupNode.getNodeName())) {
                        VersionInfo.instance().setAppUpdateTime(groupNode.getTextContent());
                    } else if (VersionInfo.VERSION_APP_URL.equals(groupNode.getNodeName())) {
                        VersionInfo.instance().setAppUrl(groupNode.getTextContent());
                    } else if (VersionInfo.VERSION_APP_VERSION.equals(groupNode.getNodeName())) {
                        VersionInfo.instance().setAppVersion(groupNode.getTextContent());
                    }
                }
            }           
        }
    }
    /**
     * 解析更新内容
     * 
     * @param childNode
     * @return
     */
    public  List<String> parseUpdateContent(Element childNode) {
        List<String> contents = new ArrayList<String>();
        NodeList groupNodes = childNode.getChildNodes();
        for (int a = 0; a < groupNodes.getLength(); a++) {
            Node groupNode = groupNodes.item(a);
            if (Node.ELEMENT_NODE == groupNode.getNodeType()) {
                Element element = ((Element)groupNode);
                String content = element.getAttribute(VersionInfo.VERSION_UPDATE_CONTENT);
                contents.add(content);
            }
        }
        return contents;
    }



    /**
     * 将元素以键值对返回
     * 
     * @param element
     * @return
     */
    public HashMap<String, String> parseElementAttr(Element element) {
    	if(element==null)
    		LogUtil.e("parseElementAttr element null!");
        HashMap<String, String> attrMap = new HashMap<String, String>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attrr = attrs.item(i);
            String n = attrr.getNodeName();
            String v = attrr.getNodeValue();
            attrMap.put(n, v);
        }
        return attrMap;
    }

    /**
     * @param fragment
     * @param groupView
     * @param element
     * @param manager
     */
    public void parseChildElement(BaseFragment fragment, GroupView groupView, Element element) {
        // BaseViewInfo info =new BaseViewInfo(fragment, groupView, element,
        // manager);
        // viewToAsyncTask.executeOnExecutor(ThreadUtil.sThreadPool, info);

        /* 创建 List 用来保存 子元素 */
        NodeList groupNodes = element.getChildNodes();
        for (int a = 0; a < groupNodes.getLength(); a++) {
            Node groupNode = groupNodes.item(a);
            /* 如果这个节点是一个可用的元素,添加进集合 */
            if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)groupNode;
                if (GlobalConstants.XML_GROUP.equals(groupNode.getNodeName()) ||
                		GlobalConstants.XML_ITEM.equals(groupNode.getNodeName())	) {
                    BaseView baseview = BaseViewFactory.newInstance(fragment,groupView,childElement);
                    if (baseview != null) {
                        groupView.addView(baseview);
                    }
                } else if (GlobalConstants.XML_DIALOG.equals(groupNode.getNodeName())) {
                    String style = childElement.getAttribute(GlobalConstants.ATTR_STYLE);
                    fragment.putDialogTemplate(style, childElement);
                } else if (GlobalConstants.XML_POPUPVIEW.equals(groupNode.getNodeName())) {
                    String style = childElement.getAttribute("id");
                    fragment.putDialogTemplate(style, childElement);
                }
            }
        }
    }


    public String elementToString(Document doc){
    	return elementToString(doc.getDocumentElement());
    }
    public String elementToString(Element childElement) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty("encoding", "utf-8");// 解决中文问题，试过用GBK不行
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            t.transform(new DOMSource(childElement), new StreamResult(bos));
            String xmlStr = bos.toString();
            return xmlStr;
        } catch (Exception e) {
        }
        return null;
    }
    public static String DocumentToString(Element childElement) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty("encoding", "utf-8");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            t.transform(new DOMSource(childElement), new StreamResult(bos));
            String xmlStr = bos.toString();
            return xmlStr;
        } catch (Exception e) {
        }
        return null;
    }
    public static String DocumentToString(Document doc){
    	return DocumentToString(doc.getDocumentElement());
    }
}
