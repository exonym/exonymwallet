package io.exonym.lib.pojo;

import java.net.URI;
import java.util.ArrayList;

public class RulebookAuth {

    private URI rulebookUID;

    private ArrayList<URI> leadBlacklist = new ArrayList<>();

    private ArrayList<URI> modBlacklist = new ArrayList<>();

    public URI getRulebookUID() {
        return rulebookUID;
    }

    public void setRulebookUID(URI rulebookUID) {
        this.rulebookUID = rulebookUID;
    }

    public ArrayList<URI> getLeadBlacklist() {
        return leadBlacklist;
    }

    public void setLeadBlacklist(ArrayList<URI> leadBlacklist) {
        this.leadBlacklist = leadBlacklist;
    }

    public ArrayList<URI> getModBlacklist() {
        return modBlacklist;
    }

    public void setModBlacklist(ArrayList<URI> modBlacklist) {
        this.modBlacklist = modBlacklist;
    }
}
