package com.foo.monitor.jmx.monitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;



/**
 * Created by Foo on 11/20/15.
 */
public class OpenFalcon extends AbstractMonitor{
    private static final Logger LOG = LoggerFactory.getLogger(OpenFalcon.class);
    //    push data to local open-falcon agent
    private final String apiAddr = "http://127.0.0.1:1988/v1/push";
    private CloseableHttpClient client;
    private Gson gson;


    public OpenFalcon() {
        client = HttpClients.createDefault();
        gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    @Override
    public void monitor(Object data) {
        String jsonData = gson.toJson((OpenFalconData)data);
        LOG.debug("OpenFalcon push data " + jsonData);
        try {
            HttpPost httpPost = new HttpPost(this.apiAddr);
            StringEntity params = new StringEntity(jsonData);
            httpPost.addHeader("content-type", "application/json");
            httpPost.setEntity(params);

            CloseableHttpResponse res = client.execute(httpPost);
            LOG.debug("Response from openfalcon: " + res);
            try {
                HttpEntity enity = res.getEntity();
                if (enity != null) {
                    InputStream stream = enity.getContent();
                    try {
                        stream.read();
                    } catch (IOException ex) {
                        throw ex;
                    } finally {
                        stream.close();
                        EntityUtils.consume(enity);
                    }
                }

            } finally {
                res.close();
            }
        } catch (Exception e) {

        } finally {
//            client.close();
        }
    }
}
