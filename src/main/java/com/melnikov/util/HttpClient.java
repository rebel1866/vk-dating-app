package com.melnikov.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClient {
    public static String sendPOST(String url, Map<String, String> params) throws IOException {
        String result;
        HttpPost post = new HttpPost(url);
        List<NameValuePair> urlParameters = new ArrayList<>();
        params.forEach((key, value) -> urlParameters.add(new BasicNameValuePair(key, value)));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            result = EntityUtils.toString(response.getEntity());
        }
        return result;
    }
}
