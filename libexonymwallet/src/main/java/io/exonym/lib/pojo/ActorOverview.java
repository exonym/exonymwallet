package io.exonym.lib.pojo;

import java.util.ArrayList;

public class ActorOverview {

    private NetworkMapItem actor;
    private Rulebook rulebook;

    private ArrayList<String> leadsForRulebook = new ArrayList<>();

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

    public ArrayList<String> getLeadsForRulebook() {
        return leadsForRulebook;
    }

    public void setLeadsForRulebook(ArrayList<String> sourcesForRulebook) {
        this.leadsForRulebook = sourcesForRulebook;
    }
}
