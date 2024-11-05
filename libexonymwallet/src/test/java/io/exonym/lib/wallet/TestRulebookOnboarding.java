package io.exonym.lib.wallet;

import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.NetworkMapItemModerator;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.helpers.Timing;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class TestRulebookOnboarding {

    private final static Logger logger = Logger.getLogger(
            TestRulebookOnboarding.class.getName());
    private final static String username = "mharris";
    private final static String password = "password";

    @BeforeClass
    public static void beforeClass() throws Exception {
        Path path = ExonymToolset.pathToContainers(Path.of("resource"));
        PassStore store = new PassStore(password, false);
        IdContainerJSON x = new IdContainerJSON(path, username, true);
        ExonymOwner o = new ExonymOwner(x);
        o.openContainer(store);
        o.setupContainerSecret(store.getEncrypt(), store.getDecipher());

    }

    @After
    public void tearDown() throws Exception {
        Path path = ExonymToolset.pathToContainers(Path.of("resource"));
        IdContainerJSON x = new IdContainerJSON(path, username, false);
        x.delete();

    }

    @Test
    public void rulebookOnboarding() {
        try {
            PassStore store = new PassStore(password, false);
            store.setUsername(username);
            Path where = TestTools.STORE_PATH;
            String sybilResult = SybilOnboarding.testNet(store, where,
                    SybilOnboarding.SYBIL_URL_TEST_NET, Rulebook.SYBIL_CLASS_PERSON);
            System.out.println(sybilResult);

            NetworkMap map = new NetworkMap(where.resolve("network-map"));

            List<String> files = map.getLeadFileNamesForRulebook(
                    Rulebook.SYBIL_RULEBOOK_UID_TEST.toString());

            NetworkMapItemModerator mod = (NetworkMapItemModerator)
                    map.nmiForNode(URI.create(
                            "urn:rulebook:trustworthy-leaders:exonym:exonym-leads:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691"));

            System.out.println(mod.getNodeUID());

            long t = Timing.currentTime();
            String result = RulebookOnboarding.onboardRulebook(store, where, mod.getNodeUID());

            System.out.println("Took " + Timing.hasBeenMs(t));
            System.out.println(result);

        } catch (UxException e) {
            logger.info(e.getMessage());
            if (e.getInfo()!=null){
                for (String info : e.getInfo()){
                    logger.info(info);

                }
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }


}