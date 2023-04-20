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
    private String sourceName;
    private String advocateName;
    private URI sourceUID;
    private URI nodeUID;
    private URL rulebookNodeURL;
    private URI broadcastAddress;
    private URL staticURL0;
    private URL staticURL1;
    private byte[] publicKeyB64;
    private URI lastIssuerUID;

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public URI getSourceUID() {
        return sourceUID;
    }

    public void setSourceUID(URI sourceUID) {
        this.sourceUID = sourceUID;
    }

    public URI getNodeUID() {
        return nodeUID;
    }

    public void setNodeUID(URI nodeUID) {
        this.nodeUID = nodeUID;
    }

    public URL getRulebookNodeURL() {
        return rulebookNodeURL;
    }

    public void setRulebookNodeURL(URL rulebookNodeURL) {
        this.rulebookNodeURL = rulebookNodeURL;
    }

    public URI getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(URI broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

    public URL getStaticURL0() {
        return staticURL0;
    }

    public void setStaticURL0(URL staticURL0) {
        this.staticURL0 = staticURL0;
    }

    public URL getStaticURL1() {
        return staticURL1;
    }

    public void setStaticURL1(URL staticURL1) {
        this.staticURL1 = staticURL1;
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getAdvocateName() {
        return advocateName;
    }

    public void setAdvocateName(String advocateName) {
        this.advocateName = advocateName;
    }
}
