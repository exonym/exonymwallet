package io.exonym.lib.pojo;

import org.apache.commons.codec.binary.Base64;
import io.exonym.lib.standard.CryptoUtils;

public class SsoChallenge extends SsoConfiguration implements ExonymChallenge {

    private String challenge;

    private String token;

    private String index;


    public static SsoChallenge newChallenge(SsoConfiguration config){
        SsoChallenge c = new SsoChallenge();
        c.setSybil(config.isSybil());
        c.challenge = Base64.encodeBase64String(
                CryptoUtils.generateNonce(32));
        c.setDomain(config.getDomain());
        c.setHonestUnder(config.getHonestUnder());
        return c;

    }


    public String getChallenge() {
        return challenge;
    }

    @Override
    public String universalLinkPrefix() {
        return Namespace.UNIVERSAL_LINK_AUTHENTICATION_REQUEST;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
