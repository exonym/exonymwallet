package io.exonym.lib.wallet;

import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.helpers.Timing;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;

public class TestSybilOnboarding {
    public final static String username = "mharris";
    public final static String password = "password";

    @BeforeClass
    public static void beforeClass() throws Exception {
        PassStore store = new PassStore(password, false);
        XContainerJSON x = new XContainerJSON(ExonymToolset.pathToContainers(Path.of("resource")), username, true);
        ExonymOwner o = new ExonymOwner(x);
        o.openContainer(store);
        o.setupContainerSecret(store.getEncrypt(), store.getDecipher());

    }

    @After
    public void tearDown() throws Exception {
        XContainerJSON x = new XContainerJSON(ExonymToolset.pathToContainers(Path.of("resource")), username, false);
        x.delete();

    }

    @Test
    public void sybilTestNetOnboarding() {
        try {
            long t = Timing.currentTime();
            PassStore store = new PassStore(password, false);
            store.setUsername(username);
            String r = SybilOnboarding.testNet(store, Path.of("resource"), Rulebook.SYBIL_CLASS_PERSON);
            System.out.println("Took " + Timing.hasBeenMs(t));
            System.out.println(r);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
