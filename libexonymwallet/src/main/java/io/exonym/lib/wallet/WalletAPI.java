package io.exonym.lib.wallet;

import eu.abc4trust.xml.SystemParameters;
import io.exonym.lib.api.*;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.actor.IdContainerExternal;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.lite.FulfillmentReport;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.pojo.IdContainer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.UnmanagedMemory;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.WordFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

public class WalletAPI {

    private static Logger logger = Logger.getLogger(WalletAPI.class.getName());
    public static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";

    @CEntryPoint(name = "c30_join_to_auth_protocol")
    public static CCharPointer c30JoinToAuthProtocol(IsolateThread thread,
                                               CCharPointer alpha_,
                                               CCharPointer beta_,
                                               CCharPointer gamma_,
                                               CCharPointer path_,
                                               boolean test){
        try {
            String alpha = CTypeConversion.toJavaString(alpha_);
            String beta = CTypeConversion.toJavaString(beta_);
            String gamma = CTypeConversion.toJavaString(gamma_);
            String path = CTypeConversion.toJavaString(path_);
            String result = C30Utils.joinToAuthProtocol(alpha, beta, gamma, path, test);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "has_player_key_for_game")
    public static boolean hasPlayerKeyForGame(IsolateThread thread, CCharPointer alpha_, CCharPointer beta_, CCharPointer rootPath_){
        try {
            String alpha = CTypeConversion.toJavaString(alpha_);
            String beta = CTypeConversion.toJavaString(beta_);
            String path = CTypeConversion.toJavaString(rootPath_);
            return C30Utils.hasPlayerKeyForGame(alpha, beta, path);

        } catch (Exception e) {
            handleError(e);
            return false;

        }
    }

    @CEntryPoint(name = "generate_player_key_for_game")
    public static CCharPointer generatePlayerKeyForGame(IsolateThread thread, CCharPointer alpha_, CCharPointer beta_, CCharPointer rootPath_){
        try {
            String alpha = CTypeConversion.toJavaString(alpha_);
            String beta = CTypeConversion.toJavaString(beta_);
            String path = CTypeConversion.toJavaString(rootPath_);
            String publicKeyAsHex = C30Utils.getPlayerPublicKeyAsString(path, alpha, beta);
            return toCString(publicKeyAsHex);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "open_system_params")
    public static CCharPointer openSystemParams(IsolateThread thread){
        try {
            SystemParameters params = IdContainerExternal.openSystemParameters();
            String xml =  IdContainer.convertObjectToXml(params);
            return toCString(xml);

        } catch (Exception e) {
            String exception = ExceptionUtils.getStackTrace(e);
            return toCString(exception);

        }
    }

    //
    // Rulebook composition features
    //
    @CEntryPoint(name = "new_rulebook")
    public static CCharPointer newRulebook(IsolateThread thread, CCharPointer name_, CCharPointer path_){
        try {
            String name = CTypeConversion.toJavaString(name_);
            String path = CTypeConversion.toJavaString(path_);
            new RulebookCreator(name, path);
            return toCString(name + "-rulebook.json created.");

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "add_new_rule")
    public static CCharPointer addNewRuleToRulebook(IsolateThread thread, CCharPointer rule_, CCharPointer path_){
        try {
            String rule = CTypeConversion.toJavaString(rule_);
            String path = CTypeConversion.toJavaString(path_);
            RulebookVerifier verifier = new RulebookVerifier(path);
            RulebookCreator creator = new RulebookCreator(verifier.getRulebook());
            creator.addRule(rule);
            creator.writeFileToOut(Path.of(path));
            return toCString("rulebook updated @ " + path);

        } catch (Exception e) {
            return handleError(e);

        }
    }


    private static CCharPointer handleError(Exception e) {
        String info = "";
        if (e instanceof UxException){
            UxException ux = (UxException)e;
            info += ": " + JaxbHelper.gson.toJson(ux.getInfo(), ArrayList.class);

        }
        String a = ExceptionUtils.getStackTrace(e);
        logger.severe(a);
        return toCString(a + info);

    }

    @CEntryPoint(name = "extend_rulebook")
    public static CCharPointer extendRulebook(IsolateThread thread, CCharPointer inputFileLocation_,
                                              CCharPointer outputFileLocation_){
        String inputFileLocation = CTypeConversion.toJavaString(inputFileLocation_);
        String outputFileLocation = CTypeConversion.toJavaString(outputFileLocation_);

        return toCString("NOT_IMPLEMENTED");

    }

    //
    // Analysis features
    //
    @CEntryPoint(name = "wallet_report")
    public static CCharPointer walletReport(IsolateThread thread,
                                               CCharPointer username_,
                                               CCharPointer passwordAsSha256Hex_,
                                               CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            Prove prove = new Prove(passStore, Path.of(path));
            String result = prove.walletReport();
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "authentication_report")
    public static CCharPointer authSummaryForUniversalLink(IsolateThread thread,
                                           CCharPointer username_,
                                           CCharPointer passwordAsSha256Hex_,
                                           CCharPointer request_,
                                           CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String request = CTypeConversion.toJavaString(request_);
            Prove prove = new Prove(passStore, Path.of(path));
            FulfillmentReport result = prove.authenticationSummaryForULink(request);
            return toCString(JaxbHelper.gson.toJson(result, FulfillmentReport.class));

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "lead_list_test")
    public static CCharPointer leadListTest(IsolateThread thread,
                                            CCharPointer url_){
        try {
            String url = CTypeConversion.toJavaString(url_);
            String result = AddToTrustworthyLeadsList.addToLeadsTest(url);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }



    @CEntryPoint(name = "spawn_network_map")
    public static CCharPointer spawnNetworkMap(IsolateThread thread,
                                         CCharPointer path_){
        try {
            String path = CTypeConversion.toJavaString(path_);
            Path p = Path.of(path);
            NetworkMapInspector inspector = new NetworkMapInspector(networkMap(path));
            String result = inspector.spawn();
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }


    @CEntryPoint(name = "view_actor")
    public static CCharPointer viewActor(IsolateThread thread,
                                           CCharPointer uid_,
                                           CCharPointer path_){
        try {
            String path = CTypeConversion.toJavaString(path_);
            Path p = Path.of(path);
            String uid = CTypeConversion.toJavaString(uid_);
            NetworkMapInspector inspector = new NetworkMapInspector(networkMap(path));
            String result = inspector.viewActor(uid);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "list_actors")
    public static CCharPointer listActor(IsolateThread thread,
                                         CCharPointer uid_,
                                         CCharPointer path_){
        try {
            String path = CTypeConversion.toJavaString(path_);
            String uid = CTypeConversion.toJavaString(uid_);
            NetworkMapInspector inspector = new NetworkMapInspector(networkMap(path));
            String result = inspector.listActors(uid);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    private static AbstractNetworkMap networkMap(String path) throws Exception {
        return new NetworkMap(Path.of(path, "network-map"));
    }

    @CEntryPoint(name = "list_rulebooks")
    public static CCharPointer listRulebooks(IsolateThread thread,
                                         CCharPointer path_){
        try {
            String path = CTypeConversion.toJavaString(path_);
            Path p = Path.of(path);
            NetworkMapInspector inspector = new NetworkMapInspector(networkMap(path));
            String result = inspector.listActors(null);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }



    //
    // SSO features
    //
    @CEntryPoint(name = "proof_for_rulebook_sso")
    public static CCharPointer proofForRulebookSso(IsolateThread thread,
                                                   CCharPointer username_,
                                                   CCharPointer passwordAsSha256Hex_,
                                                   CCharPointer ulinkChallenge_,
                                                   CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String ulinkChallenge = CTypeConversion.toJavaString(ulinkChallenge_);
            Prove prove = new Prove(passStore, Path.of(path));
            String result = prove.proofForRulebookSSO(ulinkChallenge);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }
    @CEntryPoint(name = "proof_for_rulebook_sso_anon")
    public static CCharPointer proofForRulebookSsoAnon(IsolateThread thread,
                                                   CCharPointer username_,
                                                   CCharPointer passwordAsSha256Hex_,
                                                   CCharPointer ulinkChallenge_,
                                                   CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String ulinkChallenge = CTypeConversion.toJavaString(ulinkChallenge_);
            Prove prove = new Prove(passStore, Path.of(path));
            String result = prove.proofForRulebookSSOAnon(ulinkChallenge);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "generate_delegation_request_for_third_party")
    public static CCharPointer generateDelegationRequestForThirdParty(IsolateThread thread,
                                                   CCharPointer username_,
                                                   CCharPointer passwordAsSha256Hex_,
                                                   CCharPointer ulinkChallenge_,
                                                   CCharPointer name_,
                                                   CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String ulinkChallenge = CTypeConversion.toJavaString(ulinkChallenge_);
            String name = CTypeConversion.toJavaString(name_);
            Prove prove = new Prove(passStore, Path.of(path));
            String result = prove.generateDelegationRequestForThirdParty(ulinkChallenge, name);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }


    @CEntryPoint(name = "fill_delegation_request")
    public static CCharPointer fillDelegationRequest(IsolateThread thread,
                                                   CCharPointer username_,
                                                   CCharPointer passwordAsSha256Hex_,
                                                   CCharPointer ulink_,
                                                   CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String ulink = CTypeConversion.toJavaString(ulink_);
            Prove prove = new Prove(passStore, Path.of(path));
            String result = prove.fillDelegationRequest(ulink);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }


    //verifyDelegationRequest(String requestLink, String proofLink)
    @CEntryPoint(name = "verify_delegation_request")
    public static CCharPointer verifyDelegationRequest(IsolateThread thread,
                                                   CCharPointer username_,
                                                   CCharPointer passwordAsSha256Hex_,
                                                   CCharPointer requestLink_,
                                                   CCharPointer proofLink_,
                                                   CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String requestLink = CTypeConversion.toJavaString(requestLink_);
            String proofLink = CTypeConversion.toJavaString(proofLink_);
            Prove prove = new Prove(passStore, Path.of(path));
            String result = prove.verifyDelegationRequest(requestLink, proofLink);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }


    //
    // Noninteractive
    //
    @CEntryPoint(name = "non_interactive_proof")
    public static CCharPointer nonInteractiveProofRequest(IsolateThread thread,
                                                          CCharPointer username_,
                                                          CCharPointer passwordAsSha256Hex_,
                                                          CCharPointer nonInteractiveProofRequest_,
                                                          CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String nonInteractiveProofRequest = CTypeConversion.toJavaString(nonInteractiveProofRequest_);
            Prove prove = new Prove(passStore, Path.of(path));
            String result = prove.nonInteractiveProofRequest(nonInteractiveProofRequest);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "sftp_put")
    public static CCharPointer sftpPut(IsolateThread thread,
                                        CCharPointer username_,
                                        CCharPointer passwordAsSha256Hex_,
                                        CCharPointer sftpCredentialUID_,
                                        CCharPointer fileName_,
                                        CCharPointer token_,
                                        CCharPointer remotePath_,
                                        CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String filename = CTypeConversion.toJavaString(fileName_);
            String token = CTypeConversion.toJavaString(token_);
            String remotePath = CTypeConversion.toJavaString(remotePath_);
            String sftpCredentialUID = CTypeConversion.toJavaString(sftpCredentialUID_);
            SFTPManager credentialManager = new SFTPManager(passStore, Path.of(path));
            String result = credentialManager.put(filename, token, sftpCredentialUID, remotePath);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "sftp_template")
    public static CCharPointer sftpTemplate(IsolateThread thread,
                                            CCharPointer path_){
        try {
            String path = CTypeConversion.toJavaString(path_);
            String result = SFTPManager.createTemplate(Path.of(path));
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }


    @CEntryPoint(name = "sftp_add")
    public static CCharPointer addSftpTemplate(IsolateThread thread,
                                            CCharPointer username_,
                                            CCharPointer passwordAsSha256Hex_,
                                            CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            SFTPManager sftp = new SFTPManager(passStore, Path.of(path));
            String result = sftp.add();
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "sftp_remove")
    public static CCharPointer sftpRemove(IsolateThread thread,
                                       CCharPointer username_,
                                       CCharPointer passwordAsSha256Hex_,
                                       CCharPointer name_,
                                       CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String name = CTypeConversion.toJavaString(name_);
            SFTPManager sftp = new SFTPManager(passStore, Path.of(path));
            String result = sftp.remove(name);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }


    //
    // Onboarding features
    //
    @CEntryPoint(name = "onboard_sybil_testnet")
    public static CCharPointer onboardSybilTestnet(IsolateThread thread, CCharPointer username_,
                                                   CCharPointer sybilClass_, CCharPointer passwordAsSha256Hex_,
                                                   CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String sybilClass = CTypeConversion.toJavaString(sybilClass_);
            String result = SybilOnboarding.testNet(passStore,
                    Path.of(path),
                    SybilOnboarding.SYBIL_URL_TEST_NET,
                    sybilClass);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "onboard_rulebook")
    public static CCharPointer onboardRulebook(IsolateThread thread,
                                               CCharPointer username_,
                                               CCharPointer issuancePolicy_,
                                               CCharPointer passwordAsSha256Hex_,
                                               CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String issuancePolicy = CTypeConversion.toJavaString(issuancePolicy_);
            String result = RulebookOnboarding.onboardRulebook(passStore, Path.of(path), issuancePolicy);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "onboard_rulebook_moderator_uid")
    public static CCharPointer onboardRulebookModeratorUID(IsolateThread thread,
                                                           CCharPointer username_,
                                                           CCharPointer passwordAsSha256Hex_,
                                                           CCharPointer moderatorUid_,
                                                           CCharPointer path_){
        try {
            PassStore passStore = openPassStore(username_, passwordAsSha256Hex_);
            String path = CTypeConversion.toJavaString(path_);
            String moderatorUid = CTypeConversion.toJavaString(moderatorUid_);
            String result = RulebookOnboarding.onboardRulebook(passStore, Path.of(path), URI.create(moderatorUid));
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }


    @CEntryPoint(name = "authenticate")
    public static CCharPointer authenticate(IsolateThread thread, CCharPointer username_,
                                            CCharPointer passwordAsSha256Hex_, CCharPointer path_){
        try {
            PassStore store = openPassStore(username_, passwordAsSha256Hex_);
            Path path = Path.of(CTypeConversion.toJavaString(path_));
            IdContainerJSON x = new IdContainerJSON(ExonymToolset.pathToContainers(path),
                    store.getUsername());

            ExonymOwner o = new ExonymOwner(x);
            o.authenticate(store);
            return toCString("OPENED");

        } catch (Exception e) {
            return handleError(e);

        }
    }

    @CEntryPoint(name = "setup_wallet_path")
    public static CCharPointer setupWallet(IsolateThread thread, CCharPointer username_,
                                           CCharPointer plainTextPassword_, CCharPointer path_){
        try {
            String username = CTypeConversion.toJavaString(username_);
            String plainTextPassword = CTypeConversion.toJavaString(plainTextPassword_);
            PassStore passStore = new PassStore(plainTextPassword, true);
            byte[] a = ExonymOwner.toUnsignedByteArray(PassStore.initNew(plainTextPassword));
            RecoveryPhrase phrase = new RecoveryPhrase(a);
            String[] wordVector = phrase.getWordVector();
            Path path = Path.of(CTypeConversion.toJavaString(path_));
            IdContainerJSON x = new IdContainerJSON(ExonymToolset.pathToContainers(path), username, true);
            ExonymOwner owner = new ExonymOwner(x);
            owner.openContainer(passStore);
            owner.setupContainerSecret(passStore.getEncrypt(), passStore.getDecipher());
            String result = JaxbHelper.serializeToJson(wordVector, String[].class);
            return toCString(result);

        } catch (Exception e) {
            return handleError(e);

        }
    }



    @CEntryPoint(name = "delete_wallet")
    public static int deleteWallet(IsolateThread thread, CCharPointer username_,
                                   CCharPointer plainTextPassword_,CCharPointer path_){
        try {
            String username = CTypeConversion.toJavaString(username_);
            String plainTextPassword = CTypeConversion.toJavaString(plainTextPassword_);
            PassStore store = new PassStore(plainTextPassword, false);
            store.setUsername(username);
            Path path = Path.of(CTypeConversion.toJavaString(path_));
            IdContainerJSON x = new IdContainerJSON(ExonymToolset.pathToContainers(path), store.getUsername());
            ExonymOwner o = new ExonymOwner(x);
            o.authenticate(store);
            x.delete();
            return 1;

        } catch (Exception e) {
            handleError(e);
            return 0;

        }
    }

    @CEntryPoint(name = "generate_reset_proof")
    public static CCharPointer generateResetProof(IsolateThread thread, CCharPointer username_,
                                                  CCharPointer plainTextPassword_, CCharPointer path_){
        String username = CTypeConversion.toJavaString(username_);
        String plainTextPassword = CTypeConversion.toJavaString(plainTextPassword_);
        return toCString(NOT_IMPLEMENTED);

    }


    private static PassStore openPassStore(CCharPointer username_, CCharPointer passwordAsSha256Hex_) throws Exception {
        String username = CTypeConversion.toJavaString(username_);
        String password = CTypeConversion.toJavaString(passwordAsSha256Hex_);
        byte[] pwd = ExonymOwner.toUsablePassStoreInitByteArray(password);
        PassStore passStore = new PassStore(pwd);
        passStore.setUsername(username);
        return passStore;

    }

    @CEntryPoint(name = "sha_256_as_hex")
    public static CCharPointer sha256AsHex(IsolateThread thread, CCharPointer toHash_){
        String toHash = CTypeConversion.toJavaString(toHash_);
        return toCString(CryptoUtils.computeSha256HashAsHex(toHash));

    }

    public static int restoreWalletFromBackup(byte[] walletData, String username){
        return 0;

    }

    public static CCharPointer toCString(String javaString) {
        if (javaString == null) {
            return WordFactory.nullPointer();

        }
        byte[] bytes = javaString.getBytes(StandardCharsets.UTF_8);
        CCharPointer cString = UnmanagedMemory.calloc(bytes.length + 1);

        for (int i = 0; i < bytes.length; i++) {
            cString.write(i, bytes[i]);
        }
        cString.write(bytes.length, (byte) 0);

        return cString;
    }


    @CEntryPoint(name = "free_cstring")
    public static void freeCString(IsolateThread thread, CCharPointer cString) {
        if (cString.isNonNull()) {
            UnmanagedMemory.free(cString);
        }
    }

//    private static CCharPointer toCString(String string){
//        return CTypeConversion.toCString(string).get();
//    }

    @CEntryPoint(name = "hello_exonym")
    public static int helloWallet(IsolateThread thread){
        return 12;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("2023 Exonym Wallet");


    }
}
