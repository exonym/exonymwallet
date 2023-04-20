package io.exonym.lib.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

public class SerialErrorHandling {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ArrayList<String> info;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ArrayList<String> getInfo() {
        return info;
    }

    public void setInfo(ArrayList<String> info) {
        this.info = info;
    }
}
