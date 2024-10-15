package io.exonym.lib.pojo;

import java.net.URI;
import java.util.ArrayList;

public class RevocationRequest {


	public static final int MAX_LENGTH_OF_DESCRIPTION_BYTES = 190;

	private String descriptionOfEvidence = "The requesting moderator has no evidence on file.";

	private String endonymToken;
	private ArrayList<URI> ruleUri;
	private URI moderatorUid;

	public String getEndonymToken() {
		return endonymToken;
	}

	/**
	 * The compressed token encoded as b64
	 *
	 * @param endonymToken
	 */
	public void setEndonymToken(String endonymToken) {
		this.endonymToken = endonymToken;
	}

	public ArrayList<URI> getRuleUrns() {
		return ruleUri;
	}

	public void addRuleUri(URI ruleUrn) {
		if (this.ruleUri==null){
			this.ruleUri = new ArrayList<>();
		}
		this.ruleUri.add(ruleUrn);
	}

	public URI getModeratorUid() {
		return moderatorUid;
	}

	/**
	 * This is set by your node when it forwards requests to other nodes.
	 * It therefore does not need to be set in the original request.
	 *
	 * @param moderatorUid
	 */
	public void setModeratorUid(URI moderatorUid) {
		this.moderatorUid = moderatorUid;
	}

	public String getDescriptionOfEvidence() {
		return descriptionOfEvidence;
	}

	/**
	 * The default value will result in poor defense against appeal.
	 *
	 * Describe any evidence such as "reported 150 times by peers in under 24 hours."
	 *
	 * @param descriptionOfEvidence Must be less than MAX_LENGTH_OF_DESCRIPTION_BYTES
	 */
	public void setDescriptionOfEvidence(String descriptionOfEvidence) {
		this.descriptionOfEvidence = descriptionOfEvidence;
	}

	public ArrayList<URI> getRuleUri() {
		return ruleUri;
	}

	public void setRuleUri(ArrayList<URI> ruleUri) {
		this.ruleUri = ruleUri;
	}
}


