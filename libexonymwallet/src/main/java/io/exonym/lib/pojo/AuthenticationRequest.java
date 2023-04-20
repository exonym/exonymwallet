package io.exonym.lib.pojo;

import java.util.ArrayList;

public class AuthenticationRequest {

    private String pseudonym;

    private boolean exposeSybilClass = false;

    private boolean sybilRequired = false;

    private ArrayList<RulebookRequest> rulebookRequests = new ArrayList<>();

    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }

    public boolean isExposeSybilClass() {
        return exposeSybilClass;
    }

    public void setExposeSybilClass(boolean exposeSybilClass) {
        this.exposeSybilClass = exposeSybilClass;
    }

    public boolean isSybilRequired() {
        return sybilRequired;
    }

    public void setSybilRequired(boolean sybilRequired) {
        this.sybilRequired = sybilRequired;
    }

    public ArrayList<RulebookRequest> getRulebookRequests() {
        return rulebookRequests;
    }

    public void setRulebookRequests(ArrayList<RulebookRequest> rulebookRequests) {
        this.rulebookRequests = rulebookRequests;
    }
}
