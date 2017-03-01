
package com.logansoft.UIEngine.utils;


import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

//import app.util.AESEncodeDecode;
import hdz.base.Function;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import com.logansoft.UIEngine.keyboard.AESEncodeDecode;

public class StringUtil {

    public static String getMD5Code(String str) {
        return MD5.toMD5(str);
    }

    
    public static String ToDBC(String input) {   
        char[] c = input.toCharArray();   
        for (int i = 0; i < c.length; i++) {   
            if (c[i] == 12288) {   
                c[i] = (char) 32;   
                continue;   
            }   
            if (c[i] > 65280 && c[i] < 65375)   
                c[i] = (char) (c[i] - 65248);   
        }   
        return new String(c);   
    }  
    public static boolean isEmpty(String s) {
        return (s == null || "".equals(s));
    }

    // public static boolean isTrue(String in) {
    // if(!isEmpty(in) && MapProperty.TRUE.equalsIgnoreCase(in)){
    // return true;
    // }
    // return false;
    // }
    //

    public static String defaultString(String s) {
        return (s == null ? "" : s);
    }

    /**
     * compare s1 and s2
     * 
     * @param s1
     * @param s2
     * @return -1, 0, or 1 when s1 LT , EQ or GT s2
     */
    public static int compare(final String s1, final String s2) {
        int n = s1.length() > s2.length() ? s2.length() : s1.length();
        for (int i = 0; i < n; i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 > c2) {
                return 1;
            } else if (c1 < c2) {
                return -1;
            }
        }
        if (s1.length() < s2.length()) {
            return -1;
        } else if (s1.length() > s2.length()) {
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean compareStr(String s1, String s2) {
        if (s1 == null || s2 == null)
            return false;
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        if (s1.equals(s2)) {
            return true;
        }
        return false;
    }

    public static String getFileExt(String filename) {
        int index = filename.lastIndexOf('.');
        if (index != -1 && index < (filename.length() - 1)) {
            return filename.substring(index + 1, filename.length());
        } else {
            return "";
        }
    }

    public static Vector removeLine(String str) {
        String eof = "\n";
        Vector v = new Vector();
        if (str.indexOf(eof) == -1) {
            v.addElement(str);
            return v;
        }

        int idx = -1;
        while ((idx = str.indexOf(eof)) != -1 && idx < str.length()) {
            v.addElement(str.substring(0, idx));
            str = str.substring(idx + eof.length(), str.length());
        }
        if ((idx = str.indexOf(eof)) == -1) {
            v.addElement(str);
        }
        return v;
    }

    public static String replace(String source, String dest, String replace) {
        StringBuffer s = new StringBuffer();

        int idx = -1;
        while ((idx = source.indexOf(dest)) != -1) {
            s.append(source.substring(0, idx));
            s.append(replace);
            source = source.substring(idx + dest.length(), source.length());
        }
        s.append(source.substring(0, source.length()));
        return s.toString();
    }

    // 从类似URL参数格式的 param1=v1&param2=v2&...中把参数值提取出来
    public static String getParameter(String s, String param) {
        String result = "";
        if (s.indexOf(param) != -1) {
            int idx = s.indexOf(param) + param.length() + 1;
            String sub = s.substring(idx, s.length());
            if (sub.indexOf("&") != -1) {
                int phoneIndex = s.indexOf("&");
                result = s.substring(idx, phoneIndex);
            } else {
                result = s.substring(idx, s.length());
            }
        }
        return result;
    }

    public static byte[] readAllBytesQuickly(InputStream is) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bufSize = 1024;
        byte[] buf = new byte[bufSize];
        int read = -1;
        while ((read = is.read(buf)) != -1) {
            os.write(buf, 0, read);
        }

        return os.toByteArray();

    }

    public static byte[] readAllBytes(InputStream is) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int ch;
        while ((ch = is.read()) != -1) {
            os.write(ch);
        }

        return os.toByteArray();
    }

    public static String getStringFromStream(InputStream is, String enc) throws Exception {
        return new String(readAllBytes(is), enc);
    }

    public static void copyArray2Vector(Vector v, final String[] s) {
        for (int i = 0; i < s.length; i++) {
            v.addElement(s[i]);
        }
    }

    public static byte[] getBytes(String str, String enc) {
        try {
            return str.getBytes(enc);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] getBytesUtf8(String str) {
        return getBytes(str, "UTF-8");
    }

    // public static String toSizevalueOnVertical(String params) {
    // return toSizevalue(params,MAPPHONE.Activity_height);
    // }
    //
    //
    // public static String toSizevalueOnHorizonal(String params) {
    // return toSizevalue(params,MAPPHONE.Activity_width);
    // }

    public static String toSizevalue(String params, String default_value) {
        return toSizevalue(params, Integer.parseInt(default_value));
    }

    // //以下主要是针对一些百分号变量进行数值的转化
    // public static String toSizevalue(String params,int default_value) {
    //
    // try{
    // String temp=params.toLowerCase();
    // if(temp.contains(MapProperty.pagewidth) ||
    // temp.contains(MapProperty.pageheight)) {
    // return String.valueOf(handlePageSize(temp));
    // }
    // else
    // if(params.lastIndexOf("%")==params.length()-1){
    // int value=Integer.parseInt(params.replace("%", ""))*default_value/100;
    // return String.valueOf(value);
    // }
    // else{
    // return params;
    // }
    // }
    // catch(Exception e) {
    // e.printStackTrace();
    // }
    // return params;
    //
    // }

    // 以下主要是针对一些百分号变量进行数值的转化
    public static String toSizevalue(String params, int default_value) {

        try {
            String temp = params.toLowerCase();
            if (params.lastIndexOf("%") == params.length() - 1) {
                int value = Integer.parseInt(params.replace("%", "")) * default_value / 100;
                return String.valueOf(value);
            }
            // else if(isDigit(params)){
            // return params;
            // }
            // else if(temp.contains(MapProperty.pagewidth) ||
            // temp.contains(MapProperty.pageheight)) {
            // return String.valueOf(handlePageSize(temp));
            // }
            else
                return params;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;

    }

    static boolean isDigit(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // //新增对pagewidth跟pageheight的支持
    // static int handlePageSizeWithIndex(String s){
    //
    // String[] fuhao=new String[]{"+","-","*","/"};
    //
    // int index=s.toLowerCase().indexOf(MapProperty.pagewidth);
    // if(index==-1){
    // index=s.toLowerCase().indexOf(MapProperty.pageheight);
    // if(index==-1) {
    // return 1;
    // }
    // }
    //
    // for(int i=0;i<fuhao.length;i++) {
    // String fh=fuhao[i];
    // if(s.contains(fh)) {
    // double leftValue=1;
    // double rightValue=1;
    // index=s.indexOf(fh);
    // String left=s.substring(0,index);
    // String right=s.substring(index+1, s.length());
    // if(left.equalsIgnoreCase(MapProperty.pagewidth)) {
    // leftValue=MAPPHONE.Activity_width;
    // }
    // else if(left.equalsIgnoreCase(MapProperty.pageheight)) {
    // leftValue=MAPPHONE.Activity_height;
    //
    // }
    // else{
    // leftValue=Double.parseDouble(left);
    // }
    //
    // if(right.equalsIgnoreCase(MapProperty.pagewidth)) {
    // rightValue=MAPPHONE.Activity_width;
    // }
    // else if(right.equalsIgnoreCase(MapProperty.pageheight)) {
    // rightValue=MAPPHONE.Activity_height;
    // }
    // else{
    // rightValue=Double.parseDouble(right);
    // }
    // return (int)(leftValue*rightValue);
    // }
    //
    // }
    //
    // return 1;
    // }
    //

    //
    // public static int handlePageSize(String s) {
    //
    // String[] fuhao=new String[]{"\\+","\\-","\\*","\\/"};
    // s=s.toLowerCase();
    // if(s.indexOf(MapProperty.pagewidth)==-1 &&
    // s.indexOf(MapProperty.pageheight)==-1){
    // return LayoutParams.WRAP_CONTENT;
    // }
    //
    // for(int i=0;i<fuhao.length;i++) {
    // String fh=fuhao[i];
    // String[] temp=s.split(fh);
    // if(temp!=null && temp.length==2) {
    // double leftValue=1;
    // double rightValue=1;
    // String left=temp[0];
    // String right=temp[1];
    // if(left.equalsIgnoreCase(MapProperty.pagewidth)) {
    // leftValue=MAPPHONE.Activity_width;
    // }
    // else if(left.equalsIgnoreCase(MapProperty.pageheight)) {
    // leftValue=MAPPHONE.Activity_height;
    //
    // }
    // else{
    // leftValue=Double.parseDouble(left);
    // }
    //
    // if(right.equalsIgnoreCase(MapProperty.pagewidth)) {
    // rightValue=MAPPHONE.Activity_width;
    // }
    // else if(right.equalsIgnoreCase(MapProperty.pageheight)) {
    // rightValue=MAPPHONE.Activity_height;
    // }
    // else{
    // rightValue=Double.parseDouble(right);
    // }
    // return (int)(leftValue*rightValue);
    // }
    //
    // }
    //
    // return LayoutParams.WRAP_CONTENT;
    //
    // }

    public static int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        FontMetrics fm = paint.getFontMetrics();
        return (int)Math.ceil(fm.descent - fm.top) + 2;
    }

    public static float getFontWidth(String str, float fontSize) {
        Paint mTextPaint = new Paint();
        mTextPaint.setTextSize(fontSize);
        float textWidth = mTextPaint.measureText(str);
        return textWidth;
    }

    public static String[] handleProperty(String value) {
        // 用正则表达式进行数据的拆解，然后进行x、y的赋值
        try {
            if (!StringUtil.isEmpty(value)) {
                String[] direction = value.split(",");
                return direction;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String byteToStr(byte[] data) {
        StringBuilder builer = new StringBuilder();
        for (int k = 0; k < data.length; k++) {
            String hex = Integer.toHexString(data[k] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            builer.append(hex);
        }
        return builer.toString();
    }

    /**
     * 将byte数组转为16进制字符串
     * 
     * @param byte[]
     * @return HexString
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制字符串转为byte数组
     * 
     * @param hexString
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte)(toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte)"0123456789ABCDEF".indexOf(c);
        return b;
    }

    /*
     * 毫秒转化为时间格式
     */
    public static String formatMilliSecondToTime(long ms) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        String strDay = day < 10 ? "0" + day : "" + day; // 天
        String strHour = hour < 10 ? "0" + hour : "" + hour;// 小时
        String strMinute = minute < 10 ? "0" + minute : "" + minute;// 分钟
        String strSecond = second < 10 ? "0" + second : "" + second;// 秒
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;// 毫秒
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;

        return strHour + ":" + strMinute + ":" + strSecond;
    }

    /*
     * 毫秒转化
     */
    public static long formatTimeToMilliSecond(String time) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        String[] t = time.split(":");
        long th = Integer.parseInt(t[0]) * hh;
        long tm = Integer.parseInt(t[1]) * mi;
        long ts = Integer.parseInt(t[2]) * ss;
        
        return ts + tm + th;
    }
    
    public static String stringEncryptToString(String content , String key){
        byte[] b = Function.MD5_GetByteS(key);
        byte[] a = AESEncodeDecode.AESEncode(content.getBytes(), b);
        return bytesToHexString(a);
    }
    

}
