package io.exonym.lib.pojo;

import io.exonym.lib.wallet.WalletUtils;
import org.apache.commons.codec.binary.Base64;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.QrCode;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class SsoChallenge extends SsoConfiguration implements ExonymChallenge {

    private final static Logger logger = Logger.getLogger(SsoChallenge.class.getName());
    private String challenge;

    public static SsoChallenge newChallenge(SsoConfiguration config){
        SsoChallenge c = new SsoChallenge();
        c.setSybil(config.isSybil());
        c.challenge = Base64.encodeBase64String(CryptoUtils.generateNonce(32));
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
}
