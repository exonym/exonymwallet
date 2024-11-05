package io.exonym.lib.wallet;

import io.exonym.lib.abc.util.FileLoader;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.Cache;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.ProbeCallBack;
import io.exonym.lib.lite.Http;
import io.exonym.lib.pojo.AuthenticationWrapper;
import io.exonym.lib.pojo.RevocationRequest;
import io.exonym.lib.pojo.RevocationRequestWrapper;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.standard.PassStore;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class TestTools {

    private final static Logger logger = Logger.getLogger(TestTools.class.getName());
    public static final String PASSWORD = "password";
    public static final Path STORE_PATH = Path.of("non-resources");

    public static final Path TOKENS = Path.of("resources")
            .toAbsolutePath()
            .getParent()
            .getParent()
            .resolve(Path.of("io.exonym.example.sso", "tokens"));

    public static final int SYBIL_PORT = 8079;
    public static final int SYBIL_RB_PORT = 8080;
    public static final int NODE_0_PORT = 8081;
    public static final int NODE_1_PORT = 8082;

    public static final String[] SYBIL_RB_API = {
            "0d7104b8-77ed-4546-b625-04572974573b",
            "8027e77400b7ce7146449b06524040e16447754077c049b907d9715cc48cf417"
    };
    public static final URI RULEBOOK_UID = URI.create("urn:rulebook:trustworthy-leaders:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691");
    public static final URI LEAD_UID = URI.create("urn:rulebook:trustworthy-leaders:exonym:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691");
    public static final URI MOD0_UID = URI.create("urn:rulebook:trustworthy-leaders:exonym:exonym-leads:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691");
    public static final URI MOD1_UID = URI.create("urn:rulebook:trustworthy-leaders:exonym:interpretation:9f87ae0387e1ac0c1c6633a90ad674f9564035624f490fe92aba28c911487691");

    public static final String[] NODE_0_API = {
            "a32c8791-dbea-42e8-b928-4a88ba1de3ea",
            "f2335e185a334d9d620070df592f802369e0fc0bf73e4f4316285dd847d09d51"
    };

    public static final String[] NODE_1_API = {
            "e357838c-a8c2-4c1e-9e49-a641bd8e23b0",
            "e70fea3df7af08d7f5f3a5e815797c7d9951c3d777edeb218ae8d975ae931f07"
    };


    public static ConcurrentHashMap<Integer, ExonymOwner> setupBase(int userCount, String usernamePrefix){
        ConcurrentHashMap<Integer, ExonymOwner> users = new ConcurrentHashMap<>();
        Path path = ExonymToolset.pathToContainers(STORE_PATH);
        for (int i = 0; i < userCount; i++) {
            try {
                ExonymOwner owner = initOwner(path, usernamePrefix + i);
                users.put(i, owner);

            } catch (Exception e) {
                throw new RuntimeException("User already exists", e);

            }
        }
        return users;

    }

    public static ExonymOwner initOwner(Path path, String username) throws Exception {
        PassStore store = new PassStore(PASSWORD, false);
        IdContainerJSON x = new IdContainerJSON(path, username, true);
        ExonymOwner owner = new ExonymOwner(x);
        owner.openContainer(store);
        owner.setupContainerSecret(store.getEncrypt(), store.getDecipher());
        return owner;

    }

    public static void addSybilPersonCredential(int userCount, String usernamePrefix) {
        for (int i = 0; i < userCount; i++) {
            try {
                PassStore store = new PassStore(PASSWORD, false);
                store.setUsername(usernamePrefix + i);
                String result = augmentOwnerWithSybil(store, Rulebook.SYBIL_CLASS_PERSON);
                logger.info(result);

            } catch (Exception e) {
                throw new RuntimeException("Unexpected error", e);

            }
        }
    }

    public static String augmentOwnerWithSybil(PassStore store, String RULEBOOK__SYBIL_CLASS) throws Exception {
        return SybilOnboarding.testNet(store, STORE_PATH,
                SybilOnboarding.SYBIL_URL_TEST_NET,
                RULEBOOK__SYBIL_CLASS);

    }

    public static void addRulebookCredential(int userCount, String usernamePrefix, URI targetMod) {
        for (int i = 0; i < userCount; i++) {
            try {
                PassStore store = new PassStore(PASSWORD, false);
                store.setUsername(usernamePrefix + i);
                String result = augmentOwnerWithRulebook(store, targetMod);
                logger.info(result);

            } catch (Exception e) {
                throw new RuntimeException("Unexpected error", e);

            }
        }
    }

    public static String augmentOwnerWithRulebook(PassStore store, URI targetMod) throws Exception {
        return RulebookOnboarding.onboardRulebook(store, STORE_PATH, targetMod);

    }

    public static String revocationRequest(String filename, String[] api, URI ruleUid) throws IOException, UxException {
        Map<String, ByteArrayOutputStream> files = FileLoader.loadFilesAsMap(TestTools.TOKENS);
        ArrayList<RevocationRequest> revokeTokens = new ArrayList<>();

        for (String file : files.keySet()){
            if (file.contains(filename)){
                RevocationRequest request = new RevocationRequest();
                request.setEndonymToken(
                        Base64.encodeBase64String(files.get(file).toByteArray()));
                request.addRuleUri(ruleUid);
                revokeTokens.add(request);

            }
        }
        if (revokeTokens.isEmpty()){
            throw new UxException("Not found " + filename);

        }
        RevocationRequestWrapper wrapper = new RevocationRequestWrapper();
        wrapper.setKid(api[0]);
        wrapper.setKey(api[1]);
        wrapper.setRequests(revokeTokens);
        return JaxbHelper.gson.toJson(wrapper);

    }

    public static URI getValidRulebookRuleUid(int index, URI rulebookUid) throws Exception {
        Cache cache = new Cache(TestTools.STORE_PATH);
        Rulebook rulebook = cache.open(rulebookUid);
        String rn = rulebook.getRules().get(index).getId();
        return URI.create(rn);

    }


    public static String handleError(Exception e) {
        logger.info(e.toString());

        String info = "";
        if (e instanceof UxException){
            UxException ux = (UxException)e;
            info += ": " + JaxbHelper.gson.toJson(ux.getInfo(), ArrayList.class);

        }
        String a = ExceptionUtils.getStackTrace(e);
        logger.severe(a);
        return a + info;

    }


    public static String authenticateWithSso(Http http,
                                             String node1,
                                             URI authRequest,
                                             URI postRequest,
                                             boolean signOut) throws Exception {
        logger.info(authRequest.toString());
        logger.info(postRequest.toString());
        if (signOut){
            http.newContext();

        }
        String response = http.basicGet(authRequest.toString());
        AuthenticationWrapper w = JaxbHelper.gson.fromJson(response, AuthenticationWrapper.class);
        String link = w.getLink();
        logger.info("Link from GET=" + link);

        ProbeCallBack callback = new ProbeCallBack(http, postRequest.toURL());

        PassStore store = new PassStore(TestTools.PASSWORD, false);
        store.setUsername(node1 + 0);

        Prove prove = new Prove(store, TestTools.STORE_PATH);
        prove.proofForRulebookSSO(link);

        return callback.getResult();

    }

    public static String stripEndonym(String endonym){
        String[] parts = endonym.split("-");
        String search = parts[parts.length-1];
        return search;

    }

    public static ArrayList<RevocationRequest> prepareRevocationRequests(ArrayList<String> endonymTailsAfterHyphen, URI rule, String evDesc) throws IOException {
        ArrayList<RevocationRequest> revokeTokens = new ArrayList<>();
        Map<String, ByteArrayOutputStream> files = FileLoader.loadFilesAsMap(TOKENS);

        for (String file : files.keySet()){
            String[] parts = file.split("-");
            String end = parts[parts.length-1];
            if (endonymTailsAfterHyphen.contains(end)){
                RevocationRequest request = new RevocationRequest();
                request.setEndonymToken(
                        Base64.encodeBase64String(files.get(file).toByteArray()));
                request.addRuleUri(rule);
                request.setDescriptionOfEvidence(evDesc);
                revokeTokens.add(request);

            }
        }
        return revokeTokens;

    }

    public static void expectGetRejection(Http client, String containerReg) {
        try {
            client.basicGet(containerReg);
            logger.info(containerReg);
            assert false;

        } catch (IOException e) {
            assert true;

        }
    }

    public static Header[] generateHeaders(String[] node0Api) {
        Header kid = new BasicHeader("kid", node0Api[0]);
        Header key = new BasicHeader("key", node0Api[1]);
        return new Header[] {kid, key};

    }
}
