package io.exonym.example.sso;

import io.exonym.lib.exceptions.AlreadyAuthException;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.EndonymToken;
import io.exonym.lib.pojo.SsoChallenge;
import io.exonym.lib.pojo.SsoConfiguration;

import java.net.URI;
import java.util.logging.Logger;

public class ExonymAuthenticate extends io.exonym.lib.api.ExonymAuthenticate {
    
    private final static Logger logger = Logger.getLogger(ExonymAuthenticate.class.getName());
    
    private static ExonymAuthenticate instance;
    
    static {
        instance = new ExonymAuthenticate();
        
    }
    
    private ExonymAuthenticate(){
        super();
    }
    
    protected static ExonymAuthenticate getInstance(){
        return instance;
    }

    @Override
    protected void challenge(SsoChallenge challenge, String sessionId) {
        super.challenge(challenge, sessionId);
    }

    @Override
    protected URI probeForContext(String sessionId) throws UxException {
        return super.probeForContext(sessionId);
    }

    @Override
    protected void authenticate(String token) throws UxException, HubException {
        super.authenticate(token);
    }


    @Override
    protected EndonymToken isAuthenticated(String sessionId, URI context) throws UxException {
        return super.isAuthenticated(sessionId, context);
    }

    @Override
    protected boolean isAuthenticatedQuiet(String sessionId, URI context)  {
        return super.isAuthenticatedQuiet(sessionId, context);
    }

    @Override
    protected SsoChallenge authIfNeeded(SsoConfiguration config, String sessionId) throws AlreadyAuthException {
        return super.authIfNeeded(config, sessionId);
    }

    @Override
    protected long challengeTimeout() {
        return super.challengeTimeout();
    }

    @Override
    protected void close() throws Exception {
        super.close();
    }

    @Override
    protected EndonymToken isAuthenticatedWait(String sessionId, URI context, long timeout) throws UxException {
        return super.isAuthenticatedWait(sessionId, context, timeout);
    }

    @Override
    protected void removeSession(String session) {
        super.removeSession(session);
    }
}
