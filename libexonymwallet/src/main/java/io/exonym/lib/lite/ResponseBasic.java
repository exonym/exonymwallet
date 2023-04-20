package io.exonym.lib.lite;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class ResponseBasic implements ResponseHandler<String> {


    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        HttpEntity entity = response.getEntity();
        int status = response.getStatusLine().getStatusCode();
        if (status>=200 && status <=300){
            return (entity!=null ? EntityUtils.toString(entity) : "Improper Response");

        } else {
            throw new ClientProtocolException("Error Code " + status);

        }
    }
}
