/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.wallet;

import com.google.gson.JsonObject;
import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.BuildCredentialSpecification;
import io.exonym.lib.helpers.BuildPresentationPolicy;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.actor.VerifiedClaim;
import io.exonym.lib.actor.IdContainerExternal;
import io.exonym.lib.api.SsoConfigWrapper;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.helpers.WordSets;
import io.exonym.lib.helpers.UrlHelper;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.lite.Http;
import io.exonym.lib.lite.SFTPClient;
import io.exonym.lib.lite.SFTPLogonData;
import io.exonym.lib.pojo.*;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.helpers.Timing;
import io.exonym.lib.standard.WhiteList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraalVMProbeMain {

    private static Logger logger = Logger.getLogger(GraalVMProbeMain.class.getName());

    private static void openWords() {
        for (char alpha='a'; alpha<='z'; alpha++){
            for (int length=3;length<13; length++){
                try {
                    String word = WordSets.findAppropriateWord(length,alpha);
                    int l = word.length();
                    int c = word.toCharArray()[0];
                    assert (l==length);
                    assert (c==alpha);

                } catch (Exception e) {
                    if (!(alpha=='z' && length==12)){
                        System.out.println("Failed to find " + alpha + " " + length);
                    }
                }
            }
        }
    }


    public static void openSystemParams() throws Exception {
        try {
            logger.info("Opening System Params - lambda.xml");
            SystemParameters params = IdContainerExternal.openSystemParameters();
            logger.info("Opened System Params");
            String xml =  IdContainer.convertObjectToXml(params);
            String sha256Sp = CryptoUtils.computeSha256HashAsHex(xml);

            logger.info("System Params = " + xml + " " + sha256Sp);

        } catch (Exception e) {
            throw e;

        }
    }

    public static void containerManagement() throws Exception {
        logger.info("Container Management");
        IdContainerJSON x = null;
        Path path = ExonymToolset.pathToContainers(path());

        String name = "mike";
        try {
            x = new IdContainerJSON(path, name, true);
            logger.info("Container Created");
            x = new IdContainerJSON(path, name, false);
            logger.info("Container Reopened");

        } catch (UxException e) {
            logger.info("Container Existed");
            x = new IdContainerJSON(path, name, false);

        }
        logger.info("Deleting Container");
        x.delete();

        logger.info("" + x);

    }

    public static void newContainerWithSecret() throws Exception {
        try {
            Path path = ExonymToolset.pathToContainers(path());
            logger.log(Level.INFO, "New container with secret");
            String username = generateUniqueUsername();
            logger.log(Level.INFO, "username=" + username);
            IdContainerJSON x = new IdContainerJSON(path, username, true);
            logger.log(Level.INFO, "Container created " + x);
            PassStore store = new PassStore("password", false);
            logger.log(Level.INFO, "PassStore defined " + store);
            ExonymOwner owner = new ExonymOwner(x);
            logger.log(Level.INFO, "Owner Initialised " + owner);
            byte[] a = ExonymOwner.toUnsignedByteArray(PassStore.initNew("password"));
            logger.log(Level.INFO, "Generated Unsigned Byte Array " + a);
            owner.openContainer(store);
            logger.log(Level.INFO, "Opened container " + owner);
            owner.setupContainerSecret(store.getEncrypt(), store.getDecipher());
            logger.log(Level.INFO, "Owner Authenticated " + owner);
            logger.log(Level.INFO, "Secret saved " + owner);
            IdContainerSchema schema = x.getSchema();
            logger.log(Level.INFO, "Schema username is " + schema.getUsername() + " original= " + username);
            String json = JaxbHelper.serializeToJson(schema, IdContainerSchema.class);
            logger.log(Level.INFO, json);
            owner.authenticate(store);

        } catch (Exception e) {
            throw e;

        }
    }

    private static void setupIssuanceTestProve() throws Exception{
        String issuername = "test_issuer";
        Path pathContainers = ExonymToolset.pathToContainers(path());
        Path path = pathContainers.resolve("test_issuer");
        boolean create = !Files.exists(path);

        PassStore p = new PassStore("password", false);
        IdContainerJSON xi = new IdContainerJSON(pathContainers, issuername, create);
        ExonymIssuer issuer = new ExonymIssuer(xi);

        String csUidString = "urn:io:exonym:test:c";
        String ppUidString = "urn:io:exonym:test:pp";
        String iUidString = "urn:io:exonym:test:public-id:i";
        String raUidString = "urn:io:exonym:test:public-id:ra";
        String raiUidString = "urn:io:exonym:test:public-id:rai";
        URI csUid = URI.create(csUidString);
        URI ppUid = URI.create(ppUidString);
        URI iUid = URI.create(iUidString);
        URI raUid = URI.create(raUidString);
        URI raiUid = URI.create(raiUidString);
        URI context = URI.create("urm:context");
        BuildCredentialSpecification bcs = new BuildCredentialSpecification(csUid, true);
        issuer.addCredentialSpecification(bcs.getCredentialSpecification());
        issuer.setupAsRevocationAuthority(iUid, p.getEncrypt());
        issuer.setupAsCredentialIssuer(csUid, iUid, raUid, p.getEncrypt());
        CredentialSpecification cs = bcs.getCredentialSpecification();

        VerifiedClaim claim = new VerifiedClaim(cs);
        IssuancePolicy ip = issuer.publicParameterOpener(URI.create("urn:io:exonym:test:public-id:ip"));
        IssuanceMessageAndBoolean imab = issuer.issueInit(claim, ip, p.getEncrypt(), context);

        String username = generateUniqueUsername().substring(0,8) + "_user";
        IdContainerJSON xu = new IdContainerJSON(pathContainers, username, true);
        ExonymOwner ou = new ExonymOwner(xu);
        ou.openContainer(p);
        ou.setupContainerSecret(p.getEncrypt(),p.getDecipher());
        ou.addCredentialSpecification(cs);
        ou.addIssuerParameters(xi.openResource(iUid));
        IssuanceMessage im = ou.issuanceStep(imab, p.getEncrypt());

        imab = issuer.issueStep(im, p.getEncrypt());
        ou.issuanceStep(imab, p.getEncrypt());
        logger.info("last=" + imab.isLastMessage());

        BuildPresentationPolicy bpp = new BuildPresentationPolicy(ppUid, cs);
        String rootAlias = "urn:io:exonym";
        bpp.addPseudonym(rootAlias, false, rootAlias);
        bpp.makeInteractive();

        PresentationPolicyAlternatives ppa = bpp.getPolicyAlternatives(issuer.getVerifierParameters());
        PresentationTokenDescription ptd = ou.canProveClaimFromPolicy(ppa);
        PresentationToken pt = ou.proveClaim(ptd, ppa);

        String blankName = "blank_container";
        path = pathContainers.resolve(blankName);
        create = !Files.exists(path);

        IdContainerJSON x = new IdContainerJSON(pathContainers, blankName, create);
        TokenVerifier t = new TokenVerifier(x);
        t.loadCredentialSpecification(cs);
        t.loadRevocationAuthorityParameters((RevocationAuthorityParameters) xi.openResource(raUid));
        t.loadRevocationInformation(xi.openResource(raiUid));
        t.loadIssuerParameters((IssuerParameters) xi.openResource(iUid));
        byte[] nonce = t.verifyToken(ppa, pt);
        String n = Base64.encodeBase64String(nonce);
//            logger.info("result = " + n + " input=" + in);
        // TODO turn it into a Lib and set-up a container.

    }

    private static String generateUniqueUsername() {
        return CryptoUtils.computeSha256HashAsHex("" + Timing.currentTime());
    }

    public static String newCredentialSpec(String uid){
        try {
            BuildCredentialSpecification builder = new BuildCredentialSpecification(URI.create(uid), true);
            CredentialSpecification spec = builder.getCredentialSpecification();
            return IdContainer.convertObjectToXml(spec);

        } catch (Exception e) {
            logger.log(Level.FINE, "Error", e);
            return "Error " + uid; // + " int=" + integer + " decimal=" + decimal;

        }
    }

    public static String readUrl(String url){
        try {

            return new String(UrlHelper.read(new URL(url)));

        } catch (IOException e) {
            logger.log(Level.FINE, "Error", e);
            return e.getMessage();

        }
    }





    private static void networkMapInspector() throws Exception {
        NetworkMap map = new NetworkMap(Path.of("resource", "test-network-map"));
        if (map.networkMapExists()){
            map.delete();
        }
        map.spawn();

        NetworkMapInspector inspector = new NetworkMapInspector(map);
        String s = inspector.listActors(null);
        URI ruid = Rulebook.SYBIL_RULEBOOK_UID_MAIN;
        System.out.println(s);
        System.out.println(inspector.viewActor(ruid.toString()));
        String sybilSource = inspector.viewActor(Rulebook.SYBIL_LEAD_UID_MAIN.toString());
        System.out.println(sybilSource);
        String sybilTest = inspector.viewActor(Rulebook.SYBIL_MOD_UID_MAIN.toString());
        System.out.println(sybilTest);

        String v = inspector.listActors(Rulebook.SYBIL_LEAD_UID_MAIN.toString());
        System.out.println(v);
        String t = inspector.listActors(Rulebook.SYBIL_MOD_UID_MAIN.toString());
        System.out.println(t);

        assert map.networkMapExists();
        map.delete();
        assert !map.networkMapExists();

    }

    private static void networkMapSpawning() throws Exception {
        Path nmPath =Path.of("resource", "network-map");
        NetworkMap networkMap = new NetworkMap(nmPath);
        networkMap.spawn();
        String sybilId = Rulebook.SYBIL_RULEBOOK_UID_MAIN.toString();

        List<String> leads = networkMap.getLeadFileNamesForRulebook(sybilId);
        for (String lead : leads){
            URI sourceUid = networkMap.fromNmiFilename(lead);
            NetworkMapItemLead smi = (NetworkMapItemLead) networkMap.nmiForNode(sourceUid);
            List<URI> mods = smi.getModeratorsForLead();
            System.out.println(smi.getNodeUID() + " " + WhiteList.isLeadUid(smi.getNodeUID()) + " ");
            for (URI advocate : mods){
                long t = Timing.currentTime();
                NetworkMapItemModerator nmia = (NetworkMapItemModerator) networkMap.nmiForNode(advocate);
                long e = Timing.hasBeenMs(t);
                System.out.println(nmia.getNodeUID()
                        + " " + e + " " +
                        WhiteList.isModeratorUid(nmia.getNodeUID()));

            }
        }
    }

    private static void c30SubscriptionLifecycle() throws Exception{
        String gamma = "ff1297feb9c113ff1297fe4bc11312d7";
        String api = "1a0365868d33ae0e4ccac2d25610813d337d18973f0dd5119929c8966546b527";

        String alpha = UUID.randomUUID().toString();
        String beta = UUID.randomUUID().toString();

        String epsilon = CryptoUtils.computeMd5HashAsHex((alpha + beta).getBytes(StandardCharsets.UTF_8));

        Path p = Path.of("identities");
        XKey key = C30Utils.generateNewPlayerKeyForGamma(p.toString(), alpha,beta);
        XKey.assembleAsym(epsilon, key);
        C30Utils.hasPlayerKeyForGame(alpha, beta, p.toString());

        String kb64 = Base64.encodeBase64String(key.getPublicKey());

        // Set-up player access
        Http client = new Http();
        JsonObject gameToPost = new JsonObject();
        gameToPost.addProperty("key", kb64);

        String playerPath = alpha + "/" + beta + "/" + gamma;
        logger.info(playerPath);
        HashMap<String, String> game0Header = new HashMap<>();
        game0Header.put("C30-App-Key", api);

        String response = client.basicPost("https://t1.sybil.cyber30.io/c30/" + playerPath,
                gameToPost.toString(), game0Header);

        logger.info(response);

        // Set-up player on game server
        HashMap<String, String> headersGame0 = new HashMap<>();
        headersGame0.put("kid", "b9c82418-ebe7-45c3-b19b-ca6f7f318867");
        headersGame0.put("key", "0fc8bc7cf26084f7341cb007c5233118ea2aecdf95a4ccc25212d5e8f538966b");

//        Path nmPath = Path.of("resource", "network-map");
        NetworkMap nm = new NetworkMap(p.resolve("network-map"));
        nm.spawnIfDoesNotExist();
        URI mod = URI.create("urn:rulebook:mmo:c30:home:4ccbdf03787d137fc360a193ba950eb77d6b150f99b69280a11dc084f29a2f72");
        NetworkMapItemModerator nmim0 = (NetworkMapItemModerator) nm.nmiForNode(mod);

        URI home0 = nmim0.getRulebookNodeURL();

        StringBuilder url = new StringBuilder();
        url.append("verify/");
        url.append(epsilon);
        url.append("/");
        url.append(C30Utils.getPlayerPublicKeyAsString(p.toString(), alpha, beta));

        logger.info(url.toString());

        String url0 = home0 + url.toString();

        String r0 = client.basicGet(url0.toString(), headersGame0);
        logger.info(r0);

        String message = C30Utils.joinToAuthProtocol(alpha, beta, gamma, p.toString(), false);
        assert message.contains("success");

    }




    private static void subscriptionLifecycle() throws Exception {
        resetContainer(USERNAME);
        PassStore store = pass();
        Path path = path();
        ExonymToolset exo = new ExonymToolset(store, path);
        exo.getOwner().setupContainerSecret(store.getEncrypt(), store.getDecipher());
        exo.getNetworkMap().spawn();

        URI advocateId = URI.create("urn:rulebook:mmo:c30:home:4ccbdf03787d137fc360a193ba950eb77d6b150f99b69280a11dc084f29a2f72");

        SybilOnboarding.testNet(store, path(), SybilOnboarding.SYBIL_URL_TEST_NET,
                Rulebook.SYBIL_CLASS_PERSON);
        RulebookOnboarding.onboardRulebook(store, path, advocateId);

        Prove prove = new Prove(store, path);
        logger.info(prove.walletReport());

        URI target = URI.create("http://localhost:8080");
        SsoConfigWrapper config = new SsoConfigWrapper(target);
        config.requireRulebook(URI.create("urn:rulebook:mmo:4ccbdf03787d137fc360a193ba950eb77d6b150f99b69280a11dc084f29a2f72"));

        SsoChallenge c = SsoChallenge.newChallenge(config.getConfig());
        AuthenticationWrapper w = AuthenticationWrapper.wrapToWrapper(c, 100, SsoChallenge.class);
        try {
            System.out.println(prove.proofForRulebookSSO(w.getLink()));
        } catch (Exception e) {
            assert "SSO_END_POINT_404".equals(e.getMessage());
        }

        // Service gets a request to delegate
        DelegateRequest delegateRequest = DelegateRequest.newDelegateRequest(target.resolve("/asda"));
        AuthenticationWrapper drw = AuthenticationWrapper.wrapToWrapper(delegateRequest, 100, DelegateRequest.class);

        // Service User generates a request for third-party
        String drForThirdParty = prove.generateDelegationRequestForThirdParty(drw.getLink(), "Bob's billing");
        System.out.println(drForThirdParty);

        // Third-party user generates a proof token for Service Owner

        HashMap<String, String> request = JaxbHelper.jsonToClass(drForThirdParty, HashMap.class);
        String proofLink = prove.fillDelegationRequest(request.get("link"));
        System.out.println(proofLink);
        HashMap<String, String> request0 = JaxbHelper.jsonToClass(proofLink, HashMap.class);
        String endonym = prove.verifyDelegationRequest(request.get("link"), request0.get("link"));
        System.out.println(endonym);

        String link = "https://trust.exonym.io/auth/?eJwljEELgjAYQO/+irFzspmG4DFjSUE0SLp00W04YX6LOUMR/3ta1/d4bw4QwkJXxihoFM4Q9owckoSzG7P3RAhXFCJ99un54yBqrvWDn0qXH6uLjPhroDSWeLc9pO2qFraB9v6dEaJGC1MXjiGNsz2lNCJgQ2EsqP5f9FPdmjXwblA/oDfnS5DKrXheguULtjYvwg==";
        prove.authenticationSummaryForULink(link);

    }

    private static void resetContainer(String username) throws Exception {
        Path path = ExonymToolset.pathToContainers(path());
        try{
            new IdContainerJSON(path, USERNAME, true);

        } catch (Exception e){
            IdContainerJSON x = new IdContainerJSON(path, USERNAME, false);
            x.delete();
            new IdContainerJSON(path, USERNAME, true);

        }
    }

    public static final String USERNAME = "mjh";

    public static PassStore pass() throws Exception {
        PassStore store = new PassStore("password", false);
        store.setUsername(USERNAME);
        return store;

    }

    private static Path path() {
        return Path.of("identities");
    }


    private static void sftpSetup() throws UxException {
        try {
            String host = System.getenv("SFTP_HOST");
            int port = Integer.parseInt(System.getenv("SFTP_PORT"));
            String username = System.getenv("SFTP_USERNAME");
            String password = System.getenv("SFTP_PASSWORD");
            String khost0 = System.getenv("KNOWN_HOST0");
            String khost1 = System.getenv("KNOWN_HOST1");
            String khost2 = System.getenv("KNOWN_HOST2");
            SFTPLogonData sftpCredentials = new SFTPLogonData();
            sftpCredentials.setHost(host);
            sftpCredentials.setPort(port);
            sftpCredentials.setKnownHosts(khost0, khost1, khost2);
            sftpCredentials.setUsernameAndPassword(username, password);
            SFTPClient client = new SFTPClient(sftpCredentials);
            client.connect();
            client.overwrite("test.json", "hello_sftp", true);


        } catch (NumberFormatException e) {
            throw new UxException("Set-up SFTP Environment Variables, SFTP_HOST, SFTP_PORT, SFTP_USERNAME, " +
                    "SFTP_PASSWORD, KNOWN_HOST0, KNOWN_HOST1, KNOWN_HOST2", e);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        try {
            logger.info("Probe Initialised");

            sftpSetup();

            networkMapSpawning();
            networkMapInspector();

            openWords();
            openSystemParams();
            containerManagement();
            newContainerWithSecret();

            setupIssuanceTestProve();
            c30SubscriptionLifecycle();

        } catch (Exception e) {
            String a = ExceptionUtils.getStackTrace(e);
            System.out.println(a);
            logger.log(Level.SEVERE, a, e);

        }
    }


}
