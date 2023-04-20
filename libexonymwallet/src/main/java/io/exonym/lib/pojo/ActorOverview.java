package io.exonym.lib.pojo;

import java.util.ArrayList;

public class ActorOverview {

    private NetworkMapItem actor;
    private Rulebook rulebook;

    private ArrayList<String> sourcesForRulebook = new ArrayList<>();

    public NetworkMapItem getActor() {
        return actor;
    }

    public void setActor(NetworkMapItem actor) {
        this.actor = actor;
    }

    public Rulebook getRulebook() {
        return rulebook;
    }

    public void setRulebook(Rulebook rulebook) {
        this.rulebook = rulebook;
    }

    public ArrayList<String> getSourcesForRulebook() {
        return sourcesForRulebook;
    }

    public void setSourcesForRulebook(ArrayList<String> sourcesForRulebook) {
        this.sourcesForRulebook = sourcesForRulebook;
    }
}
