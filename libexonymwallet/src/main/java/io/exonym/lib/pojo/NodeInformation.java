package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.LinkedList;

@XmlType(name="NodeInformation", namespace = Namespace.EX)  
/* 
 * propOrder={"nodeName", "nodeUrl", "failOverNodeUrl", "nodeRootUid", 
				"sourceUrl", "failOverSourceUrl", "sourceUid", 
				"lastUpdateReceived", "issuerParameterUids"}
 */
public class NodeInformation {

	private String nodeName;
	private URI staticNodeUrl0;
	private URI staticNodeUrl1;
	private URI staticSourceUrl0;
	private URI staticSourceUrl1;
	private URI nodeUid;
	private URI rulebookNodeUrl;
	private URI broadcastAddress;
	private URI sourceUid;
	private String region;
	private String lastUpdateReceived;
	private LinkedList<URI> issuerParameterUids = null;

	@XmlElement(name = "RulebookNodeURL", namespace = Namespace.EX)
	public URI getRulebookNodeUrl() {
		return rulebookNodeUrl;
	}

	public void setRulebookNodeUrl(URI rulebookNodeUrl) {
		this.rulebookNodeUrl = rulebookNodeUrl;
	}

	@XmlElement(name = "NodeName", namespace = Namespace.EX)
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@XmlElement(name = "StaticNodeURL0", namespace = Namespace.EX)
	public URI getStaticNodeUrl0() {
		return staticNodeUrl0;
	}

	public void setStaticNodeUrl0(URI staticNodeUrl0) {
		this.staticNodeUrl0 = staticNodeUrl0;
	}

	@XmlElement(name = "StaticNodeURL1", namespace = Namespace.EX)
	public URI getStaticNodeUrl1() {
		return staticNodeUrl1;
	}

	public void setStaticNodeUrl1(URI staticNodeUrl1) {
		this.staticNodeUrl1 = staticNodeUrl1;
	}

	@XmlElement(name = "BroadcastAddress", namespace = Namespace.EX)
	public URI getBroadcastAddress() {
		return broadcastAddress;
	}

	public void setBroadcastAddress(URI broadcastAddress) {
		this.broadcastAddress = broadcastAddress;
	}

	@XmlElement(name = "NodeUID", namespace = Namespace.EX)
	public URI getNodeUid() {
		return nodeUid;
	}

	public void setNodeUid(URI nodeUid) {
		this.nodeUid = nodeUid;
	}

	@XmlElement(name = "StaticSourceURL0", namespace = Namespace.EX)
	public URI getStaticSourceUrl0() {
		return staticSourceUrl0;
	}

	public void setStaticSourceUrl0(URI staticSourceUrl0) {
		this.staticSourceUrl0 = staticSourceUrl0;
	}

	@XmlElement(name = "StaticSourceURL1", namespace = Namespace.EX)
	public URI getStaticSourceUrl1() {
		return staticSourceUrl1;
	}

	public void setStaticSourceUrl1(URI staticSourceUrl1) {
		this.staticSourceUrl1 = staticSourceUrl1;
	}

	@XmlElement(name = "SourceUID", namespace = Namespace.EX)
	public URI getSourceUid() {
		return sourceUid;
	}

	public void setSourceUid(URI sourceUid) {
		this.sourceUid = sourceUid;
	}

	@XmlElement(name = "LastUpdatedReceivedUTC", namespace = Namespace.EX)
	public String getLastUpdateReceived() {
		return lastUpdateReceived;
	}

	@XmlElement(name = "Jurisdiction", namespace = Namespace.EX)
	public String getRegion() {
		return region;

	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setLastUpdateReceived(String lastUpdateReceived) {
		this.lastUpdateReceived = lastUpdateReceived;
	}

	@XmlElement(name = "IssuerParameterUID", namespace = Namespace.EX)
	public LinkedList<URI> getIssuerParameterUids() {
		if (issuerParameterUids==null) {
			issuerParameterUids = new LinkedList<URI>();
			
		}
		return issuerParameterUids;
	}

	public void setIssuerParameterUids(LinkedList<URI> issuerParameterUids) {
		this.issuerParameterUids = issuerParameterUids;
		
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NodeInformation) {
			NodeInformation n =(NodeInformation)obj;
			return n.getNodeUid().equals(this.getNodeUid());
			
		} else {
			return false; 
			 
		}
	}

	@Override
	public int hashCode() {
		return this.getNodeUid().hashCode();
		
	}
}
