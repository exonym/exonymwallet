package io.exonym.lib.pojo;


import io.exonym.lib.standard.CryptoUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ExoNotify implements Msg, VioIndexable {

    public static final String TYPE_JOIN = "JOIN";

    public static final String TYPE_LEAD = "LEAD";
    public static final String TYPE_VIOLATION = "VIOLATION";

    public static final String TYPE_MOD = "MOD";

    public static final String TYPE_OVERRIDE = "OVERRIDE";

    private String type;
    private URI nodeUid;
    private String t;
    private String nibble6;
    private String hashOfX0;
    private String timeOfViolation;
    private String sigB64;
    private String ppB64;

    private String raiB64;

    private String ppSigB64;

    private String raiSigB64;

    private ArrayList<Vio> vios;

    public ArrayList<Vio> getVios() {
        if (vios ==null){
            vios = new ArrayList<>();
        }
        return vios;
    }

    public void setVios(ArrayList<Vio> vios) {
        this.vios = vios;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public URI getNodeUid() {
        return nodeUid;
    }

    public void setNodeUid(URI nodeUid) {
        this.nodeUid = nodeUid;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getNibble6() {
        return nibble6;
    }

    public void setNibble6(String nibble6) {
        this.nibble6 = nibble6;
    }

    public String getHashOfX0() {
        return hashOfX0;
    }

    public void setHashOfX0(String hashOfX0) {
        this.hashOfX0 = hashOfX0;
    }

    public String getSigB64() {
        return sigB64;
    }

    public void setSigB64(String sigB64) {
        this.sigB64 = sigB64;
    }

    public String getPpB64() {
        return ppB64;
    }

    public void setPpB64(String ppB64) {
        this.ppB64 = ppB64;
    }

    public String getRaiB64() {
        return raiB64;
    }

    public void setRaiB64(String raiB64) {
        this.raiB64 = raiB64;
    }

    public String getPpSigB64() {
        return ppSigB64;
    }

    public void setPpSigB64(String ppSigB64) {
        this.ppSigB64 = ppSigB64;
    }

    public String getRaiSigB64() {
        return raiSigB64;
    }

    public void setRaiSigB64(String raiSigB64) {
        this.raiSigB64 = raiSigB64;
    }

    public String getTimeOfViolation() {
        return timeOfViolation;
    }

    @Override
    public URI getModOfVioUid() {
        if (!vios.isEmpty()){
            return vios.get(0).getModOfVioUid();
        }
        return null;
    }


    public void setTimeOfViolation(String timeOfViolation) {
        this.timeOfViolation = timeOfViolation;
    }

    public static byte[] signatureOn(ExoNotify notify){
        StringBuilder builder = new StringBuilder();
        ArrayList<Vio> vios = notify.getVios();
        for (Vio vio : vios){
            if (!vio.getRuleUids().isEmpty()){
                builder.append(vio.getRuleUids().get(0));
                builder.append(vio.getTimeOfViolation());
            }
        }
        builder.append(notify.getTimeOfViolation());
        builder.append(notify.getTimeOfViolation());
        builder.append(notify.getNibble6());
        builder.append(notify.getType());
        builder.append(notify.getHashOfX0());
        builder.append(notify.getNodeUid());
        return CryptoUtils.computeSha256HashAsBytes(
                builder.toString().getBytes(StandardCharsets.UTF_8));

    }

    public String getT() {
        return t;
    }

    @Override
    public String toString() {
        return this.type + " n6=" + nibble6 +
                " x0' || mod=" +
                (this.hashOfX0==null ? this.getNodeUid() : this.hashOfX0);
    }

}
