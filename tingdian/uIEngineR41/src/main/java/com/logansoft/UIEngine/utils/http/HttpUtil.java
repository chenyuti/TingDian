
package com.logansoft.UIEngine.utils.http;

import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.LogUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * 网络连接类
 * 
 * @author zhang xing
 */
public class HttpUtil {

    /**
     * 判断网络是否连接
     * 
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * Socket 连接
     * 
     * @param hostName HOST
     * @param port
     * @param jsonStr
     * @return result of request network
     * @throws IOException
     */
    public static String request(String hostName, int port, String jsonStr) throws IOException {
        String result = "";
        String message = jsonStr;
        @SuppressWarnings("resource")
        Socket socket = new Socket(hostName, port);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream(), Charset.defaultCharset())), true);
        out.print(message);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                Charset.defaultCharset()));
        String buffer = null;
        StringBuffer sb = new StringBuffer();
        while ((buffer = reader.readLine()) != null) {
            sb.append(buffer);
        }
        if (sb.toString() == null || sb.toString().equals("")) {
            result = "空的";
        } else {
            result = sb.toString();
        }
        return result;
    }

    /**
     * 通过路径获得流
     * 
     * @param path
     * @return 输入流
     */
    public static InputStream getInputStreamFromUrl(String path) {
        InputStream is = null;
        try {
            URL url = new URL(path);
            is = url.openStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    static HttpClient client;

    /**
     * 发送HttpPost请求
     * 
     * @param url
     * @param params
     * @param isEncrypt
     * @return
     * @throws Exception
     */
    public static String httpPost(String url, ArrayList<NameValuePair> params,
            final boolean isEncrypt) throws Exception {
        String result = null;
        // 获得请求
        HttpPost request = new HttpPost(url);
        if (client == null) {
            client = getHttpClient();
        }

        /* 往头里添加 */
        request.addHeader("CMF_OS", "android");
        request.addHeader("CMF_CLIENTVERSION", Configure.appVersion);// 软件版本号
        request.addHeader("CMF_CLIENTID", "AD_" + Configure.macAddress); // 客户端唯一标识
        request.addHeader("CMF_ENCRYPTTYPE", "AES");
        request.addHeader("CMF_CHECKKEY", checkKey);
        request.addHeader("CMF_SIGNATURE", Configure.apkSignature);

        HttpEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
        request.setEntity(entity);// 设置实体参数
        HttpResponse response = client.execute(request);// 执行请求,并获得响应

        // 判断连接
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // 返回成功
            // 获得返回信息
            result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            LogUtil.i("请求返回 :" + result);
        } else {
            result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            throw new Exception("httpPost request error");
        }
        return result;
    }

    private static HttpClient customerHttpClient;

    private static String checkKey;

    public static synchronized HttpClient getHttpClient() {
        try {
            if (null == customerHttpClient) {
                HttpParams params = new BasicHttpParams();

                // 设置一些基本参数
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
                HttpProtocolParams.setUseExpectContinue(params, true);
                /* 从连接池中取连接的超时时间 */
                ConnManagerParams.setTimeout(params, 10000);
                /* 连接超时 */
                HttpConnectionParams.setConnectionTimeout(params, 0);
                /* 请求超时 */
                HttpConnectionParams.setSoTimeout(params, 0);

                // 设置我们的HttpClient支持HTTP和HTTPS两种模式
                SchemeRegistry schReg = new SchemeRegistry();
                schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

                /* 设置https证书 信任全部 */
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory sslFactory = new MySSLSocketFactory(trustStore);
                /* 设置支持 https请求模式 */
                schReg.register(new Scheme("https", sslFactory, 443));

                // 使用线程安全的连接管理来创建HttpClient
                ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

                customerHttpClient = new DefaultHttpClient(conMgr, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customerHttpClient;
    }

    /**
     * http post请求
     * 
     * @param url 请求地址
     * @param params2 请求参数
     * @return
     * @throws Exception
     */
    public static String POST(String url, HashMap<String, String> params2, boolean isEncrypt)
            throws Exception {
        LogUtil.i("url --->" + url);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        if (params2 != null) {
            Set<String> keys = params2.keySet();
            NameValuePair param = null;
            for (String key : keys) {
                LogUtil.i("params2  --->" + key + "<---------->" + params2.get(key));
                param = new BasicNameValuePair(key, params2.get(key));
                params.add(param);
            }
        }
        return httpPost(url, params, isEncrypt);
    }

    // /**
    // * http post请求
    // *
    // * @param url 请求地址
    // * @param params2 请求参数
    // * @return
    // * @throws Exception
    // */
    // public static String AES_POST(String url, String contentes, boolean
    // isEncrypt) throws Exception {
    // byte[] content = contentes.getBytes();
    // byte[] keyBytes = hdz.base.Function.MD5_GetByteS("AD_" +
    // Configure.macAddress);
    // byte[] params = AESEncodeDecode.AESEncode(content, keyBytes);
    //
    // byte[] checkEnd = Configure.checkEnd.getBytes();
    // byte[] ByteArray = new byte[params.length + checkEnd.length];
    // System.arraycopy(params, 0, ByteArray, 0, params.length);
    // System.arraycopy(checkEnd, 0, ByteArray, params.length, checkEnd.length);
    //
    // byte[] data = hdz.base.Function.MD5_GetByteS(ByteArray);
    // checkKey = StringUtil.byteToStr(data);
    //
    // // LogUtil.i("url -->" + url);
    // // LogUtil.i("未加密的内容 contentes -->" + contentes);
    // // LogUtil.i("加密 的内容 params -->" + StringUtil.byteToStr(params));
    // // byte[] cccc = AESEncodeDecode.AESDecode(params, keyBytes);
    // // LogUtil.i("解密的内容 params -->" + );
    //
    //
    //
    //
    // return httpPost(url, params, isEncrypt);
    // // return "" ;
    // }

    public static String parseParams(HashMap<String, String> params2) {
        StringBuilder sb = new StringBuilder();
        if (params2 != null) {
            Set<String> keys = params2.keySet();
            for (String key : keys) {
                sb.append(key + "=" + params2.get(key) + "&");
            }
        }

        String contentS = sb.toString();
        String sss = contentS;
        if (contentS.length() > 0) {
            sss = contentS.substring(0, contentS.lastIndexOf("&"));
        }

        return sss;
    }

}
