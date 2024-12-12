package io.exonym.lib.api;

import com.google.gson.JsonObject;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.lite.Http;
import io.exonym.lib.standard.WhiteList;

public class AddToTrustworthyLeadsList {

    public static String addToLeadsTest(String url) throws Exception {
        Http client = new Http();

        try {
            if (WhiteList.isLeadUrl(url)){
                JsonObject o = new JsonObject();
                o.addProperty("test", true);
                o.addProperty("sourceUrl", url);
                String r = client.basicPost("https://node.t0.sybil.exonym.io/registerSource", o.toString());
                return r;

            } else {
                throw new UxException(ErrorMessages.URL_INVALID + " ", url);

            }
        } catch (Exception e) {
            throw e;

        }
    }

}
