package io.exonym.lib.api;

import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.RulebookAuth;
import io.exonym.lib.pojo.SsoConfiguration;

import java.net.URI;

public class SsoConfigWrapper {

    private final SsoConfiguration config;

    public SsoConfigWrapper(URI domain) {
        this.config = new SsoConfiguration();
        this.config.setDomain(domain);

    }

    public SsoConfigWrapper(SsoConfiguration config) {
        this.config = config;
    }

    public void addSourceToBlacklist(URI sourceUID) {
        String rulebookId = UIDHelper.computeRulebookIdFromSourceUid(sourceUID);
        RulebookAuth rulebook = this.config.getHonestUnder().get(rulebookId);
        if (rulebook==null){
            rulebook = new RulebookAuth();
            rulebook.setRulebookUID(URI.create(rulebookId));
            this.config.getHonestUnder().put(rulebookId, rulebook);

        }
        rulebook.getSourceBlacklist().add(sourceUID);

    }

    public void addAdvocateToBlacklist(URI advocateUID) {
        String  rulebookId = UIDHelper.computeRulebookIdFromAdvocateUid(advocateUID);
        RulebookAuth rulebook = this.config.getHonestUnder().get(rulebookId);
        if (rulebook==null){
            rulebook = new RulebookAuth();
            rulebook.setRulebookUID(URI.create(rulebookId));
            this.config.getHonestUnder().put(rulebookId, rulebook);

        }
        rulebook.getAdvocateBlacklist().add(advocateUID);
    }

    public void requireSybil(boolean required){
        this.config.setSybil(required);

    }

    public SsoConfiguration getConfig() {
        return config;
    }

    public void requireRulebook(URI uri) {
        this.config.setSybil(true);
        RulebookAuth auth = this.config.getHonestUnder().get(uri.toString());
        if (auth==null){
            auth = new RulebookAuth();
            this.config.getHonestUnder().put(uri.toString(), auth);
            auth.setRulebookUID(uri);
        }
    }
}
