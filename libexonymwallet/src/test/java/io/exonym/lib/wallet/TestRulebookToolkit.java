package io.exonym.lib.wallet;

import com.google.gson.Gson;
import io.exonym.lib.api.RulebookCreator;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.RulebookVerifier;
import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.lite.SFTPClient;
import io.exonym.lib.lite.SFTPLogonData;
import io.exonym.lib.pojo.NetworkMapItemAdvocate;
import io.exonym.lib.pojo.NetworkMapItemSource;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.standard.WhiteList;
import io.exonym.lib.helpers.Timing;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TestRulebookToolkit {


    private static Logger logger = Logger.getLogger(TestRulebookToolkit.class.getName());


    @Test
    public void buildSybilRulebook() {
        try {
//            RulebookCreator creator = new RulebookCreator("sources", "resource");
//            RulebookCreator creator0 = new RulebookCreator("sybil", "resource");
            RulebookVerifier verifier = new RulebookVerifier(Path.of("resource", "sybil-rulebook.json").toString());

        } catch (Exception e) {
            String a = ExceptionUtils.getStackTrace(e);
            logger.info(a);
            assert false;

        }
    }

    // todo assertions
    @Test
    public void sftpSetup() {
        try {
            // sftp template

            Path working = Path.of("resource");
            XContainerJSON x = new XContainerJSON(
                    ExonymToolset.pathToContainers(working), "mjh", true);

            ExonymOwner owner = new ExonymOwner(x);
            PassStore passStore = new PassStore("password", false);
            passStore.setUsername("mjh");
            owner.openContainer(passStore);
            owner.setupContainerSecret(passStore.getEncrypt(), passStore.getDecipher());

//            SFTPCredentialManager.createTemplate(working);
            SFTPCredentialManager.add(passStore, working);
            SFTPCredentialManager.remove(passStore, "urn:rulebook:exonym-trust:sftp", working);


            // sftp add filename
            // sftp remove name


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    // todo assertions
    @Test
    public void viewAndListActors() {
        try {
            Gson gson = JaxbHelper.gson;
            NetworkMapInspector inspector = new NetworkMapInspector(
                    new NetworkMap(Path.of("resource", "network-map")));
            String r0 = inspector.listActors(null);
            System.out.println("rulebooks list");
            System.out.println(r0);
            String rulebook = ((ArrayList<String>)
                    gson.fromJson(r0, ArrayList.class)).get(0);

            String r1 = inspector.listActors(rulebook);
            System.out.println("rulebooks list --uid--");
            System.out.println(r1);

            String source = ((ArrayList<String>)
                    gson.fromJson(r1, ArrayList.class)).get(0);
            String r2 = inspector.listActors(source);
            System.out.println("sources list --uid--");
            System.out.println(r2);


            String r2s = inspector.viewActor(source);
            System.out.println("sources view --uid--");
            System.out.println(r2s);

            NetworkMapItemSource nmis = JaxbHelper.gson.fromJson(r2, NetworkMapItemSource.class);
            ArrayList<URI> advocates = nmis.getAdvocatesForSource();
            URI advocate = advocates.get(0);
            String r3 = inspector.listActors(advocate.toString());
            System.out.println("avocates list --uid--");
            System.out.println(r3);
            String r4 = inspector.viewActor(advocate.toString());
            System.out.println("avocates view --uid--");
            System.out.println(r4);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void networkMapCreateAndDelete() {
        try {
            NetworkMap map = new NetworkMap(Path.of("resource", "test-network-map"));
            assert !map.networkMapExists();
            map.spawn();

            NetworkMapInspector inspector = new NetworkMapInspector(map);
            String s = inspector.listActors(null);
            String ruid = "urn:rulebook:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa";
            System.out.println(s);
            System.out.println(inspector.viewActor(ruid));
            String sybilSource = inspector.viewActor("urn:rulebook:sybil:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa");
            System.out.println(sybilSource);
            String sybilTest = inspector.viewActor("urn:rulebook:sybil:test-net:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa");
            System.out.println(sybilTest);

            String v = inspector.listActors("urn:rulebook:sybil:test-net:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa");
            System.out.println(v);
            String t = inspector.listActors("urn:rulebook:sybil:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa");
            System.out.println(t);

            assert map.networkMapExists();
            map.delete();
            assert !map.networkMapExists();



        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    @Test
    public void testNetworkMap() {
        try {
            NetworkMap networkMap = new NetworkMap(Path.of("resource", "network-map"));
            networkMap.spawn();
            String rulebookId = Namespace.URN_PREFIX_COLON + "69bb840695e4fd79a00577de5f0071b311bbd8600430f6d0da8f865c5c459d44";
            String sybilId = Rulebook.SYBIL_RULEBOOK_ID.toString();

            List<String> sources = networkMap.getSourceFilenamesForRulebook(rulebookId);
            for (String source : sources){
                URI sourceUid = networkMap.fromNmiFilename(source);
                NetworkMapItemSource smi = (NetworkMapItemSource) networkMap.nmiForNode(sourceUid);
                List<URI> advocates = smi.getAdvocatesForSource();
                System.out.println(smi.getNodeUID() + " " + WhiteList.isSourceUid(smi.getNodeUID()) + " ");
                for (URI advocate : advocates){
                    long t = Timing.currentTime();
                    NetworkMapItemAdvocate nmia = (NetworkMapItemAdvocate) networkMap.nmiForNode(advocate);
                    long e = Timing.hasBeenMs(t);
                    System.out.println(nmia.getNodeUID()
                            + " " + e + " " +
                            WhiteList.isAdvocateUid(nmia.getNodeUID()));

                }
            }
        } catch (Exception e) {
            String a = ExceptionUtils.getStackTrace(e);
            System.out.println(a);
            assert false;
            throw new RuntimeException(e);

        }
    }

    @Test
    public void uidWhitelist() {
        assert(WhiteList.isAdvocateUid(
                URI.create("urn:rulebook:sybil-a:main-a:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(WhiteList.isAdvocateUid(
                URI.create("urn:rulebook:sybil:main:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(!WhiteList.isAdvocateUid(
                URI.create("urn:ruleboo:sybil:main:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(!WhiteList.isAdvocateUid(
                URI.create("urn:rulebook:sybil:main:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa:")));

        assert(WhiteList.isSourceUid(
                URI.create("urn:rulebook:sybi-a:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(WhiteList.isSourceUid(
                URI.create("urn:rulebook:sybil:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(!WhiteList.isSourceUid(
                URI.create("urn:ruleboo:sybil:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(!WhiteList.isSourceUid(
                URI.create("urn:rulebook:sybil:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa:")));
        assert(!WhiteList.isSourceUid(
                URI.create("urn:rulebook:sybil:main:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa:")));

    }

}
