package io.exonym.lib.pojo;


public class RulebookDescription {

    private String name;

    private String simpleDescriptionEN;

    boolean production = false;

    private Penalty defaultPenalty;

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

    public Penalty getDefaultPenalty() {
        return defaultPenalty;
    }

    public void setDefaultPenalty(Penalty defaultPenalty) {
        this.defaultPenalty = defaultPenalty;
    }
}