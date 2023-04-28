package io.exonym.lib.pojo;

import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

public class Rulebook extends SerialErrorHandling {

    public static final String SYBIL_RULEBOOK_HASH = "800a5e641a0f9e6f2ba77c8a31384df2d034a969ccd565b8b9206fd4f8126296";

    public static final URI SYBIL_SOURCE_UID = URI.create(Namespace.URN_PREFIX_COLON
            + "sybil:" + SYBIL_RULEBOOK_HASH);

    public static final URI SYBIL_RULEBOOK_ID =  URI.create(Namespace.URN_PREFIX_COLON + SYBIL_RULEBOOK_HASH);

    public static final URI SYBIL_TEST_NET_UID =  URI.create(Namespace.URN_PREFIX_COLON
            + "sybil:test-net:" + SYBIL_RULEBOOK_HASH);

    public static final URI SYBIL_MAIN_NET_UID =  URI.create(Namespace.URN_PREFIX_COLON
            + "sybil:main-net:" + SYBIL_RULEBOOK_HASH);

    public static final String SYBIL_CLASS_PERSON = "person";
    public static final String SYBIL_CLASS_ENTITY = "entity";
    public static final String SYBIL_CLASS_ROBOT = "robot";
    public static final String SYBIL_CLASS_PRODUCT = "product";
    public static final String SYBIL_CLASS_REPRESENTATIVE = "representative";

    public static final String SYBIL_CLASS_TYPE = Namespace.URN_PREFIX_COLON + "sybil-class";
    private String rulebookId;

    private RulebookDescription description;
    private ArrayList<RulebookItem> rules = new ArrayList<>();
    private ArrayList<RulebookItem> ruleExtensions = new ArrayList<>();

    private String challengeB64;

    private ArrayList<String> acceptsSybilClasses = new ArrayList<>();

    private RulebookPenalties penalties;

    private String link;


    public Rulebook(){
        this.rulebookId = UUID.randomUUID()
                .toString().replaceAll("-", "")
                .substring(16);
        this.acceptsSybilClasses.add("all");
        this.penalties = new RulebookPenalties();
    }

    public String getRulebookId() {
        return rulebookId;
    }
    public URI computeCredentialSpecId() {
        return URI.create(rulebookId + ":c");
    }

    public void setRulebookId(String rulebookId) {
        this.rulebookId = rulebookId;
    }

    public ArrayList<RulebookItem> getRules() {
        return rules;
    }

    public void setRules(ArrayList<RulebookItem> rules) {
        this.rules = rules;
    }

    public static boolean isSybil(URI uid){
        return Rulebook.isSybil(uid.toString());

    }

    public static boolean isSybil(String rulebookId){
        if (rulebookId==null){
            return false;
        } else {
            return rulebookId.contains(SYBIL_RULEBOOK_HASH);
        }
    }

    public RulebookDescription getDescription() {
        return description;
    }

    public void setDescription(RulebookDescription description) {
        this.description = description;
    }

    public ArrayList<RulebookItem> getRuleExtensions() {
        return ruleExtensions;
    }

    public void setRuleExtensions(ArrayList<RulebookItem> ruleExtensions) {
        this.ruleExtensions = ruleExtensions;
    }

    public ArrayList<String> getAcceptsSybilClasses() {
        return acceptsSybilClasses;
    }

    public void setAcceptsSybilClasses(ArrayList<String> acceptsSybilClasses) {
        this.acceptsSybilClasses = acceptsSybilClasses;
    }

    public RulebookPenalties getPenalties() {
        return penalties;
    }

    public void setPenalties(RulebookPenalties penalties) {
        this.penalties = penalties;
    }

    public String getChallengeB64() {
        return challengeB64;
    }

    public void setChallengeB64(String challengeB64) {
        this.challengeB64 = challengeB64;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
