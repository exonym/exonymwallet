package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.ArrayList;

@XmlRootElement(name="XNodeDescription", namespace = Namespace.EX)
@XmlType(name="XNodeDescription", namespace = Namespace.EX)
public class XNodeDescription {

    private String shortName;
    private String longName;
    private String nodeMissionSummary;
    private URI rulebookUid;
    private ArrayList<String> nodeMissionParagraphs;
    private ArrayList<RulebookDescription> rules;
    private String companySignOff;
    @XmlElement(name = "ShortOrganizationName", namespace = Namespace.EX)
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @XmlElement(name = "LongOrganizationName", namespace = Namespace.EX)
    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    @XmlElement(name = "NodeMissionSummary", namespace = Namespace.EX)
    public String getNodeMissionSummary() {
        return nodeMissionSummary;
    }

    public void setNodeMissionSummary(String nodeMissionSummary) {
        this.nodeMissionSummary = nodeMissionSummary;
    }

    @XmlElement(name = "NodeMissionParagraphs", namespace = Namespace.EX)
    public ArrayList<String> getNodeMissionParagraphs() {
        return nodeMissionParagraphs;
    }

    public void setNodeMissionParagraphs(ArrayList<String> nodeMissionParagraphs) {
        this.nodeMissionParagraphs = nodeMissionParagraphs;
    }
    @XmlElement(name = "RulebookUID", namespace = Namespace.EX)
    public URI getRulebookUid() {
        return rulebookUid;
    }

    public void setRulebookUid(URI rulebookUid) {
        this.rulebookUid = rulebookUid;
    }

    @XmlElement(name = "Rulebook", namespace = Namespace.EX)
    public ArrayList<RulebookDescription> getRules() {
        return rules;
    }

    public void setRules(ArrayList<RulebookDescription> rules) {
        this.rules = rules;
    }

    @XmlElement(name = "OrganizationSignOff", namespace = Namespace.EX)
    public String getCompanySignOff() {
        return companySignOff;
    }

    public void setCompanySignOff(String companySignOff) {
        this.companySignOff = companySignOff;
    }
}
