package io.exonym.lib.pojo;

import java.math.BigInteger;
import java.net.URI;

public class IssuanceSigma extends SerialErrorHandling  {

    private String hello;
    private String im;
    private String imab;

    private String sybilClass;
    private BigInteger h;
    private URI issuerUid;
    private boolean testNet = false;

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }

    public String getIm() {
        return im;
    }

    public void setIm(String im) {
        this.im = im;
    }

    public String getImab() {
        return imab;
    }

    public void setImab(String imab) {
        this.imab = imab;
    }

    public boolean isTestNet() {
        return testNet;
    }

    public void setTestNet(boolean testNet) {
        this.testNet = testNet;
    }

    public BigInteger getH() {
        return h;
    }

    public void setH(BigInteger h) {
        this.h = h;
    }

    public URI getIssuerUid() {
        return issuerUid;
    }

    public void setIssuerUid(URI issuerUid) {
        this.issuerUid = issuerUid;
    }

    public String getSybilClass() {
        return sybilClass;
    }

    public void setSybilClass(String sybilClass) {
        this.sybilClass = sybilClass;
    }
}
