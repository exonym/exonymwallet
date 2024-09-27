package io.exonym.example.sso;

import com.google.gson.JsonObject;
import com.ibm.zurich.idmx.exception.ProofException;
import io.exonym.lib.exceptions.*;
import io.exonym.lib.helpers.Timing;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.AuthenticationWrapper;
import io.exonym.lib.pojo.EndonymToken;
import io.exonym.lib.pojo.IdContainer;
import io.exonym.lib.pojo.SsoChallenge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@WebServlet("/exonym/*")
public class ExonymServlet extends HttpServlet {

    private final Path PATH_TO_STATIC = Path.of("/var", "www", "html", "tokens");
    

    private static final Logger logger = LogManager.getLogger(ExonymServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        JsonObject o = new JsonObject();
        try {
            ExonymAuthenticate auth = ExonymAuthenticate.getInstance();
            String sessionId = req.getSession().getId();
            String path = req.getPathInfo();
            SsoChallenge challenge = levelOfAccess(path, sessionId);
            auth.challenge(challenge, sessionId);
            if (challenge != null) {
                try {
                    String c = AuthenticationWrapper.wrap(challenge, 150, SsoChallenge.class);
                    resp.getWriter().write(c);

                } catch (Exception e) {
                    throw e;

                }
            } else {
                throw new UxException("UNKNOWN_SSO_CONFIG_REQUEST");

            }
        } catch (AlreadyAuthException e){
            o.addProperty("auth", true);
            resp.getWriter().write(o.toString());

        } catch (UxException e) {
            o.addProperty("error", e.getMessage());
            resp.getWriter().write(o.toString());

        } catch (Exception e) {
            o.addProperty("error", "SERVER_ERROR");
            logger.debug("Error", e);
            resp.getWriter().write(o.toString());

        }
    }

    private void requestAuth(HttpServletRequest req, HttpServletResponse resp,
                             ExonymAuthenticate auth, String sessionId) throws Exception {
    }

    private SsoChallenge levelOfAccess(String path, String sessionId) throws IOException, AlreadyAuthException {
        SsoProperties props = SsoProperties.getInstance();
        ExonymAuthenticate auth = ExonymAuthenticate.getInstance();

        if (path==null || path.equals("/")){
            return auth.authIfNeeded(props.getBasic(), sessionId);

        } else {
            String[] requests = path.split("/");
            logger.debug(path + " " + requests.length);
            if (requests.length>1){
                if (requests[1].equals("sybil")){
                    return auth.authIfNeeded(props.getSybil(), sessionId);

                } else if  (requests[1].equals("rulebooks")){
                    return auth.authIfNeeded(props.getRulebooks(), sessionId);

                }
            }
        }
        return null;
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String probeOrXmlToken = buildParamsAsString(req);
            logger.debug(probeOrXmlToken);
            if (probeOrXmlToken.startsWith("{\"probe")){
                probeIn(req, resp);

            } else {
                ExonymAuthenticate auth = ExonymAuthenticate.getInstance();
                auth.authenticate(probeOrXmlToken);


            }
        } catch (UxException e) {
            JsonObject o = new JsonObject();
            o.addProperty("error", e.getMessage());
            resp.getWriter().write(o.toString());

        } catch (Exception e) {
            JsonObject o = new JsonObject();
            logger.info("Error ", e);
            o.addProperty("error", ErrorMessages.FAILED_TO_AUTHORIZE);
            resp.getWriter().write(o.toString());

        }
    }

    private void probeIn(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ExonymAuthenticate auth = ExonymAuthenticate.getInstance();
        String sessionId = req.getSession().getId();
        URI context = auth.probeForContext(sessionId);
        synchronized (sessionId){
            try {
                long timeout = auth.challengeTimeout();
                long t0 = Timing.currentTime();
                sessionId.wait(timeout);
                JsonObject o = new JsonObject();
                if (Timing.hasBeen(t0, timeout)){
                    o.addProperty("timeout", true);
                    resp.getWriter().write(o.toString());

                } else {
                    // you have an authenticated identifier for this user associated with a session.
                    EndonymToken endonym = auth.isAuthenticated(sessionId, context);
                    logger.debug("GOT ID=" + endonym.getEndonym());
                    o.addProperty("auth", true);
                    o.addProperty("endonym", endonym.getEndonym().toString());
                    resp.getWriter().write(o.toString());
                    Files.write(PATH_TO_STATIC.resolve(endonym.computeIndex()),
                            endonym.getXmlPresentationToken().getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

                }
            } catch (InterruptedException e) {
                throw new RuntimeException();

            }
        }
    }


    public static String buildParamsAsString(HttpServletRequest req) throws Exception {
        InputStream inputStream = req.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[30];
        int r=0;
        while( r >= 0 ) {
            r = inputStream.read(buffer);
            if( r >= 0 ) outputStream.write(buffer, 0, r);

        }
        outputStream.close();
        return outputStream.toString();
    }

}
