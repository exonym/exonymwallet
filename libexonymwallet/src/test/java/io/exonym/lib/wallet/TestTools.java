package io.exonym.lib.wallet;

import com.google.gson.JsonObject;
import io.exonym.lib.abc.util.FileLoader;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.Cache;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.C30Game;
import io.exonym.lib.helpers.C30Player;
import io.exonym.lib.helpers.ProbeCallBack;
import io.exonym.lib.lite.Http;
import io.exonym.lib.pojo.*;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.PassStore;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
    };

    public static final URI RULEBOOK_UID = URI.create("");
    public static final URI LEAD_UID = URI.create("urn:rulebook:mmo:c30:4ccbdf03787d137fc360a193ba950eb77d6b150f99b69280a11dc084f29a2f72");
    public static final URI MOD0_UID = URI.create("urn:rulebook:mmo:c30:home:4ccbdf03787d137fc360a193ba950eb77d6b150f99b69280a11dc084f29a2f72");
    public static final URI MOD1_UID = URI.create("urn:rulebook:mmo:c30:jank:4ccbdf03787d137fc360a193ba950eb77d6b150f99b69280a11dc084f29a2f72");

    public static C30Game game0;
    public static  C30Game game1;


    // TODO
    public static final String[] NODE_0_API = {
            "b9c82418-ebe7-45c3-b19b-ca6f7f318867",
            "0fc8bc7cf26084f7341cb007c5233118ea2aecdf95a4ccc25212d5e8f538966b"
    };


    public static final String[] NODE_1_API = {
            "eefcddbc-b654-42df-bdee-230897f2067e",
            "5126a4706846f26887c949950c022b9c07d955d67e0a3c08cb6dd942a8c45354"
    };

    static {
        game0 = new C30Game();
        game0.setGamma("ff1297feb9c113ff1297fe4bc11312d7");
        game0.setApiKey("1a0365868d33ae0e4ccac2d25610813d337d18973f0dd5119929c8966546b527");

        game1 = new C30Game();
        game1.setGamma("d8991e2b363b4946b53e9685ca2b626a");
        game1.setApiKey("61f7a573ea911c949ec8e1ce038416c704c44f76eaad202f8dd2b37a6e197c2d");

    }


    public static void registerPlayerOnC30Server(C30Player playerNew, C30Game game, String keyAsB64){
        try {
            Http client = new Http();
            JsonObject gameToPost = new JsonObject();
            gameToPost.addProperty("key", keyAsB64);

            String playerPath = playerNew.getAlpha() + "/" +
                    playerNew.getBeta() + "/" +
                    game.getGamma();

            logger.info(playerPath);

            BasicHeader game0Header = new BasicHeader(TestC30.HEADER_NAME_C30_SYBIL_API_KEY,
                    game.getApiKey());

            String response = client.basicPost(TestC30.END_POINT_CONTAINER + playerPath,
                    gameToPost.toString(), game0Header);

            logger.info(response);

        } catch (Exception e) {
            TestTools.handleError(e);

        }
    }

    public static void registerPlayerOnGameServers(C30Player player){
        try {
            Http client = new Http();
            Header[] headersGame0 = TestTools.generateHeaders(TestTools.NODE_0_API);
            Header[] headersGame1 = TestTools.generateHeaders(TestTools.NODE_1_API);

            NetworkMap nm = new NetworkMap(TestTools.STORE_PATH.resolve("network-map"));
            NetworkMapItemModerator nmim0 = (NetworkMapItemModerator) nm.nmiForNode(TestTools.MOD0_UID);
            NetworkMapItemModerator nmim1 = (NetworkMapItemModerator) nm.nmiForNode(TestTools.MOD1_UID);

            URI home0 = nmim0.getRulebookNodeURL();
            URI home1 = nmim1.getRulebookNodeURL();

            StringBuilder url = new StringBuilder();
            url.append("verify/");
            url.append(player.getEpsilon());
            url.append("/");
            url.append(C30Utils.getPlayerPublicKeyAsString(
                    TestTools.STORE_PATH, player.getAlpha(), player.getBeta()));

            logger.info(url.toString());

            String url0 = home0 + url.toString();
            String url1 = home1 + url.toString();

            String r0 = client.basicGet(url0.toString(), headersGame0);
            logger.info(r0);
            String r1 = client.basicGet(url1.toString(), headersGame1);
            logger.info(r1);

            logger.info("Epsilon="
                    + player.getEpsilon() + " "
                    + CryptoUtils.computeMd5HashAsHex(
                    player.getEpsilon().getBytes(StandardCharsets.UTF_8)));


        } catch (Exception e) {
            TestTools.handleError(e);
            assert false;

        }


    }

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
        // todo headers need inserting (kid key)
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
