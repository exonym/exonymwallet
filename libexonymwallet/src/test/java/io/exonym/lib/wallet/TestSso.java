package io.exonym.lib.wallet;

import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.SsoConfigWrapper;
import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.lite.FulfillmentReport;
import io.exonym.lib.pojo.AuthenticationWrapper;
import io.exonym.lib.pojo.DelegateRequest;
import io.exonym.lib.pojo.SsoChallenge;
import io.exonym.lib.standard.PassStore;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class TestSso {

    private final static URI RULEBOOK_TARGET = URI.create("urn:rulebook:69bb840695e4fd79a00577de5f0071b311bbd8600430f6d0da8f865c5c459d44");
    private final static URI ADVOCATE_TARGET = URI.create("urn:rulebook:exosources:raised:69bb840695e4fd79a00577de5f0071b311bbd8600430f6d0da8f865c5c459d44");
    private final static URI ADVOCATE_TO_BLACKLIST = URI.create("urn:rulebook:exosources:baseline:69bb840695e4fd79a00577de5f0071b311bbd8600430f6d0da8f865c5c459d44");
    private final static URI TARGET_SSO_URL = URI.create("http://localhost:20001");


    @BeforeClass
    public static void beforeClass() throws Exception {
        PassStore p = pass();
        Path path = path();
        Path containers = path.resolve("containers");
        Path container = containers.resolve(p.getUsername());
        if (!Files.exists(container)){
            XContainerJSON x = new XContainerJSON(containers, p.getUsername(), true);
            ExonymOwner owner = new ExonymOwner(x);
            owner.openContainer(p);
            owner.setupContainerSecret(p.getEncrypt(), p.getDecipher());
            SybilOnboarding.testNet(p, path, "person");
            RulebookOnboarding.onboardRulebook(p, path, ADVOCATE_TARGET);

        }
    }

    private static Logger logger = Logger.getLogger(TestSso.class.getName());

    @Test
    public void delegate() {
        try {
            // Service gets a request to delegate
            logger.finer("Hello");
            DelegateRequest delegateRequest = DelegateRequest.newDelegateRequest(URI.create("http://localhost:20001"));
            AuthenticationWrapper dr = AuthenticationWrapper.wrapToWrapper(delegateRequest, 100, DelegateRequest.class);

            // Service User generates a request for third-party
            Prove prove = new Prove(pass(), path());
            String drForThirdParty = prove.generateDelegationRequestForThirdParty(dr.getLink(), "Bob's billing");
            System.out.println(drForThirdParty);

            // Third-party user generates a proof token for Service Owner

            HashMap<String, String> request = JaxbHelper.jsonToClass(drForThirdParty, HashMap.class);
            String proofLink = prove.fillDelegationRequest(request.get("link"));
            System.out.println(proofLink);
            HashMap<String, String> request0 = JaxbHelper.jsonToClass(proofLink, HashMap.class);
            String endonym = prove.verifyDelegationRequest(request.get("link"), request0.get("link"));
            System.out.println(endonym);
            logger.info(endonym);

        } catch (Exception e) {
            logger.throwing("TestSso.class", "delegate()", e);
            throw new RuntimeException(e);

        }
    }

    @Test
    public void walletSummary() {
        try {
            assert FileType.isCredential("exosources.baseline.69bb840695e4fd79a00577de5f0071b311bbd8600430f6d0da8f865c5c459d44.602dfbbb.ic.xml");
            PassStore store = pass();
            Prove prove = new Prove(store, Path.of("resource"));
            System.out.println(prove.walletReport());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void ssoAuthenticate() {
        try {

            SsoConfigWrapper config = new SsoConfigWrapper(TARGET_SSO_URL);
            config.requireRulebook(RULEBOOK_TARGET);
            config.addAdvocateToBlacklist(ADVOCATE_TO_BLACKLIST);
            SsoChallenge c = SsoChallenge.newChallenge(config.getConfig());
            String s = AuthenticationWrapper.wrap(c, 100, SsoChallenge.class);
            System.out.println(s);

            HashMap<String, String> request0 = JaxbHelper.jsonToClass(s, HashMap.class);

            // To app
            PassStore store = pass();
            Prove prove = new Prove(store, path());
            System.out.println(prove.proofForRulebookSSO(request0.get("link")));


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void ssoWithLinkAuth() {
        try {
            String link = "https://trust.exonym.io/auth/?eJwljEELgjAYQO/+irFzspmG4DFjSUE0SLp00W04YX6LOUMR/3ta1/d4bw4QwkJXxihoFM4Q9owckoSzG7P3RAhXFCJ99un54yBqrvWDn0qXH6uLjPhroDSWeLc9pO2qFraB9v6dEaJGC1MXjiGNsz2lNCJgQ2EsqP5f9FPdmjXwblA/oDfnS5DKrXheguULtjYvwg==";
            PassStore store = pass();
            Prove prove = new Prove(store, path());
            FulfillmentReport report = prove.authenticationSummaryForULink(link);
            System.out.println(JaxbHelper.gson.toJson(report));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static PassStore pass() throws Exception {
        PassStore store = new PassStore(TestSybilOnboarding.password, false);
        store.setUsername(TestSybilOnboarding.username);
        return store;

    }

    private static Path path() {
        return Path.of("test-data");
    }

}
