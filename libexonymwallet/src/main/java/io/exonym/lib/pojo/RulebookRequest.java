package io.exonym.lib.pojo;

import java.util.ArrayList;

public class RulebookRequest {

    private String rulebookId;

    private ArrayList<String> blacklistSources = new ArrayList<>();

    private ArrayList<String> blacklistAdvocates = new ArrayList<>();

    public String getRulebookId() {
        return rulebookId;
    }

    public void setRulebookId(String rulebookId) {
        this.rulebookId = rulebookId;
    }

    public ArrayList<String> getBlacklistSources() {
        return blacklistSources;
    }

    public void setBlacklistSources(ArrayList<String> blacklistSources) {
        this.blacklistSources = blacklistSources;
    }

    public ArrayList<String> getBlacklistAdvocates() {
        return blacklistAdvocates;
    }

    public void setBlacklistAdvocates(ArrayList<String> blacklistAdvocates) {
        this.blacklistAdvocates = blacklistAdvocates;
    }
}
