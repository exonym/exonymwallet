package io.exonym.lib.wallet;

import com.google.gson.Gson;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.RulebookCreator;
import io.exonym.lib.api.RulebookVerifier;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.pojo.NetworkMapItemModerator;
import io.exonym.lib.pojo.NetworkMapItemLead;
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
            String root = "non-resources";
            RulebookCreator creator = new RulebookCreator("leads", root);
            RulebookCreator creator0 = new RulebookCreator("sybil", root);
            RulebookVerifier verifier = new RulebookVerifier(
                    Path.of(root, "sybil-rulebook-test.json").toString());

        } catch (Exception e) {
            String a = ExceptionUtils.getStackTrace(e);
            System.out.println(a);
            assert false;

        }
    }

    // todo assertions
    @Test
    public void sftpSetup() {
        try {
            // sftp template
            Path working = Path.of("non-resources");
//            SFTPManager.createTemplate(working);
            String username = "mharris";


            IdContainerJSON x = new IdContainerJSON(
                    ExonymToolset.pathToContainers(working), username, true);

            ExonymOwner owner = new ExonymOwner(x);
            PassStore passStore = new PassStore("password", false);
            passStore.setUsername(username);
            owner.openContainer(passStore);
            owner.setupContainerSecret(passStore.getEncrypt(), passStore.getDecipher());
            SFTPManager sftp = new SFTPManager(passStore, working);

            sftp.add();

            // sftp.remove("urn:rulebook:exonym-trust:sftp");

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

            String lead = ((ArrayList<String>)
                    gson.fromJson(r1, ArrayList.class)).get(0);
            String r2 = inspector.listActors(lead);
            System.out.println("leads list --uid--");
            System.out.println(r2);

            String r2s = inspector.viewActor(lead);
            System.out.println("leads view --uid--");
            System.out.println(r2s);

            NetworkMapItemLead nmis = JaxbHelper.gson.fromJson(r2, NetworkMapItemLead.class);
            ArrayList<URI> mods = nmis.getModeratorsForLead();
            URI advocate = mods.get(0);
            String r3 = inspector.listActors(advocate.toString());
            System.out.println("mods list --uid--");
            System.out.println(r3);
            String r4 = inspector.viewActor(advocate.toString());
            System.out.println("mods view --uid--");
            System.out.println(r4);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void networkMapCreateAndDelete() {
        try {
            Path nmPath = Path.of("resource", "test-network-map");

            NetworkMap map = new NetworkMap(nmPath);
//            assert !map.networkMapExists();
            map.spawn();

            NetworkMapInspector inspector = new NetworkMapInspector(map);
            String s = inspector.listActors(null);
            String ruid = Rulebook.SYBIL_RULEBOOK_UID_TEST.toString();
            System.out.println(s);
            System.out.println(inspector.viewActor(ruid));
            String sybilSource = inspector.viewActor(Rulebook.SYBIL_LEAD_UID_TEST.toString());
            System.out.println(sybilSource);
            String sybilTest = inspector.viewActor(Rulebook.SYBIL_MOD_UID_TEST.toString());
            System.out.println(sybilTest);
            String v = inspector.listActors(Rulebook.SYBIL_MOD_UID_TEST.toString());
            System.out.println(v);
            String t = inspector.listActors(Rulebook.SYBIL_LEAD_UID_TEST.toString());
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
            // TODO

            String rulebookId = Rulebook.SYBIL_RULEBOOK_UID_TEST.toString();
            URI sybilId = Rulebook.SYBIL_RULEBOOK_UID_TEST;

            List<String> leads = networkMap.getLeadFileNamesForRulebook(rulebookId);

            for (String lead : leads){
                URI leadUid = networkMap.fromNmiFilename(lead);
                NetworkMapItemLead smi = (NetworkMapItemLead) networkMap.nmiForNode(leadUid);
                List<URI> moderators = smi.getModeratorsForLead();
                System.out.println(smi.getNodeUID() + " isLeadUid=" + WhiteList.isLeadUid(smi.getNodeUID()) + " ");
                for (URI moderator : moderators){
                    long t = Timing.currentTime();
                    NetworkMapItemModerator nmia = (NetworkMapItemModerator) networkMap.nmiForNode(moderator);
                    long e = Timing.hasBeenMs(t);
                    System.out.println(nmia.getNodeUID()
                            + " " + e + " " +
                            WhiteList.isModeratorUid(nmia.getNodeUID()));

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
        assert(WhiteList.isModeratorUid(
                URI.create("urn:rulebook:sybil:sybil-a:main-a:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(WhiteList.isModeratorUid(
                URI.create("urn:rulebook:sybil:sybil:main:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(!WhiteList.isModeratorUid(
                URI.create("urn:ruleboo:sybil:sybil:main:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(!WhiteList.isModeratorUid(
                URI.create("urn:rulebook:sybil:sybil:main:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa:")));

        assert(WhiteList.isLeadUid(
                URI.create("urn:rulebook:sybil:sybi-a:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(WhiteList.isLeadUid(
                URI.create("urn:rulebook:sybil:sybil:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(!WhiteList.isLeadUid(
                URI.create("urn:ruleboo:sybil:sybil:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa")));
        assert(!WhiteList.isLeadUid(
                URI.create("urn:rulebook:sybil:sybil:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa:")));
        assert(!WhiteList.isLeadUid(
                URI.create("urn:rulebook:sybil:sybil:main:7a13071495188f94e6bc1432f90981160ce730d7d7cd01f3f539d7e4f0e55afa:")));

    }

}
