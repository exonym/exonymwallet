package io.exonym.lib.pojo;

import java.net.URI;
import java.util.ArrayList;

public class RulebookAuth {

    private URI rulebookUID;

    private ArrayList<URI> sourceBlacklist = new ArrayList<>();

    private ArrayList<URI> advocateBlacklist = new ArrayList<>();

    public URI getRulebookUID() {
        return rulebookUID;
    }

    public void setRulebookUID(URI rulebookUID) {
        this.rulebookUID = rulebookUID;
    }

    public ArrayList<URI> getSourceBlacklist() {
        return sourceBlacklist;
    }

    public void setSourceBlacklist(ArrayList<URI> sourceBlacklist) {
        this.sourceBlacklist = sourceBlacklist;
    }

    public ArrayList<URI> getAdvocateBlacklist() {
        return advocateBlacklist;
    }

    public void setAdvocateBlacklist(ArrayList<URI> advocateBlacklist) {
        this.advocateBlacklist = advocateBlacklist;
    }
}
