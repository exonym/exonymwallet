package io.exonym.lib.pojo;


import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.standard.CryptoUtils;

import java.net.URI;
import java.util.ArrayList;

public class RevocationRequestWrapper {

    private ArrayList<RevocationRequest> requests = new ArrayList<>();

    private String key, kid;

    private byte[] signature;

    private URI moderator;


    public ArrayList<RevocationRequest> getRequests() {
        return requests;
    }

    public void setRequests(ArrayList<RevocationRequest> requests) {
        this.requests = requests;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public URI getModerator() {
        return moderator;
    }

    public void setModerator(URI moderator) {
        this.moderator = moderator;
    }

    public static String signatureOn(ArrayList<RevocationRequest> requests){
        return CryptoUtils.computeSha256HashAsHex(JaxbHelper.gson.toJson(requests));

    }
}
