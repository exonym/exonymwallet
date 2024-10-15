package io.exonym.lib.pojo;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

public class Penalty implements Comparable<Penalty> {

    public static final String TYPE_NONE = "NONE";
    public static final String TYPE_TIME_BAN = "TIME_BAN";
    public static final String TYPE_SERVICE = "SERVICE";
    public static final String TYPE_FINANCIAL = "FINANCIAL";

    public static final String DEN_TEMP_MINUTES = "Minutes";
    public static final String DEN_TEMP_HOURS = "Hours";
    public static final String DEN_TEMP_DAYS = "Days";
    public static final String DEN_TEMP_MONTHS = "Months";
    public static final String DEN_TEMP_YEARS = "Years";
    public static final String DEN_TEMP_PERMANENT = "PERMANENT";

    private String type = TYPE_TIME_BAN;
    private String denomination = DEN_TEMP_MINUTES;
    private HashMap<Integer, URI> modUidForService;
    private int quantity = 2;
    private double repeatOffenceMultiplier = 2;
    private int offenceCount = 1; // New field for offence count
    private String modifier = Rulebook.MODIFIER_PROTECTED;

    // Constructor, getters and setters...

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getRepeatOffenceMultiplier() {
        return repeatOffenceMultiplier;
    }

    public void setRepeatOffenceMultiplier(double repeatOffenceMultiplier) {
        this.repeatOffenceMultiplier = repeatOffenceMultiplier;
    }

    public int getOffenceCount() {
        return offenceCount;
    }

    public void setOffenceCount(int offenceCount) {
        this.offenceCount = offenceCount;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public HashMap<Integer, URI> getModUidForService() {
        return modUidForService;
    }

    public void setModUidForService(HashMap<Integer, URI> modUidForService) {
        this.modUidForService = modUidForService;
    }

    private int calculatePenaltySeverity() {
        return (int) (quantity * Math.max(1, (offenceCount - 1))
                * repeatOffenceMultiplier);
    }

    private int getTemporalSeverity() {
        switch (denomination) {
            case DEN_TEMP_MINUTES: return calculatePenaltySeverity();
            case DEN_TEMP_HOURS: return calculatePenaltySeverity() * 60;
            case DEN_TEMP_DAYS: return calculatePenaltySeverity() * 60 * 24;
            case DEN_TEMP_MONTHS: return calculatePenaltySeverity() * 60 * 24 * 30;
            case DEN_TEMP_YEARS: return calculatePenaltySeverity() * 60 * 24 * 365;
            case DEN_TEMP_PERMANENT: return Integer.MAX_VALUE; // Highest severity
            default: return 0;
        }
    }

    private int getFinancialSeverity() {
        return calculatePenaltySeverity();
    }

    @Override
    public int compareTo(Penalty other) {
        int thisTypeSeverity = getTypeSeverity(this.type);
        int otherTypeSeverity = getTypeSeverity(other.type);

        if (thisTypeSeverity != otherTypeSeverity) {
            return Integer.compare(thisTypeSeverity, otherTypeSeverity);
        }

        switch (this.type) {
            case TYPE_TIME_BAN:
                return Integer.compare(this.getTemporalSeverity(), other.getTemporalSeverity());
            case TYPE_SERVICE:
                return Integer.compare(this.calculatePenaltySeverity(), other.calculatePenaltySeverity());
            case TYPE_FINANCIAL:
                return Integer.compare(this.getFinancialSeverity(), other.getFinancialSeverity());
            default:
                return 0;
        }
    }

    private int getTypeSeverity(String type) {
        switch (type) {
            case TYPE_TIME_BAN:
                return 1;
            case TYPE_SERVICE:
                return 2;
            case TYPE_FINANCIAL:
                return 3;
            default:
                return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Penalty penalty = (Penalty) o;
        return quantity == penalty.quantity &&
                repeatOffenceMultiplier == penalty.repeatOffenceMultiplier &&
                offenceCount == penalty.offenceCount &&
                Objects.equals(type, penalty.type) &&
                Objects.equals(denomination, penalty.denomination) &&
                Objects.equals(modUidForService, penalty.modUidForService) &&
                Objects.equals(modifier, penalty.modifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, denomination, modUidForService,
                quantity, repeatOffenceMultiplier, offenceCount, modifier);
    }

    @Override
    public String toString() {
        return this.type + " (" + this.quantity + " * (offenceCount -1) * " +
                this.repeatOffenceMultiplier + ") " + this.denomination;
    }
}
