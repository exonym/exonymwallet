package io.exonym.lib.lite;

import io.exonym.lib.exceptions.UxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.logging.Logger;

public class ResponseBasic implements ResponseHandler<String> {

    private final static Logger logger = Logger.getLogger(ResponseBasic.class.getName());

    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        HttpEntity entity = response.getEntity();
        int status = response.getStatusLine().getStatusCode();
        if (status>=200 && status <=300){
            return (entity!=null ? EntityUtils.toString(entity) : "Improper Response");

        } else {
            logger.info(EntityUtils.toString(entity));
            throw new ClientProtocolException("Error Code " + status);

        }
    }
}
