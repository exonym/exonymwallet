package io.exonym.lib.pojo;

public class NetworkMapItemModerator extends NetworkMapItem {

    private boolean hasCredentialFrom = false;
    private String revocationInformationHash;

    public boolean isHasCredentialFrom() {
        return hasCredentialFrom;
    }

    public void setHasCredentialFrom(boolean hasCredentialFrom) {
        this.hasCredentialFrom = hasCredentialFrom;
    }

    public String getRevocationInformationHash() {
        return revocationInformationHash;
    }

    public void setRevocationInformationHash(String revocationInformationHash) {
        this.revocationInformationHash = revocationInformationHash;
    }
}
