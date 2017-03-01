package com.logansoft.UIEngine.utils;

import com.logansoft.UIEngine.utils.http.HttpUtil;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class VolleyImageDownload extends BaseImageDownloader {

    private HttpClient httpClient;
    public VolleyImageDownload(Context context ) {
        super(context);
        httpClient=HttpUtil.getHttpClient();
    }
    @Override
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        HttpGet httpRequest = new HttpGet(imageUri);
//         Map<String, String> requestHeader=RequestManager.getHeader();
//         Set<Entry<String, String>> entry = requestHeader.entrySet();
//         for (Entry<String, String> entry2 : entry) {
//             LogUtil.i(entry2.getKey()+":"+entry2.getValue());
//             httpRequest.addHeader(entry2.getKey(), entry2.getValue());
//        }
        HttpResponse response = httpClient.execute(httpRequest);
//         Header[] header = response.getHeaders("Set-Cookie");
//         for (Header header2 : header) {
//             String cookieName = header2.getName();
//             String cookie = header2.getValue();
//             LogUtil.i(cookieName+":"+cookie);
//           if(cookie.contains("ASP.NET_SessionId")||cookie.contains("JSESSIONID")){
//               String cookie1 = RequestManager.getHeader().get("Cookie");
//               if (!TextUtils.isEmpty(cookie1)&&!cookie1.contains(cookie)) {
//                   cookie1+=";"+cookie;
//               }else {
//                   cookie1 = cookie;
//               }
//               RequestManager.getHeader().put("Cookie",cookie1);
//           }
//        }
        HttpEntity entity = response.getEntity();
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        return bufHttpEntity.getContent();
    }
}
