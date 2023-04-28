package io.exonym.lib.lite;

import com.google.gson.JsonObject;

import java.util.HashSet;

public class NonInteractiveProofRequest {

    private HashSet<String> issuerUids = new HashSet<>();
    private HashSet<String> pseudonyms = new HashSet<>();
    private JsonObject metadata;

    public HashSet<String> getIssuerUids() {
        return issuerUids;
    }

    public void setIssuerUids(HashSet<String> issuerUids) {
        this.issuerUids = issuerUids;
    }

    public HashSet<String> getPseudonyms() {
        return pseudonyms;
    }

    public void setPseudonyms(HashSet<String> pseudonyms) {
        this.pseudonyms = pseudonyms;
    }


    public JsonObject getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonObject metadata) {
        this.metadata = metadata;
    }
}
