package io.exonym.lib.lite;

import io.exonym.lib.pojo.RulebookAuth;

import java.util.ArrayList;

public class FulfillmentReport {

    private String endonymUnderDomain;
    private ArrayList<String> issuersToUse = new ArrayList<>();
    private ArrayList<RulebookAuth> missing = new ArrayList<>();
    private WalletReport userChoices = new WalletReport();


    public ArrayList<RulebookAuth> getMissing() {
        return missing;
    }

    public WalletReport getUserChoices() {
        return userChoices;
    }

    public ArrayList<String> getIssuersToUse() {
        return issuersToUse;
    }

    public String getEndonymUnderDomain() {
        return endonymUnderDomain;
    }

    public void setEndonymUnderDomain(String endonymUnderDomain) {
        this.endonymUnderDomain = endonymUnderDomain;
    }

    public boolean isProvable(){
        return missing.isEmpty() && userChoices.getRulebooksToIssuers().isEmpty();
    }
}
