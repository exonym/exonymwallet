package io.exonym.lib.helpers;

import io.exonym.lib.pojo.Penalty;

import java.net.URI;

public class RuleForAppeal {

    private URI ruleUid;
    private String ruleOriginal;
    private String interpretation;
    private Penalty currentPenalty;

    public URI getRuleUid() {
        return ruleUid;
    }

    public void setRuleUid(URI ruleUid) {
        this.ruleUid = ruleUid;
    }

    public String getRuleOriginal() {
        return ruleOriginal;
    }

    public void setRuleOriginal(String ruleOriginal) {
        this.ruleOriginal = ruleOriginal;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public Penalty getCurrentPenalty() {
        return currentPenalty;
    }

    public void setCurrentPenalty(Penalty currentPenalty) {
        this.currentPenalty = currentPenalty;
    }
}
