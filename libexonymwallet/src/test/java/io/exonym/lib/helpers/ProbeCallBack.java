package io.exonym.lib.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.exonym.lib.lite.Http;
import io.exonym.lib.lite.ModelSingleSequence;
import io.exonym.lib.wallet.TestTools;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class ProbeCallBack extends ModelSingleSequence {

    private final static Logger logger = Logger.getLogger(ProbeCallBack.class.getName());

    private Http httpClient;
    private URL endPoint;

    private String result = null;
    private JsonObject jsonObject;


    public ProbeCallBack(Http httpClient, URL endPoint) throws Exception {
        super("Probe");
        this.httpClient = httpClient;
        this.endPoint=endPoint;
        this.start();

    }

    @Override
    protected void process() {
        try {
            String r = httpClient.basicPost(endPoint.toString(), "{\"probe");
            logger.info("Raw Response=" + r);

            jsonObject = JsonParser.parseString(r).getAsJsonObject();

            if (jsonObject.has("endonym")){
                this.result = jsonObject.get("endonym").getAsString();

            } else {
                this.result = r;

            }
            synchronized (this){
                this.notifyAll();
            }
        } catch (IOException e) {
            TestTools.handleError(e);

        }
    }

    public synchronized String getResult() {
        if (result ==null){
            synchronized (this){
                try {
                    this.wait();

                } catch (InterruptedException e) {
                    TestTools.handleError(e);

                }
            }
        }
        return result;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }
}
