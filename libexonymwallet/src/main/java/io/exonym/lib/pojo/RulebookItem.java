package io.exonym.lib.pojo;

import java.util.ArrayList;

public class RulebookItem {
    String id;
    String description;
    ArrayList<Interpretation> interpretations = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Interpretation> getInterpretations() {
        return interpretations;
    }

    public void setInterpretations(ArrayList<Interpretation> interpretations) {
        this.interpretations = interpretations;
    }
}
