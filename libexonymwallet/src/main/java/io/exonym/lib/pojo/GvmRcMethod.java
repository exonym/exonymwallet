package io.exonym.lib.pojo;

import java.util.ArrayList;

public class GvmRcMethod {

    String name;
    ArrayList<String> parameterTypes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(ArrayList<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
