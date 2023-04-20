package io.exonym.lib.pojo;

import java.net.URI;
import java.util.ArrayList;

public class NetworkMapItemSource extends NetworkMapItem {

    private boolean defaultAllow = true;

    private ArrayList<URI> advocatesForSource = new ArrayList<>();

    public boolean isDefaultAllow() {
        return defaultAllow;
    }

    public void setDefaultAllow(boolean defaultAllow) {
        this.defaultAllow = defaultAllow;
    }


    public ArrayList<URI> getAdvocatesForSource() {
        return advocatesForSource;
    }

    public void setAdvocatesForSource(ArrayList<URI> advocatesForSource) {
        this.advocatesForSource = advocatesForSource;
    }
}
