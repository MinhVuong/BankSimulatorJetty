/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.notify;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import vng.paygate.domain.bo.BoMiNotifyResponse;
import vng.paygate.domain.common.Constants;

/**
 *
 * @author CPU01661-local
 */
public class NotifyHelper {
    public String postNotify(String url, JsonObject json, String dataType, String contentType) throws UnsupportedEncodingException, IOException{
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            if(StringUtils.isEmpty(contentType)){
                contentType = "application/json";
            }
            if(StringUtils.isEmpty(dataType)){
                dataType = "application/json";
            }
            httpPost.setHeader("Content-type", contentType);
            httpPost.setHeader("Accept-Content Type", dataType);
            httpPost.setEntity(new StringEntity(json.toString()));
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();
                return response.toString();
            }else{
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}
