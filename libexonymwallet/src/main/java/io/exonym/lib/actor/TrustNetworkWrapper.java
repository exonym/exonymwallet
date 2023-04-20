package io.exonym.lib.actor;

import io.exonym.lib.helpers.DateHelper;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.pojo.NetworkParticipant;
import io.exonym.lib.pojo.NodeInformation;
import io.exonym.lib.pojo.TrustNetwork;
import io.exonym.lib.pojo.XKey;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;

public class TrustNetworkWrapper {
	private final TrustNetwork trustNetwork;
	private HashMap<URI, NetworkParticipant> participants = new HashMap<>();
	
	private final static Logger logger = Logger.getLogger(TrustNetworkWrapper.class.getName());
	public TrustNetworkWrapper(TrustNetwork trustNetwork) {
		if (trustNetwork==null) {
			throw new NullPointerException();
			
		}
		this.trustNetwork=trustNetwork;
		
		for (NetworkParticipant n : this.trustNetwork.getParticipants()) {
			participants.put(n.getNodeUid(), n);

		}
		this.trustNetwork.getParticipants().clear();
		
	}

	/**
	 *
	 * @param p
	 * @throws Exception
	 */
	public void addParticipant(NetworkParticipant p) throws Exception {
		if (p!=null && p.getNodeUid()==null) {
			throw new Exception("Node Name was not set " + p);
			
		}
		participants.put(p.getNodeUid(), p);
		
	}

	public Collection<NetworkParticipant> getAllParticipants(){
		if (!this.participants.isEmpty()){
			return this.participants.values();

		} else {
			return Collections.EMPTY_LIST;

		}
	}

	/**
	 *
	 * @param nodeUid
	 * @param nodeUrl
	 * @param failover
	 * @param publicKey
	 * @return
	 */
	public NetworkParticipant addParticipant(URI nodeUid, URI nodeUrl,
											 URI failover, URI xNodeUrl, URI multicastUrl,
											 XKey publicKey, String region) throws Exception {
		logger.info("Adding Participant " + nodeUid);
		if (nodeUid== null) {
			throw new NullPointerException("Node UID");

		} if (nodeUrl == null) {
			throw new NullPointerException("Node URL");

		} if (failover == null) {
			throw new NullPointerException("Failover");

		} if (xNodeUrl == null) {
			throw new NullPointerException("XNode URL");

		} if (publicKey == null) {
			throw new NullPointerException("Node Public Key");
			
		} if (region==null){
			// throw new NullPointerException("Region");

		}
		NetworkParticipant p = new NetworkParticipant();
		p.setNodeUid(nodeUid);
		p.setStaticNodeUrl0(nodeUrl.toURL());
		p.setRulebookNodeUrl(xNodeUrl.toURL());
		p.setBroadcastAddress(multicastUrl);
		p.setPublicKey(publicKey);
		p.setStaticNodeUrl1(failover.toURL());
		p.setRegion(region);
		p.setLastUpdateTime(DateHelper.currentIsoUtcDateTime());
		p.setAvailableOnMostRecentRequest(true);
		this.participants.put(nodeUid, p);
		return p;
		
	}
	
	
	public void removeParticipant(URI p) throws HubException {
		URI participantUid = computeParticipantUid(p);
		logger.info("Attempting to remove Participant " + participantUid);
		NetworkParticipant part = participants.remove(participantUid);
		if (part!=null) {
			logger.info("Successfully removed participant");
			
		} else {
			logger.info("Failed to removed participant");
			
		}
	}
	
	private URI computeParticipantUid(URI p) throws HubException {
		String uid = p.toString();
		String[] parts = uid.split(":");
		if (parts.length==5) {
			return p;
			
		} else if (uid.endsWith(":i")) {
			return URI.create(parts[0] + ":" + parts[1] + ":" + parts[2] + ":" + parts[3] + ":" + parts[4]);
			
		} else {
			throw new HubException("Unknown Participant Translation " + p);
			
		}
	}

	public NetworkParticipant getParticipant(URI name) {
		return participants.get(name);
		
	}
	
	public NetworkParticipant getParticipantWithError(URI  name) throws Exception {
		NetworkParticipant p = participants.get(name);
		if (p!=null) {
			return p; 
			
		} else {
			throw new Exception("There was no participant called " + name);
			
		}
	}
	
	public TrustNetwork finalizeTrustNetwork() {
		ArrayList<NetworkParticipant> l = this.trustNetwork.getParticipants();
		for (URI name : participants.keySet()) {
			l.add(participants.get(name));
			
		}
		trustNetwork.setLastUpdated(DateHelper.currentIsoUtcDateTime());
		return trustNetwork;
		
	}
	
	public NodeInformation getNodeInformation() {
		return trustNetwork.getNodeInformation();
		
	}
	
	public URI getMostRecentIssuerParameters() {
		return this.trustNetwork.getNodeInformation().getIssuerParameterUids().getLast();
		
	}
	
	public static TrustNetworkWrapper open(URI url) {
		return null;
		
	}
}