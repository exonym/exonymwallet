package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.ArrayList;

@XmlRootElement(name="TrustNetwork", namespace = Namespace.EX)
@XmlType(name="TrustNetwork", namespace = Namespace.EX)
		// propOrder={"nodeInformationUid", "lastUpdated", "nodeInformation", "participants"})
public class TrustNetwork {

	private URI nodeInformationUid;
	private String lastUpdated;
	private NodeInformation nodeInformation;
	private ArrayList<NetworkParticipant> participants = null;

	@XmlElement(name = "NodeInformationUID", namespace = Namespace.EX)
	public URI getNodeInformationUid() {
		return nodeInformationUid;
	}

	public void setNodeInformationUid(URI nodeInformationUid) {
		this.nodeInformationUid = nodeInformationUid;
	}

	@XmlElement(name = "NodeInformation", namespace = Namespace.EX)
	public NodeInformation getNodeInformation() {
		return nodeInformation;
	}

	public void setNodeInformation(NodeInformation nodeInformation) {
		this.nodeInformation = nodeInformation;
	}

	@XmlElement(name = "NetworkParticipants", namespace = Namespace.EX)
	public ArrayList<NetworkParticipant> getParticipants() {
		if (participants==null) {
			participants = new ArrayList<NetworkParticipant>();
			
		}
		return participants;
	}

	@XmlElement(name = "NodeLastUpdatedUTC", namespace = Namespace.EX)
	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
