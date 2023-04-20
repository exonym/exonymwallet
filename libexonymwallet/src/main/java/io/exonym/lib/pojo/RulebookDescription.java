package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RulebookDescription {

    private String name;

    private String simpleDescriptionEN;

    boolean production = false;

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSimpleDescriptionEN() {
        return simpleDescriptionEN;
    }

    public void setSimpleDescriptionEN(String simpleDescriptionEN) {
        this.simpleDescriptionEN = simpleDescriptionEN;
    }

}