package io.exonym.lib.lite;

import java.util.HashSet;

public class NonInteractiveProofRequest {

    private HashSet<String> issuerUids = new HashSet<>();
    private HashSet<String> pseudonyms = new HashSet<>();
    private String metadata;

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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
