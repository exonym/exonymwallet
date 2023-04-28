package io.exonym.lib.wallet;

import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.NetworkMapItemAdvocate;
import io.exonym.lib.pojo.NetworkMapItemSource;
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

public class RulebookOnboardingTest  {

    private final static Logger logger = Logger.getLogger(RulebookOnboardingTest.class.getName());
    private final static String username = "mharris";
    private final static String password = "password";

    @BeforeClass
    public static void beforeClass() throws Exception {
        Path path = ExonymToolset.pathToContainers(Path.of("resource"));
        PassStore store = new PassStore(password, false);
        XContainerJSON x = new XContainerJSON(path, username, true);
        ExonymOwner o = new ExonymOwner(x);
        o.openContainer(store);
        o.setupContainerSecret(store.getEncrypt(), store.getDecipher());

    }

    @After
    public void tearDown() throws Exception {
        Path path = ExonymToolset.pathToContainers(Path.of("resource"));
        XContainerJSON x = new XContainerJSON(path, username, false);
//        x.delete();

    }

    @Test
    public void rulebookOnboarding() {
        try {
            PassStore store = new PassStore(password, false);
            store.setUsername(username);
            Path where = Path.of("resource");
            String sybilResult = SybilOnboarding.testNet(store, where, Rulebook.SYBIL_CLASS_PERSON);
            System.out.println(sybilResult);

            NetworkMap map = new NetworkMap(where.resolve("network-map"));
            List<String> files = map.getSourceFilenamesForRulebook(
                    "29a655983776d9cd7b4be696ed4cd773e63e6d640241e05c3a40b5d81f5d1f1c");
            NetworkMapItemAdvocate advocate = (NetworkMapItemAdvocate)
                    // map.nmiForNode(source.getAdvocatesForSource().get(0));
                    map.nmiForNode(URI.create("urn:rulebook:exonym:trusted-sources:29a655983776d9cd7b4be696ed4cd773e63e6d640241e05c3a40b5d81f5d1f1c"));
            System.out.println(advocate.getNodeUID());

            long t = Timing.currentTime();

            String result = RulebookOnboarding.onboardRulebook(store, where, advocate.getNodeUID());

            System.out.println("Took " + Timing.hasBeenMs(t));
            System.out.println(result);

        } catch (UxException e) {
            logger.info(e.getMessage());
            for (String info : e.getInfo()){
                logger.info(info);

            }
            throw new RuntimeException(e);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }


}