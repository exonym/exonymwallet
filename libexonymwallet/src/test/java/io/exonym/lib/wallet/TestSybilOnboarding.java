package io.exonym.lib.wallet;

import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.helpers.Timing;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestSybilOnboarding {
    public final static String username = "mharris";
    public final static String password = "password";

    private final static Logger logger = Logger.getLogger(TestSybilOnboarding.class.getName());

    @BeforeClass
    public static void beforeClass() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource("logging.properties");
        if (resource != null) {
            System.out.println("Found logging.properties at: " + resource.getPath());
        } else {
            System.out.println("logging.properties not found on the classpath");
        }

        logger.info("Info log");
        logger.fine("Fine log");


        PassStore store = new PassStore(password, false);
        IdContainerJSON x = new IdContainerJSON(
                ExonymToolset.pathToContainers(Path.of("non-resources")), username, true);
        ExonymOwner o = new ExonymOwner(x);
        o.openContainer(store);
        o.setupContainerSecret(store.getEncrypt(), store.getDecipher());

    }

    @After
    public void tearDown() throws Exception {
        IdContainerJSON x = new IdContainerJSON(
                ExonymToolset.pathToContainers(
                        Path.of("resource")), username, false);
        x.delete();

    }

    @Test
    public void sybilTestNetOnboarding() {
        try {
            long t = Timing.currentTime();
            PassStore store = new PassStore(password, false);
            store.setUsername(username);
            String r = SybilOnboarding.testNet(store, Path.of("resource"),
                    SybilOnboarding.SYBIL_URL_TEST_NET, Rulebook.SYBIL_CLASS_PERSON);
            System.out.println("Took " + Timing.hasBeenMs(t));
            System.out.println(r);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }
}
