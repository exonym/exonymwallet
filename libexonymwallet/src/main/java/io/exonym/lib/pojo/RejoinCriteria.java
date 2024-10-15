package io.exonym.lib.pojo;

import java.net.URI;
import java.util.ArrayList;

public class RejoinCriteria {

    private boolean canRejoin = false;

    private String imabFinalB64;
    private ArrayList<URI> revokedModerators = new ArrayList<>();
    private String penaltyType;
    private String bannedLiftedUTC;
    private String credentialFrom;

    private String error;

    public boolean isCanRejoin() {
        return canRejoin;
    }

    public void setCanRejoin(boolean canRejoin) {
        this.canRejoin = canRejoin;
    }

    public ArrayList<URI> getRevokedModerators() {
        return revokedModerators;
    }

    public void setRevokedModerators(ArrayList<URI> revokedModerators) {
        this.revokedModerators = revokedModerators;
    }

    public String getPenaltyType() {
        return penaltyType;
    }

    public void setPenaltyType(String penaltyType) {
        this.penaltyType = penaltyType;
    }

    public String getBannedLiftedUTC() {
        return bannedLiftedUTC;
    }

    public void setBannedLiftedUTC(String bannedLiftedUTC) {
        this.bannedLiftedUTC = bannedLiftedUTC;
    }

    public String getCredentialFrom() {
        return credentialFrom;
    }

    public void setCredentialFrom(String credentialFrom) {
        this.credentialFrom = credentialFrom;
    }

    public String getImabFinalB64() {
        return imabFinalB64;
    }

    public void setImabFinalB64(String imabFinalB64) {
        this.imabFinalB64 = imabFinalB64;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
