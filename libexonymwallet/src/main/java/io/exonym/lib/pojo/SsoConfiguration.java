package io.exonym.lib.pojo;

import java.net.URI;
import java.util.HashMap;

public class SsoConfiguration {

    private URI domain;

    private boolean sybil;

    private HashMap<String, RulebookAuth> honestUnder = new HashMap<>();

    public SsoConfiguration() {
    }

    public URI getDomain() {
        return domain;
    }

    public void setSybil(boolean sybil) {
        this.sybil = sybil;
    }

    public boolean isSybil() {
        return sybil;
    }

    public HashMap<String, RulebookAuth> getHonestUnder() {
        return honestUnder;
    }

    public void setDomain(URI domain) {
        this.domain = domain;
    }

    public void setHonestUnder(HashMap<String, RulebookAuth> honestUnder) {
        this.honestUnder = honestUnder;
    }
}
