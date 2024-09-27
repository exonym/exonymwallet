package io.exonym.lib.wallet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.SsoConfigWrapper;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.lite.FulfillmentReport;
import io.exonym.lib.lite.NonInteractiveProofRequest;
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
import java.util.logging.Logger;

public class TestSso {

    private final static URI RULEBOOK_TARGET = URI.create("urn:rulebook:trustworthy-leaders:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691");
    private final static URI MOD_TARGET = URI.create("urn:rulebook:trustworthy-leaders:exonym:exonym-leads:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691");
    private final static URI MOD_TO_BLACKLIST = URI.create("urn:rulebook:trustworthy-leaders:exonym:interpretation:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691");
    private final static URI TARGET_SSO_URL = URI.create("http://localhost:20001");


    @BeforeClass
    public static void beforeClass() throws Exception {
        PassStore p = pass();
        Path path = path();
        Path containers = ExonymToolset.pathToContainers(path);
        Path container = containers.resolve(p.getUsername());

        if (!Files.exists(container)){
            NetworkMap nm = new NetworkMap(path.resolve("network-map"));
            nm.spawnIfDoesNotExist();
            IdContainerJSON x = new IdContainerJSON(containers, p.getUsername(), true);
            ExonymOwner owner = new ExonymOwner(x);
            owner.openContainer(p);
            owner.setupContainerSecret(p.getEncrypt(), p.getDecipher());
            SybilOnboarding.testNet(p, path, "person");
            RulebookOnboarding.onboardRulebook(p, path, MOD_TARGET);

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
            Prove prove = new Prove(store, path());
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
            config.addModeratorToBlacklist(MOD_TO_BLACKLIST);
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

    @Test
    public void nonInteractiveProof() {
        try {
            PassStore store = pass();
            Prove prove = new Prove(store, path());
            NonInteractiveProofRequest proof = new NonInteractiveProofRequest();
            JsonObject metadata = new JsonObject();
            metadata.addProperty("url", "https://exonym.io/");
            JsonArray whitelist = new JsonArray();
            whitelist.add("adult");
            whitelist.add("humor");
            whitelist.add("satire");
            metadata.add("whitelist", whitelist);
            JsonArray blacklist = new JsonArray();
            blacklist.add("religious");
            metadata.add("blacklist", blacklist);
            proof.setMetadata(metadata);

            // it's really difficult to work out the correct id from here.
            proof.getIssuerUids().add("urn:rulebook:trustworthy-leaders:exonym:exonym-leads:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691:d9c5d55f:i");
            String token = prove.nonInteractiveProofRequest(proof);

            // TODO - the set-up for this isn't correct. It assumes an SFTP credential is present. It's likely so that SFTP credentials don't get uploaded to the repo.
            SFTPManager sftp = new SFTPManager(store, path());
            sftp.put("token.xml", token, "urn:rulebook:sybil-test:sftp", "");

//            logger.info(token);

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
        return Path.of("non-resources");
    }

}
