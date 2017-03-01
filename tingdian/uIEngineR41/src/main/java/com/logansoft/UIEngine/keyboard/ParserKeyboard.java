
package com.logansoft.UIEngine.keyboard;

import com.logansoft.UIEngine.utils.FileUtil;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

/**
 * 完成对密码键盘字典的解析、密码字符串加密
 * 
 * @author bincom
 */
public class ParserKeyboard {

    public static final String localPopupTabPath = "localxml/dictionary.xml";

    public static final String KEYFORDICTIONARY = "7011240A82CEABCD";

    public FormObj formobj = null;

    public boolean isload = false;

    // public static private ParserKeyboard keboard = null;
    //
    //
    // public void
    public static ParserKeyboard instance = null;

    public int size = 0;

    public static ParserKeyboard getInstance() {
        if (instance == null) {
            instance = new ParserKeyboard();
            instance.formobj = new FormObj();
        }
        return instance;
    }

    /**
     * 获取密码对应字典的字符串
     * 
     * @param type 字典类型
     * @param arraytemp 字符相应编码集合
     * @return
     */
    public static String GetPassWord(String type, List<Integer> arraytemp) {

        SimpleObj obj = ParserKeyboard.getInstance().formobj.form.get(type);
        if (obj == null)
            return null;
        StringBuilder builder = new StringBuilder();
        builder.append(type + '|');
        String value = null;
        for (Integer item : arraytemp) {
            String temp = Character.toString(((char)item.intValue()));
            value = obj.map.get(temp);
            builder.append(value + '#');
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static String getDecodeStr(String code) {
        String[] codes = code.split("\\|");
        String type = codes[0];
        SimpleObj obj = ParserKeyboard.getInstance().formobj.form.get(type);
        if (obj == null)
            return null;
        String[] arraytemp = codes[1].split("#");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arraytemp.length; i++) {
            Set<String> keys = obj.map.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = obj.map.get(key);
                if (arraytemp[i].equals(value)) {
                    sb.append(key);
                }
            }
        }
        String decode = sb.toString();
        return decode;
    }

    public static String encodeStr(String str, String clientId) {

        String key = clientId + "0000000000000000";
        key = key.substring(0, 16);
        // key = Function.MD5_GetString(clientId);
        // Log.i("array2", "key|"+key);
        try {
            byte[] content = str.getBytes("utf-8");
            byte[] keybyte = key.getBytes("utf-8");
            byte[] publicbyte = "0123456789ABCDEF".getBytes("utf-8");
            byte[] encode = AESEncodeDecode.AESEncodeByIv(content, keybyte, publicbyte);
    		String codestr = StringUtil.bytesToHexString(encode);
            return codestr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decodeStr(String str, String clientId) {

        String key = clientId + "0000000000000000";
        key = key.substring(0, 16);
        try {
            byte[] content = StringUtil.hexStringToByte(str);
            byte[] keybyte = key.getBytes("utf-8");
            byte[] decode = AESEncodeDecode.AESDecode(content, keybyte);

            String codestr = new String(decode);
            return codestr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ParserKeyboard() {
        // mcontext=context;
    }

    // public Context mcontext = null;

    /**
     * 获取密码键盘字节流
     * 
     * @param url 密码键盘文件
     * @param mcontext
     * @return
     */
    public byte[] openfilestream(String url, Context mcontext) {
        int buffer_size = 1024;

        int ch;

        byte[] array = null;

        try {
            InputStream stream = mcontext.getAssets().open(url);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[buffer_size];

            while ((ch = stream.read(buffer)) != -1) {

                outputStream.write(buffer, 0, ch);

            }

            String temp = outputStream.toString();

            byte[] content = StringUtil.hexStringToByte(temp);

            byte[] key = KEYFORDICTIONARY.getBytes("utf-8");
            //array = AESEncodeDecode.AESDecode(content, key);
            byte[] publickey = "0123456789ABCDEF".getBytes("utf-8");
            
            array=AESEncodeDecode.AESDecodeByIV(content, key,publickey );
            //LogUtil.i("*******************"+new String(array));
            // Log.i("formitem", new String(array,"utf-8"));

            stream.close();

            outputStream.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return array;
    }

    public String handleAttr(Node tempnode) {
        NamedNodeMap map = tempnode.getAttributes();
        int attrSize = map.getLength();
        if (attrSize == 1) {
            return map.item(0).getNodeValue();
        }
        return null;
    }

    // public void handleItemAttr(Node tempnode,SimpleObj simpleobj)
    // {
    // NamedNodeMap map = tempnode.getAttributes();
    // if(map.getLength()==2)
    // {// 得到的是node的属性节点
    // String value = map.item(0).getNodeValue();
    // String key = map.item(1).getNodeValue();
    // simpleobj.map.put(key, value);
    // }
    // }

    /**
     * 解析密码字典，并生成字典结构赋值给 formobj
     * 
     * @param array 密码键盘字节流
     * @return
     */
    public boolean parseDataSourcesXML(byte[] array) {
        boolean returnvalue = false;
        if (array == null || array.length == 0) {
            return returnvalue;
        }
        ByteArrayInputStream arrayStream = null;

        try {

            DocumentBuilder builder = DocumentFactory.getInstance().newDocumentBuilder();

            arrayStream = new ByteArrayInputStream(array);

            Document dom = builder.parse(arrayStream);

            Element rootElement = dom.getDocumentElement();

            NodeList items = rootElement.getChildNodes();

            // items = ((Element) items.item(0)).getChildNodes();
            if (items == null) {
                return returnvalue;
            }
            Node nodechild = null;
            String itemvalue, itemkey = null;

            for (int i = 0; i < items.getLength(); i++) {

                nodechild = items.item(i);
                if (nodechild == null || nodechild.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                if (nodechild.getNodeName().equals("dictionary")) {
                    String key = handleAttr(nodechild);
                    if (key != null) {
                        NodeList nodelistitems = nodechild.getChildNodes();
                        int length = nodelistitems.getLength();
                        // if(length!=10)
                        // continue;
                        SimpleObj simpleobj = new SimpleObj();
                        for (int index = 0; index < length; index++) {

                            Node nodeitem = nodelistitems.item(index);
                            if (nodeitem == null || nodeitem.getNodeType() != Node.ELEMENT_NODE)
                                continue;
                            NamedNodeMap map = nodeitem.getAttributes();
                            // Log.i("formitem", "map length:"+map.getLength());
                            if (map.getLength() == 2) {
                                itemvalue = map.item(0).getNodeValue();
                                itemkey = map.item(1).getNodeValue();
                                simpleobj.map.put(itemkey, itemvalue);
                                // Log.i("formitem", "key:" + itemkey +
                                // "|value:"
                                // + itemvalue);
                            }

                        }
                        formobj.form.put(key, simpleobj);
                        // Log.i("form", "key:" + key);
                    }
                    isload = true;
                    // mcontext = null;
                    // formobj.destory();
                }
            }
            size = ParserKeyboard.getInstance().formobj.form.size();
            items = null;
            rootElement = null;
            dom = null;
            nodechild = null;
            builder = null;
            returnvalue = true;

        } catch (SAXException se) {
            se.printStackTrace();

        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (arrayStream != null) {
                try {
                    arrayStream.close();
                    arrayStream = null;
                } catch (IOException e) {

                }

            }
        }
        return returnvalue;

    }

    public static void clean() {
        if (instance != null) {
            instance.destory();
            instance = null;
        }

    }

    public void destory() {
        isload = false;
        if (formobj != null) {
            formobj.destory();
            formobj = null;
        }

    }
}
