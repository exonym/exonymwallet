package io.exonym.lib.wallet;


import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.Cache;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.helpers.ProbeCallBack;
import io.exonym.lib.helpers.Timing;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.lite.Http;
import io.exonym.lib.pojo.*;
import io.exonym.lib.standard.Const;
import io.exonym.lib.standard.PassStore;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Sybil + 2 Rulebook Nodes + SSO Example Required
 */
public class TestRevocation {

    private final static Logger logger = Logger.getLogger(TestRulebookOnboarding.class.getName());

    private static final String PREFIX_NODE_0 = "node0_";
    private static final String PREFIX_NODE_1 = "node1_";

    private static final String BASE_SSO = "http://localhost:20001/";

    @BeforeClass
    public static void beforeClass() throws Exception {
        //"../"

        logger.info(TestTools.TOKENS.toString());

        long t0 = Timing.currentTime();
        int countNode0 = 1;
        int countNode1 = 0;
        TestTools.setupBase(countNode0, PREFIX_NODE_0);
        TestTools.setupBase(countNode1, PREFIX_NODE_1);
        logger.info("Base complete in " + Timing.hasBeenMs(t0));

        TestTools.addSybilPersonCredential(countNode0, PREFIX_NODE_0);
        TestTools.addSybilPersonCredential(countNode1, PREFIX_NODE_1);

        TestTools.addRulebookCredential(countNode0, PREFIX_NODE_0, TestTools.MOD0_UID);
        TestTools.addRulebookCredential(countNode1, PREFIX_NODE_1, TestTools.MOD1_UID);
        logger.info("Set-up complete in " + Timing.hasBeenMs(t0) + " with " + (countNode0 + countNode1) + " accounts");

    }

    @Test
    public void revokeAllTokensInSsoFolder() {
        try {
            String node = PREFIX_NODE_0;
//            System.exit(1);
            Http http = new Http();
            http.newContext();

            Path exonym = Path.of("exonym");
            Path rb = exonym.resolve("rulebooks");
            URI challengeEndpoint = URI.create(BASE_SSO + rb);
            URI authEndpoint = URI.create(BASE_SSO + exonym);

            String endonym = TestTools.authenticateWithSso(
                    http, node, challengeEndpoint, authEndpoint, false);

            logger.info(endonym);
            assert endonym.startsWith(Namespace.ENDONYM_PREFIX);

//            System.exit(1);

            Cache cache = new Cache(TestTools.STORE_PATH);
            Rulebook rulebook = cache.open(TestTools.RULEBOOK_UID);
            String rn = rulebook.getRules()
                    .get(rulebook.getRules().size()-1).getId();
            URI rnUID = URI.create(rn);

            ArrayList<String> targets = new ArrayList<>();
            targets.add(TestTools.stripEndonym(endonym));

            ArrayList<RevocationRequest> revokeTokens = TestTools.prepareRevocationRequests(
                    targets, rnUID, "They complemented my mother!");

            RevocationRequestWrapper wrapper = new RevocationRequestWrapper();
            wrapper.setRequests(revokeTokens);
            String revocationPost = JaxbHelper.gson.toJson(wrapper);

            NetworkMap nm = new NetworkMap(TestTools.STORE_PATH.resolve("network-map"));
            NetworkMapItemModerator nmim = (NetworkMapItemModerator) nm.nmiForNode(TestTools.MOD0_UID);

            URI revokeEndPoint = nmim.getRulebookNodeURL().resolve("revoke");
            String result = http.basicPost(revokeEndPoint.toString(), revocationPost);
            logger.info(revokeEndPoint.toString());
            logger.info(result);

            Thread.sleep(1000);

            String error = TestTools.authenticateWithSso(http, node, challengeEndpoint, authEndpoint, true);
            logger.info(error);
            assert error!=null && error.equals("{\"error\":\"FAILED_TO_AUTHORIZE\"}");

        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;

        }
    }

    @Test
    public void rejoin() {
        try {
            String node = PREFIX_NODE_0;
            Http http = new Http();
            http.newContext();

            PassStore store = new PassStore(TestTools.PASSWORD, false);
            store.setUsername(node + "0");

            String response = TestTools.augmentOwnerWithRulebook(store, TestTools.MOD1_UID);
            logger.info(response);
            RejoinCriteria rejoin = JaxbHelper.gson.fromJson(response, RejoinCriteria.class);

            if (!rejoin.isCanRejoin()){
                // appeal
                URI revokedMod = rejoin.getRevokedModerators().get(0);
                URI leadUid = UIDHelper.computeLeadUidFromModUid(revokedMod);
                NetworkMap nm = new NetworkMap(TestTools.STORE_PATH.resolve("network-map"));

                NetworkMapItemLead nmim = (NetworkMapItemLead) nm.nmiForNode(leadUid);
                String url = nmim.getRulebookNodeURL().toString() + Const.ENDPOINT_APPEAL;

                String challenge = http.basicGet(url);
                Rulebook rulebook = JaxbHelper.jsonToClass(challenge, Rulebook.class);

                ProbeCallBack probe = new ProbeCallBack(http, new URL(url));

                String walletResponse = RulebookOnboarding.
                        onboardRulebook(store, TestTools.STORE_PATH, rulebook.getLink());
                logger.info(walletResponse);
                String uiResponse = probe.getResult();
                logger.info(uiResponse);

            } else {
                logger.info("Rejoined successfully, run prove.");
                // prove
            }
        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;
        }
    }


    @Test
    public void violationOverride() {
        String nibble6 = "357d89";
        String x0Hash = "c041e73f65578034b33e068daab5c30b0344b19e8f83a19cf84323349f4b26f7";
        String modUid = "urn:rulebook:trustworthy-leaders:exonym:interpretation:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691";
        String tov = "2024-10-14T13:04:44Z";

        try {
            OverrideRequest plain = new OverrideRequest();

            plain.setNibble6(nibble6);
            plain.setX0Hash(x0Hash);
            plain.setTimeOfViolation(tov);
            plain.setModOfVioUid(URI.create(modUid));

            plain.setType(OverrideRequest.TYPE_PLAIN);
            HashMap<String, String> header = new HashMap<>();
            header = TestTools.generateHeaders(TestTools.NODE_0_API);

            NetworkMap nm = new NetworkMap(TestTools.STORE_PATH.resolve("network-map"));
            NetworkMapItemModerator nmim = (NetworkMapItemModerator) nm.nmiForNode(TestTools.MOD0_UID);
            URI modEndpoint = nmim.getRulebookNodeURL().resolve("mod/revert");

            Http http = new Http();
            String r = http.basicPost(
                    modEndpoint.toString(), JaxbHelper.gson.toJson(plain), header);
            logger.info(r);

        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;

        }
    }


    @Test
    public void deleteCredential() {
        try {
            Path path = ExonymToolset.pathToContainers(TestTools.STORE_PATH);
            PassStore store = new PassStore(TestTools.PASSWORD, false);
            IdContainerJSON id = new IdContainerJSON(path,PREFIX_NODE_1 + "0");
            ArrayList<String> owned = id.getOwnerSecretList();
            // The container needs to
            for (String c : owned){
                logger.info(c.toString());
            }
//            id.deleteCredential("trustworthy", "ic", store);



        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;
        }
    }

    @Test
    public void revokeSingleToken() {

        try {
            String endonymTail = "3636b8f21c9d2d396c459365b7a83da3cb715597f54efb83b90d516468560344";

            Cache cache = new Cache(TestTools.STORE_PATH);
            Rulebook rulebook = cache.open(TestTools.RULEBOOK_UID);
            String rn = rulebook.getRules().get(rulebook.getRules().size()-1).getId();
            URI rnUID = URI.create(rn);

            Http http = new Http();
            http.newContext();

            ArrayList<String> targets = new ArrayList<>();
            targets.add(endonymTail);

            ArrayList<RevocationRequest> revokeTokens = TestTools.prepareRevocationRequests(
                    targets, rnUID, "They complemented my mother!");

            RevocationRequestWrapper wrapper = new RevocationRequestWrapper();
            wrapper.setRequests(revokeTokens);
            String revocationPost = JaxbHelper.gson.toJson(wrapper);

            NetworkMap nm = new NetworkMap(TestTools.STORE_PATH.resolve("network-map"));
            nm.spawnIfDoesNotExist();
            NetworkMapItemModerator nmim = (NetworkMapItemModerator) nm.nmiForNode(TestTools.MOD0_UID);

            URI revokeEndPoint = nmim.getRulebookNodeURL().resolve("revoke");
            String result = http.basicPost(revokeEndPoint.toString(), revocationPost);
            logger.info(revokeEndPoint.toString());
            logger.info(result);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }



    @Test
    public void revokeTokenByName() {
        try {
            String filename = "";
            String url = "http://localhost:" + TestTools.NODE_0_PORT;
            if (!filename.equals("")){
                URI rn = TestTools.getValidRulebookRuleUid(2, TestTools.RULEBOOK_UID);
                String json = TestTools.revocationRequest(filename, TestTools.NODE_0_API, rn);
                url += "/revoke";
                Http http = new Http();
                http.basicPost(url, json);

            } else {
                logger.info("skipped");

            }
        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;

        }
    }


}
