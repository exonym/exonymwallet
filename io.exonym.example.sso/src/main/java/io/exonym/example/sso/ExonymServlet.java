package io.exonym.example.sso;

import com.google.gson.JsonObject;
import io.exonym.lib.exceptions.*;
import io.exonym.lib.pojo.AuthenticationWrapper;
import io.exonym.lib.pojo.EndonymToken;
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

                } else if (requests[1].equals("reset")){
                    auth.removeSession(sessionId);
                    return null;

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

            if (probeOrXmlToken.startsWith("{\"probe")){
                logger.info("----------------- Probe IN");
                probeIn(req, resp);

            } else {
                logger.info("----------------- Token IN");
                ExonymAuthenticate auth = ExonymAuthenticate.getInstance();
                auth.authenticate(probeOrXmlToken);

            }
        } catch (UxException e) {
            logger.error("Error", e);
            logger.info("--------------- Received UxException");
            JsonObject o = new JsonObject();
            o.addProperty("error", "" + e.getMessage());
            resp.getWriter().write(o.toString());

        } catch (Exception e) {
            logger.error("Error", e);
            logger.info("--------------- Received Other Exception");
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
        logger.info("Waiting on authentication for " + sessionId);
        EndonymToken endonym = auth.isAuthenticatedWait(sessionId, context, 30000l);
        logger.info("Got authentication for " + sessionId);
        if (endonym.isTimeout()){
            throw new UxException(ErrorMessages.TIME_OUT);

        } else if (endonym.hasError()){
            throw new UxException(endonym.getError());

        } else {
            // you have an authenticated identifier for this user associated with a session.
            logger.debug("GOT ID=" + endonym.getEndonym());
            JsonObject o = new JsonObject();
            o.addProperty("auth", true);
            o.addProperty("endonym", endonym.getEndonym().toString());
            resp.getWriter().write(o.toString());
            Files.write(PATH_TO_STATIC.resolve(endonym.computeIndex()),
                    endonym.getCompressedPresentationToken(),
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);


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
