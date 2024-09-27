package io.exonym.lib.pojo;


import io.exonym.lib.helpers.AbstractCouchDbObject;

import java.net.URI;
import java.net.URL;

public class NetworkMapItem extends AbstractCouchDbObject {

    public static final String TYPE = "rulebook";
    public static final String FIELD_SOURCE_UID = "sourceUID";
    public static final String FIELD_NODE_UID = "nodeUID";
    public static final String FIELD_PUBLIC_KEY = "publicKeyB64";
    public static final String FIELD_RULEBOOK_NODE_URL = "rulebookNodeURL";
    public static final String FIELD_BROADCAST_URL = "broadcastAddress";
    public static final String FIELD_STATIC_URL = "staticURL0";

    private String lastUpdated;
    private String region;
    private String leadName;
    private String moderatorName;
    private URI leadUID;
    private URI nodeUID;
    private URI rulebookNodeURL;
    private URI broadcastAddress;
    private URI staticURL0;
    private byte[] publicKeyB64;
    private URI lastIssuerUID;

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public URI getLeadUID() {
        return leadUID;
    }

    public void setLeadUID(URI leadUID) {
        this.leadUID = leadUID;
    }

    public URI getNodeUID() {
        return nodeUID;
    }

    public void setNodeUID(URI nodeUID) {
        this.nodeUID = nodeUID;
    }


    public URI getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(URI broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }


    public byte[] getPublicKeyB64() {
        return publicKeyB64;
    }

    public void setPublicKeyB64(byte[] publicKeyB64) {
        this.publicKeyB64 = publicKeyB64;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public URI getLastIssuerUID() {
        return lastIssuerUID;
    }

    public void setLastIssuerUID(URI lastIssuerUID) {
        this.lastIssuerUID = lastIssuerUID;
    }

    public String getLeadName() {
        return leadName;
    }

    public void setLeadName(String leadName) {
        this.leadName = leadName;
    }

    public String getModeratorName() {
        return moderatorName;
    }

    public void setModeratorName(String moderatorName) {
        this.moderatorName = moderatorName;
    }

    public URI getRulebookNodeURL() {
        return rulebookNodeURL;
    }

    public void setRulebookNodeURL(URI rulebookNodeURL) {
        this.rulebookNodeURL = rulebookNodeURL;
    }

    public URI getStaticURL0() {
        return staticURL0;
    }

    public void setStaticURL0(URI staticURL0) {
        this.staticURL0 = staticURL0;
    }
}
