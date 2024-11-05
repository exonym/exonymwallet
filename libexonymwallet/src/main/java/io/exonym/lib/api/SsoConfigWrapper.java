package io.exonym.lib.api;

import io.exonym.lib.exceptions.UxException;
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
    public SsoConfigWrapper() {
        this.config = new SsoConfiguration();

    }

    public SsoConfigWrapper(SsoConfiguration config) {
        this.config = config;
    }

    public void addLeadToBlacklist(URI leadUid) throws UxException {
        String rulebookId = UIDHelper.computeRulebookHashUid(leadUid);
        RulebookAuth rulebook = this.config.getHonestUnder().get(rulebookId);
        if (rulebook==null){
            rulebook = new RulebookAuth();
            rulebook.setRulebookUID(URI.create(rulebookId));
            this.config.getHonestUnder().put(rulebookId, rulebook);

        }
        rulebook.getLeadBlacklist().add(leadUid);

    }

    public void addModeratorToBlacklist(URI modUid) throws UxException {
        String rulebookId = UIDHelper.computeRulebookIdFromAdvocateUid(modUid);
        RulebookAuth rulebook = this.config.getHonestUnder().get(rulebookId);
        if (rulebook==null){
            rulebook = new RulebookAuth();
            rulebook.setRulebookUID(URI.create(rulebookId));
            this.config.getHonestUnder().put(rulebookId.toString(), rulebook);

        }
        rulebook.getModBlacklist().add(modUid);
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
