package io.exonym.lib.wallet;

import io.exonym.lib.api.Cache;
import io.exonym.lib.api.PkiExternalResourceContainer;
import io.exonym.lib.helpers.C30Player;
import io.exonym.lib.helpers.Timing;
import io.exonym.lib.pojo.XKey;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Logger;

public class TestC30Onboarding {
    
    private final static Logger logger = Logger.getLogger(TestC30Onboarding.class.getName());

    private static C30Player player0FromSybil;

    @BeforeClass
    public static void beforeClass() throws Exception {
        NetworkMap nm = new NetworkMap(TestTools.STORE_PATH.resolve("network-map"));
        nm.spawnIfDoesNotExist();
        Cache cache = new Cache(TestTools.STORE_PATH);
        PkiExternalResourceContainer.getInstance().setNetworkMapAndCache(nm, cache);

    }

    @Test
    public void straightOnboard() {
        try {
            C30Player player = registerNewPlayer();
            String alpha = player.getAlpha();
            String beta = player.getBeta();

            long t0 = Timing.currentTime();

            String message = C30Utils.joinToAuthProtocol(alpha, beta,
                    TestTools.game0.getGamma(), TestTools.STORE_PATH, false);
            assert message.contains("success");

            logger.info("Took Total:" + Timing.hasBeenMs(t0));

        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;

        }
    }

    @Test
    public void onboardFromSybilOnly() {
        try {
            C30Player player = registerNewPlayer();
            String alpha = player.getAlpha();
            String beta = player.getBeta();

            String message = C30Utils.joinToAuthProtocol(alpha, beta, TestTools.game0.getGamma(),
                    TestTools.STORE_PATH, false, C30Utils.STOPPER_SYBIL);
            logger.info(message);

            long t0 = Timing.currentTime();

            message = C30Utils.joinToAuthProtocol(alpha, beta, TestTools.game0.getGamma(),
                    TestTools.STORE_PATH, false);
            assert message.contains("success");

            logger.info("Took Total:" + Timing.hasBeenMs(t0));

        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;

        }
    }

    @Test
    public void onboardFromProofOnly() {
        try {
            C30Player player = registerNewPlayer();
            String alpha = player.getAlpha();
            String beta = player.getBeta();

            String message = C30Utils.joinToAuthProtocol(alpha, beta, TestTools.game0.getGamma(),
                    TestTools.STORE_PATH, false, C30Utils.STOPPER_RULEBOOK);
            logger.info(message);

            long t0 = Timing.currentTime();

            message = C30Utils.joinToAuthProtocol(alpha, beta, TestTools.game0.getGamma(),
                    TestTools.STORE_PATH, false);
            assert message.contains("success");

            logger.info("Took Total:" + Timing.hasBeenMs(t0));

        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;

        }
    }


    private C30Player registerNewPlayer() throws Exception {
        C30Player player = C30Player.init();

        XKey key = C30Utils.generateNewPlayerKeyForGamma(
                TestTools.STORE_PATH, player.getAlpha(),player.getBeta());

        XKey.assembleAsym(player.getEpsilon(), key);

        String kb64 = Base64.encodeBase64String(key.getPublicKey());

        TestTools.registerPlayerOnGameServers(player);
        TestTools.registerPlayerOnC30Server(player, TestTools.game0, kb64);
        TestTools.registerPlayerOnC30Server(player, TestTools.game1, kb64);
        return player;

    }
}
