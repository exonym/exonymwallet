package io.exonym.lib.api;

import com.ibm.zurich.idmx.exception.SerializationException;
import eu.abc4trust.xml.*;
import io.exonym.lib.exceptions.AlreadyAuthException;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.Parser;
import io.exonym.lib.helpers.Timing;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.lite.ModelCommandProcessor;
import io.exonym.lib.lite.Msg;
import io.exonym.lib.pojo.*;
import io.exonym.lib.wallet.ExonymOwner;
import io.exonym.lib.wallet.WalletUtils;
import org.apache.commons.codec.binary.Base64;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ExonymAuthenticate extends ModelCommandProcessor {


    // todo parallel processing
    private final static Logger logger = Logger.getLogger(ExonymAuthenticate.class.getName());
    
    private ConcurrentHashMap<String, ExonymChallenge> challengeToAuthenticationRequest = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, URI> challengeToDomainContext = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> challengeToSessionId = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> challengeToT0 = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> sessionIdToChallenge = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, URI> sessionIdToEndonym = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<URI, URI>> authSessionIdToEndonym = new ConcurrentHashMap<>();

    protected void challenge(SsoChallenge challenge, String sessionId){
        String c = challenge.getChallenge();
        logger.info("Setup challenge " + c);
        challengeToAuthenticationRequest.put(c, challenge);
        sessionIdToChallenge.put(sessionId, c);
        challengeToDomainContext.put(challenge.getChallenge(), challenge.getDomain());
        logger.info("Putting challenge and domain=" + challengeToDomainContext);


    }

    protected URI probeForContext(String sessionId) throws UxException {
        String challenge = sessionIdToChallenge.remove(sessionId);
        if (challenge!=null){
            challengeToSessionId.put(challenge, sessionId);
            challengeToT0.put(challenge, Timing.currentTime());
            URI domain = challengeToDomainContext.get(challenge);
            logger.info("(getting) challengeToDomainContext=" + challengeToDomainContext);
            return domain;

        } else {
            throw new UxException(ErrorMessages.TIME_OUT);
        }
    }

    protected void authenticate(String token) throws UxException, HubException {
        long t0 = Timing.currentTime();
        try {
            if (token != null) {
                String challenge = authenticateToken(token);
                String sessionId = challengeToSessionId.remove(challenge);
                synchronized (sessionId) {
                    addAuthorizedSession(sessionId, challenge);
                    sessionId.notifyAll();
                    logger.info("Authentication Duration = " + Timing.hasBeenMs(t0));
                }
            } else {
                throw new NullPointerException("No Token Provided");

            }
        } catch (HubException e){
            throw e;

        } catch (UxException e){
            throw e;

        } catch (SerializationException e) {
            throw new HubException(ErrorMessages.TOKEN_INVALID + ":Serialization", e);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    private void addAuthorizedSession(String sessionId, String challenge) {
        logger.info("(z) challengeToDomainContext=" + challengeToDomainContext);
        ConcurrentHashMap<URI, URI> contexts = authSessionIdToEndonym.get(sessionId);
        URI context = challengeToDomainContext.get(challenge);

        if (contexts==null){
            contexts = new ConcurrentHashMap<>();
            authSessionIdToEndonym.put(sessionId, contexts);
            logger.info("created map for session " + contexts);


        }
        URI nym = sessionIdToEndonym.remove(sessionId);
        contexts.put(context, nym);

        logger.info("context= " + context + " nym=" + nym + " onto=" + contexts);
        logger.info("authSessionIdToEndonym= " + authSessionIdToEndonym);

    }

    /**
     * Check for authorized sessions.
     *
     * @param sessionId
     * @return The endonyms associated with the session.
     * @throws UxException If the session is not authorized.
     *
     */
    protected URI isAuthenticated(String sessionId, URI context) throws UxException {
        ConcurrentHashMap<URI, URI> contextToEndonym = authSessionIdToEndonym.get(sessionId);
        if (contextToEndonym!=null) {
            logger.info("contextToEndonym " + contextToEndonym + " context=" + context);
            URI endonym = contextToEndonym.get(context);
            if (endonym != null) {
                return endonym;
            }
        } else {
            logger.info("No contextToEndonym Map " + contextToEndonym);

        }
        throw new UxException(ErrorMessages.FAILED_TO_AUTHORIZE);
    }

    protected boolean isAuthenticatedQuiet(String sessionId, URI context)  {
        try{
            isAuthenticated(sessionId, context);
            return true;

        } catch (Exception e){
            logger.throwing("ExonymAuthenticate.class", "isAuthenticatedQuiet()", e);
            return false;
        }
    }

    protected SsoChallenge authIfNeeded(SsoConfiguration config, String sessionId) throws AlreadyAuthException {
        if (!isAuthenticatedQuiet(sessionId, config.getDomain())){
            return SsoChallenge.newChallenge(config);

        } else {
            throw new AlreadyAuthException();

        }
    }


    private String authenticateToken(String token) throws Exception {
        PresentationToken pt = Parser.parsePresentationTokenFromXml(token);
        PresentationTokenDescription ptd = pt.getPresentationTokenDescription();
        String challenge = Base64.encodeBase64String(ptd.getMessage().getNonce());

        String sessionId = challengeToSessionId.get(challenge);
        ExonymChallenge c = challengeToAuthenticationRequest.remove(challenge);

        logger.info("challenge/sessionId at authenticate (no value should be null)="
                + challenge + " " + sessionId + " " + c);

        PresentationPolicyAlternatives ppa = verifyOfferingAndBuildPolicy(c, sessionId, ptd);
        ExonymOwner owner = ExonymOwner.verifierOnly();
        owner.verifyClaim(ppa, pt);
        return challenge;

    }

    private PresentationPolicyAlternatives verifyOfferingAndBuildPolicy(ExonymChallenge c,
                                     String sessionId, PresentationTokenDescription ptd) throws Exception {
        if (c instanceof SsoChallenge){
            return juxtaposeTokenAndSsoChallenge((SsoChallenge)c, sessionId, ptd);

        } else if (c instanceof DelegateRequest) {
            return juxtaposeTokenAndDelegateChallenge((DelegateRequest) c, sessionId, ptd);

        } else if (c==null){
            throw new UxException(ErrorMessages.UNEXPECTED_TOKEN_FOR_THIS_NODE);

        } else {
            throw new HubException("Unsupported Challenge Type " + c);

        }
    }

    private PresentationPolicyAlternatives juxtaposeTokenAndSsoChallenge(SsoChallenge c,
                          String sessionId, PresentationTokenDescription ptd) throws Exception {
        PresentationPolicy policy = checkPseudonym(sessionId, c, ptd);

        if (c.isSybil() || !c.getHonestUnder().isEmpty()){
            HashMap<String, CredentialInToken> rulebookIdToCredentialMap = checkSybil(policy, ptd);

            if (!c.getHonestUnder().isEmpty()){
                checkRulebooks(c, ptd, rulebookIdToCredentialMap);

            }
        }
        PresentationPolicyAlternatives ppa = new PresentationPolicyAlternatives();
        ppa.getPresentationPolicy().add(policy);
        return ppa;
    }

    private void checkRulebooks(SsoChallenge c, PresentationTokenDescription ptd,
                                HashMap<String, CredentialInToken> rcMap) throws Exception {

        HashMap<String, RulebookAuth> requests = c.getHonestUnder();
        for (String rulebook : requests.keySet()){
            RulebookAuth auth = requests.get(rulebook);
            CredentialInToken cit = rcMap.get(rulebook);

            if (auth.getAdvocateBlacklist()
                    .contains(cit.getIssuerParametersUID())){
                throw new UxException(ErrorMessages.BLACKLISTED_ADVOCATE);
            }
            URI sourceUID = UIDHelper.computeSourceUidFromNodeUid(
                    cit.getIssuerParametersUID());
            if (auth.getSourceBlacklist().contains(sourceUID)){
                throw new UxException(ErrorMessages.BLACKLISTED_SOURCE);

            }
            auth.getSourceBlacklist();

        }
    }

    private PresentationPolicy checkPseudonym(String sessionId, ExonymChallenge c,
                     PresentationTokenDescription ptd) throws HubException, UxException {
        String domain = c.getDomain().toString();
        List<PseudonymInToken> nyms = ptd.getPseudonym();
        PresentationPolicy pp = new PresentationPolicy();
        pp.setMessage(ptd.getMessage());
        pp.setPolicyUID(ptd.getPolicyUID());
        boolean hasBasis = false;
        boolean hasExclusive = false;

        for (PseudonymInToken nym : nyms){
            if (nym.getScope().equals(domain)){
                if (nym.isExclusive() && !hasExclusive){
                    pp.getPseudonym().add(Parser.nymInTokenToPolicy(nym));

                    URI endonym = WalletUtils.endonymForm(nym.getScope(), nym.getPseudonymValue());

                    logger.info("sessionIdToEndonym=" + sessionIdToEndonym);

                    this.sessionIdToEndonym.put(sessionId, endonym);
                    hasExclusive = true;
                }
            } else {
                if (!nym.isExclusive()){
                    pp.getPseudonym().add(Parser.nymInTokenToPolicy(nym));
                    hasBasis = true;

                }
            }
        }
        if (hasExclusive && hasBasis){
            return pp;

        } else {
            throw new HubException(ErrorMessages.UNEXPECTED_PSEUDONYM_REQUEST,
                    "The correct pseudonym was not provided",
                    "hasBasis=" + hasBasis,
                    "hasExclusive=" + hasExclusive);

        }
    }

    private HashMap<String, CredentialInToken> checkSybil(PresentationPolicy buildingPolicy,
                                                          PresentationTokenDescription ptd) throws Exception {
        String sybilUID = Rulebook.SYBIL_TEST_NET_UID.toString();
        boolean foundSybil = false;
        HashMap<String, CredentialInToken> map = new HashMap<>();
        List<CredentialInPolicy> credentials = new ArrayList<>();
        for (CredentialInToken cit : ptd.getCredential()){
            String rid = UIDHelper.computeRulebookIdFromAdvocateUid(
                    cit.getIssuerParametersUID());
            map.put(rid, cit);
            credentials.add(Parser.credentialInTokenToPolicy(cit));
            if (cit.getIssuerParametersUID().toString().startsWith(sybilUID)){
                foundSybil = true;

            }
        }
        buildingPolicy.getCredential().addAll(credentials);

        if (foundSybil){
            return map;

        } else {
            throw new HubException(ErrorMessages.SYBIL_WARN,
                    "A sybil credential was requested and none was provided");

        }

    }

    private PresentationPolicyAlternatives juxtaposeTokenAndDelegateChallenge(
            DelegateRequest c, String cb64, PresentationTokenDescription ptd) {
        return null;
    }


    protected ExonymAuthenticate(){
        super(1,"ExonymAuthenticate", 120000);

    }

    protected long challengeTimeout(){
        return 60000;

    }

    @Override
    protected void periodOfInactivityProcesses() {
        ArrayList<String> cleanup = new ArrayList<>();

        for (String challenge : challengeToT0.keySet()){
            long t0 = challengeToT0.get(challenge);
            if (Timing.hasBeen(t0, this.challengeTimeout())){
                cleanup.add(challenge);

            }
        }
        for (String c : cleanup){
            logger.info("Cleaning up " + c);
            challengeToT0.remove(c);
            String sessionId = challengeToSessionId.remove(c);
            challengeToAuthenticationRequest.remove(c);
            challengeToDomainContext.remove(c);
//            sessionIdToChallenge.remove(sessionId);
//            sessionIdToEndonym.remove(sessionId);

        }
    }

    @Override
    protected void receivedMessage(Msg msg) {
        // do nothing
    }
}
