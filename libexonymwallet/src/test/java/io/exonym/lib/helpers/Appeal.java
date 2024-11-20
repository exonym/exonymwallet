package io.exonym.lib.helpers;


import io.exonym.lib.pojo.VioIndexable;

import java.net.URI;
import java.util.ArrayList;

public class Appeal implements VioIndexable {

    public static final String OUTCOME_OVERRIDE = "OVERRIDE";
    public static final String OUTCOME_EXPUNGED = "EXPUNGED";
    public static final String OUTCOME_PERSISTED = "PERSISTED";

    // When the appeal is being raised by the user
    public static final String STATUS_RAISING = "RAISING";

    // When the appeal is newly submitted and awaiting initial review
    public static final String STATUS_NEW = "NEW";

    // When the appeal is actively being reviewed by the relevant authorities
    public static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";

    // When additional information is requested from the appellant
    public static final String STATUS_NEED_MORE_INFO = "NEED_MORE_INFO";

    // When the appeal has been escalated to a higher authority for further review
    public static final String STATUS_ESCALATED = "ESCALATED";

    // When a decision on the appeal has been reached
    public static final String STATUS_DECISION_MADE = "DECISION_MADE";

    // When the appeal process is completed and closed
    public static final String STATUS_CLOSED = "CLOSED";

    // When the appeal is rejected during review
    public static final String STATUS_REJECTED = "REJECTED";

    // When the appeal is approved and the requested action will be taken
    public static final String STATUS_APPROVED = "APPROVED";

    // When the appellant withdraws the appeal
    public static final String STATUS_WITHDRAWN = "WITHDRAWN";
    private String status;
    private String timeOfViolation;
    private String banLifted = "N/A";
    private URI modOfVioUid;
    private URI requestingModUid;
    private boolean openForAppeal = false;

    private String nibble6;
    private String x0Hash;
    private URI hostMod;

    public String getX0Hash() {
        return x0Hash;
    }

    public void setX0Hash(String x0Hash) {
        this.x0Hash = x0Hash;
    }

    public URI getHostMod() {
        return hostMod;
    }

    public void setHostMod(URI hostMod) {
        this.hostMod = hostMod;
    }

    private ArrayList<RuleForAppeal> targetRules = new ArrayList<>();
    private ArrayList<AppealTransaction> history = new ArrayList<>();

    private String outcome;

    @Override
    public String getNibble6() {
        return nibble6;
    }

    public void setNibble6(String nibble6) {
        this.nibble6 = nibble6;
    }

    @Override
    public String getTimeOfViolation() {
        return timeOfViolation;
    }

    public void setTimeOfViolation(String timeOfViolation) {
        this.timeOfViolation = timeOfViolation;
    }

    @Override
    public URI getModOfVioUid() {
        return modOfVioUid;
    }

    public void setModOfVioUid(URI modOfVioUid) {
        this.modOfVioUid = modOfVioUid;
    }

    public URI getRequestingModUid() {
        return requestingModUid;
    }

    public void setRequestingModUid(URI requestingModUid) {
        this.requestingModUid = requestingModUid;
    }

    public boolean isOpenForAppeal() {
        return openForAppeal;
    }

    public void setOpenForAppeal(boolean openForAppeal) {
        this.openForAppeal = openForAppeal;
    }

    public String getBanLifted() {
        return banLifted;
    }

    public void setBanLifted(String banLifted) {
        this.banLifted = banLifted;
    }

    public ArrayList<RuleForAppeal> getTargetRules() {
        return targetRules;
    }

    public void setTargetRules(ArrayList<RuleForAppeal> targetRules) {
        this.targetRules = targetRules;
    }

    public ArrayList<AppealTransaction> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<AppealTransaction> history) {
        this.history = history;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
