package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.net.URL;

@XmlType(name="NetworkParticipant", namespace = Namespace.EX)
public class NetworkParticipant {
	
	private String networkName;
	private URI staticNodeUrl0;
	private URI rulebookNodeUrl;
	private URI broadcastAddress;
	private URI nodeUid;

	private URI lastIssuerUID;
	private String lastUpdateTime;
	private String region;
	private boolean availableOnMostRecentRequest;
	private XKey publicKey;

	@XmlElement(name = "RulebookNodeURL", namespace = Namespace.EX)
	public URI getRulebookNodeUrl() {
		return rulebookNodeUrl;
	}

	public void setRulebookNodeUrl(URI rulebookNodeUrl) {
		this.rulebookNodeUrl = rulebookNodeUrl;
	}

	@XmlElement(name = "NetworkName", namespace = Namespace.EX)
	public String getNetworkName() {
		return networkName;
	}

	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public URI getNodeUid() {
		return nodeUid;
	}

	@XmlElement(name = "NodeUID", namespace = Namespace.EX)
	public void setNodeUid(URI nodeUid) {
		this.nodeUid = nodeUid;
	}

	@XmlElement(name = "StaticNodeURL0", namespace = Namespace.EX)
	public URI getStaticNodeUrl0() {
		return staticNodeUrl0;
	}

	public void setStaticNodeUrl0(URI staticNodeUrl0) {
		this.staticNodeUrl0 = staticNodeUrl0;
	}

	@XmlElement(name = "BroadcastAddress", namespace = Namespace.EX)
	public URI getBroadcastAddress() {
		return broadcastAddress;
	}

	public void setBroadcastAddress(URI broadcastAddress) {
		this.broadcastAddress = broadcastAddress;
	}

	@XmlElement(name = "LastUpdateTime", namespace = Namespace.EX)
	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	@XmlElement(name = "AvailableOnMostRecentRequest", namespace = Namespace.EX)
	public boolean isAvailableOnMostRecentRequest() {
		return availableOnMostRecentRequest;
	}

	public void setAvailableOnMostRecentRequest(boolean availableOnMostRecentRequest) {
		this.availableOnMostRecentRequest = availableOnMostRecentRequest;
	}

	@XmlElement(name = "PublicKey", namespace = Namespace.EX)
	public XKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(XKey publicKey) {
		this.publicKey = publicKey;
	}

	@XmlElement(name = "NodeRegion", namespace = Namespace.EX)
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	@XmlElement(name = "LastIssuerUID", namespace = Namespace.EX)
	public URI getLastIssuerUID() {
		return lastIssuerUID;
	}

	public void setLastIssuerUID(URI lastIssuerUID) {
		this.lastIssuerUID = lastIssuerUID;
	}
}
