package io.exonym.example.sso;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.xml.SystemParameters;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.NotificationSubscriber;
import io.exonym.lib.api.SsoConfigWrapper;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.RulebookAuth;
import io.exonym.lib.pojo.SsoConfiguration;
import io.exonym.lib.standard.WhiteList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SsoProperties  {
    

    private static final Logger logger = LogManager.getLogger(SsoProperties.class);
    
    private static SsoProperties instance;


    private NotificationSubscriber subscriber;

    private SsoConfiguration basic;
    private SsoConfiguration sybil;
    private SsoConfiguration rulebooks;

    private URI myDomain;

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SsoProperties() throws UxException {
        String url = mandatory("SERVICE_URL");
        myDomain = URI.create(url);


        // The authentication request will be sent to domain provided.
        // The user identity (the endonym) will be unique within the context of the URL string
        SsoConfigWrapper basic = new SsoConfigWrapper(URI.create(myDomain + "/entry"));
        this.basic = basic.getConfig();

        // require the user to have executed one-time onboarding.
        SsoConfigWrapper sybil = new SsoConfigWrapper(URI.create(myDomain + "/no-clones"));
        sybil.requireSybil(true);
        this.sybil = sybil.getConfig();

        // you can require as many rulebooks as you need.
        // all rulebooks require the use to have onboarded to Sybil.
        SsoConfigWrapper rulebooks = new SsoConfigWrapper(URI.create(myDomain + "/accountability-required"));

        String rulebook = mandatory("RULEBOOK_URN");

        rulebooks.requireRulebook(URI.create(rulebook));
        subscriber = NotificationSubscriber.getInstance();
        subscriber.subscribe(rulebooks.getConfig(), true, true);

        String excludeAdvocate = optional("BLACKLIST_ADVOCATE", null);
        String excludeSource = optional("BLACKLIST_SOURCE", null);

        // You can black list specific advocates or sources.
        if (WhiteList.isModeratorUid(excludeAdvocate)){
            rulebooks.addModeratorToBlacklist(URI.create(excludeAdvocate));

        } else {
            logger.warn("BLACKLIST_ADVOCATE=" + excludeAdvocate);

        }
        if (WhiteList.isLeadUid(excludeSource)){
            rulebooks.addLeadToBlacklist(URI.create(excludeSource));

        } else {
            logger.warn("BLACKLIST_SOURCE=" + excludeAdvocate);
        }

        this.rulebooks=rulebooks.getConfig();

    }

    public SsoConfiguration getBasic() {
        return basic;
    }

    public SsoConfiguration getSybil() {
        return sybil;
    }

    public SsoConfiguration getRulebooks() {
        return rulebooks;
    }

    static {
        try {
            instance = new SsoProperties();

        } catch (UxException e) {
            logger.error("Critical Error", e);

        }
    }

    public static SsoProperties getInstance() {
        return instance;
    }
    



    protected String messageOnFail(String env, String message) throws UxException {
        try {
            return mandatory(env);

        } catch (UxException e) {
            throw new UxException("'" + env + "' is mandatory: " + message);

        }
    }

    protected String optional(String env, String def) {
        String var = System.getenv(env);
        if (var!=null){
            return var.trim();

        } else {
            return def;

        }
    }

    protected String mandatory(String env) throws UxException {
        String var = System.getenv(env);
        if (var==null || var.equals("")){
            throw new UxException("The environment variable '" + env + "' has not been set and is mandatory.");

        } else {
            return var.trim();

        }
    }

    public static void main(String[] args) throws Exception {
        SsoConfiguration configuration = new SsoConfiguration();
        configuration.setDomain(URI.create("https://example.com/transact"));
        RulebookAuth r = new RulebookAuth();
        r.setRulebookUID(URI.create("urn:rulebook:29a655983776d9cd7b4be696ed4cd773e63e6d640241e05c3a40b5d81f5d1f1c"));
        ArrayList<URI> ads = new ArrayList<>();
        ads.add(URI.create("urn:rulebook:lead-name:moderator-name:29a655983776d9cd7b4be696ed4cd773e63e6d640241e05c3a40b5d81f5d1f1c"));
        r.setModBlacklist(ads);

        ArrayList<URI> leads = new ArrayList<>();
        r.setLeadBlacklist(leads);
        leads.add(URI.create("urn:rulebook:another-lead-name:29a655983776d9cd7b4be696ed4cd773e63e6d640241e05c3a40b5d81f5d1f1c"));

        configuration.getHonestUnder().put(r.getRulebookUID().toString(), r);

        String j = JaxbHelper.serializeToJson(configuration, SsoConfiguration.class);
        System.out.println(j);


        try(InputStream stream = ClassLoader.getSystemResourceAsStream("lambda.xml")){
            if (stream!=null){
                byte[] in = new byte[stream.available()];
                stream.read(in);
                SystemParameters p = (SystemParameters) JaxbHelperClass.deserialize(new String(in, StandardCharsets.UTF_8)).getValue();
                logger.info(p.getSystemParametersUID().toString());

            } else {
                throw new UxException("SYSTEM_PARAMETERS_NOT_FOUND_ON_CLASS_PATH",
                        "Unable to load system parameters", "lambda.xml");

            }
        } catch (Exception e){
            throw e;

        }
    }
}
