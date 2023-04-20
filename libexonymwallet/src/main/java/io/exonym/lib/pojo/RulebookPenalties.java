package io.exonym.lib.pojo;

import java.util.ArrayList;

public class RulebookPenalties {

    public static final String ACCOUNTABILITY_REQUIREMENT_FINANCIAL = "financial";
    public static final String ACCOUNTABILITY_REQUIREMENT_TEMPORAL = "temporal";
    public static final String ACCOUNTABILITY_REQUIREMENT_SERVICE = "service";
    public static final String ACCOUNTABILITY_REQUIREMENT_TEST_NET = "test-net";

    private ArrayList<String> accountabilityTypes = new ArrayList<>();

    public RulebookPenalties() {
        this.accountabilityTypes.add(ACCOUNTABILITY_REQUIREMENT_TEST_NET);
    }

    public ArrayList<String> getAccountabilityTypes() {
        return accountabilityTypes;
    }

    public void setAccountabilityTypes(ArrayList<String> accountabilityTypes) {
        this.accountabilityTypes = accountabilityTypes;
    }
}
