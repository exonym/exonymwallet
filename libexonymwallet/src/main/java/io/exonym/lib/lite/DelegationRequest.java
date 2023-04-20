package io.exonym.lib.lite;

public class DelegationRequest {
    private String name;
    private String service;
    private String link;
    private String qrPngB64;
    private String requestDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getQrPngB64() {
        return qrPngB64;
    }

    public void setQrPngB64(String qrPngB64) {
        this.qrPngB64 = qrPngB64;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }
}
