package io.exonym.lib.pojo;

import java.net.URI;
import java.util.ArrayList;

public class NetworkMapItemLead extends NetworkMapItem {

    private boolean defaultAllow = true;

    private ArrayList<URI> moderatorsForLead = new ArrayList<>();

    public boolean isDefaultAllow() {
        return defaultAllow;
    }

    public void setDefaultAllow(boolean defaultAllow) {
        this.defaultAllow = defaultAllow;
    }


    public ArrayList<URI> getModeratorsForLead() {
        return moderatorsForLead;
    }

    public void setModeratorsForLead(ArrayList<URI> moderatorsForLead) {
        this.moderatorsForLead = moderatorsForLead;
    }
}
