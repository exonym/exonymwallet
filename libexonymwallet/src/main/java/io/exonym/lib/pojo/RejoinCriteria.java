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

    private String tovutc;

    private String nibble6;
    private String x0Hash;
    private URI hostMod;



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

    public String getTovutc() {
        return tovutc;
    }

    public void setTovutc(String tovutc) {
        this.tovutc = tovutc;
    }

    public String getNibble6() {
        return nibble6;
    }

    public void setNibble6(String nibble6) {
        this.nibble6 = nibble6;
    }

    public String getX0Hash() {
        return x0Hash;
    }

    public void setX0Hash(String x0Hash) {
        this.x0Hash = x0Hash;
    }

    public URI getHostMod() {
        return hostMod;
    }

    public void setHostMod(URI hostMod) {
        this.hostMod = hostMod;
    }
}
