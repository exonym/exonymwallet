package io.exonym.lib.api;

import com.google.gson.JsonObject;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.lite.Http;
import io.exonym.lib.standard.WhiteList;

public class AddToTrustworthySourcesList {

    public static String addToSourcesTest(String url) throws Exception {
        Http client = new Http();

        try {
            if (WhiteList.isSourceUrl(url)){
                JsonObject o = new JsonObject();
                o.addProperty("test", true);
                o.addProperty("sourceUrl", url);
                String r = client.basicPost("https://node.t0.sybil.exonym.io/registerSource", o.toString());
                client.close();
                return r;

            } else {
                client.close();
                throw new UxException(ErrorMessages.URL_INVALID + " ", url);

            }
        } catch (Exception e) {
            client.close();
            throw e;

        }
    }

    public static void main(String[] args) throws Exception {
        // addToSourcesTest("");
        Http http = new Http();
        JsonObject o = new JsonObject();
        o.addProperty("test", true);
        o.addProperty("sourceUrl", "https://example.com/x-source");
        System.out.println(o.toString());

        System.out.println(http.basicPost("https://node.t0.sybil.exonym.io/registerSource", o.toString()));

    }
}
