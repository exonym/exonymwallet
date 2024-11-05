package io.exonym.lib.lite;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.logging.Logger;

public class Http implements AutoCloseable {

    private static Logger logger = Logger.getLogger(Http.class.getName());

    private final CloseableHttpClient client;
    private HttpContext context = null;
    private final RequestConfig config;

    public Http() {
        int timeout = 60;
        config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        // this.client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        this.client = HttpClients.createDefault();

    }

    public void newContext(){
        CookieStore cookieStore = new BasicCookieStore();
        this.context = new BasicHttpContext();
        this.context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);


    }

    public String basicPost(String url, String json, Header... headers) throws IOException {
        if (context==null){
            this.newContext();

        }
        HttpPost post = new HttpPost(url);
        post.setConfig(config);
        post.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        post.addHeader("Accept", "application/json");
        post.addHeader("Accept-Language", "en-US,en;q=0.5");
        post.addHeader("Access-Control-Request-Headers", "content-type");
        post.addHeader("Access-Control-Request-Method", "POST");
        post.addHeader("Method", "POST");
        for (Header header : headers){
            post.addHeader(header);
        }

//        post.addHeader("Cache-Control", "no-cache");
//        post.addHeader("Connection", "keep-alive");
//        post.addHeader("Host", "https://node.spectra.plus");
//        post.addHeader("Sec-Fetch-Dest", "empty");
//        post.addHeader("Sec-Fetch-Mode", "cors");
//        post.addHeader("Sec-Fetch-Site", "same-site");
        post.addHeader("content-type", "application/json");
        StringEntity entity = new StringEntity(json);
        post.setEntity(entity);
        ResponseBasic r = new ResponseBasic();
        // logger.debug(context.toString());
        return client.execute(post, r, context);

    }

    public String basicGet(String url, Header... headers) throws IOException{
        if (context==null){
            this.newContext();

        }
        HttpGet get = new HttpGet(url);
        get.setConfig(config);
        get.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        get.addHeader("origin", "http://exonym-node");
        get.addHeader("content-type", "application/json");
        get.addHeader("Accept", "text/plain");
        get.addHeader("Method", "GET");
        for (Header header : headers){
            get.addHeader(header);
        }


        ResponseBasic r = new ResponseBasic();
        return client.execute(get, r, context);

    }


    @Override
    public void close() throws Exception {
        if (client!=null){
            client.close();

        }
    }
}
