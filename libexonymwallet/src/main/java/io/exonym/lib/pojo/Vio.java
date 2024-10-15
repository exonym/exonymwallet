package io.exonym.lib.pojo;


import io.exonym.lib.standard.CryptoUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Vio  implements VioIndexable {

    public static final String FIELD_X0_HASH = "x0Hash";

    private URI modUid;

    private URI modUidOfRequestor;
    private String nibble6;
    private String x0Hash;
    private String t;

    private boolean reissued = false;

    private boolean override = false;

    private ArrayList<URI> ruleUids = new ArrayList<>();

    private String descriptionOfEvidence;

    @Override
    public URI getModOfVioUid() {
        return modUid;
    }

    private HashMap<String, Integer> historic = new HashMap<>();

    public void setModUid(URI modUid) {
        this.modUid = modUid;
    }

    public URI getModUid() {
        return modUid;
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

    public String getTimeOfViolation() {
        return t;
    }


    public void setT(String t) {
        this.t = t;
    }

    public ArrayList<URI> getRuleUids() {
        return ruleUids;
    }

    public void setRuleUids(ArrayList<URI> ruleUids) {
        this.ruleUids = ruleUids;
    }

    public String getDescriptionOfEvidence() {
        return descriptionOfEvidence;
    }

    public void setDescriptionOfEvidence(String descriptionOfEvidence) {
        this.descriptionOfEvidence = descriptionOfEvidence;
    }

    public HashMap<String, Integer> getHistoric() {
        return historic;
    }

    public boolean isReissued() {
        return reissued;
    }

    public void setReissued(boolean reissued) {
        this.reissued = reissued;
    }

    public void setHistoric(HashMap<String, Integer> historic) {
        this.historic = historic;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public URI getModUidOfRequestor() {
        return modUidOfRequestor;
    }

    public void setModUidOfRequestor(URI modUidOfRequestor) {
        this.modUidOfRequestor = modUidOfRequestor;
    }

    public String getT() {
        return t;
    }

    public static String index(VioIndexable indexable){
        String indexRaw = indexable.getNibble6() +
                indexable.getTimeOfViolation() +
                indexable.getModOfVioUid();
        return CryptoUtils.computeMd5HashAsHex(indexRaw.getBytes(StandardCharsets.UTF_8));

    }
}
