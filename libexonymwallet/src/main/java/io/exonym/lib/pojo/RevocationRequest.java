package io.exonym.lib.pojo;

import java.net.URI;
import java.util.ArrayList;

public class RevocationRequest {


	private String endonymToken;
	private ArrayList<URI> ruleUri;
	private URI moderatorUid;

	private String index;

	public static final int MAX_LENGTH_OF_DESCRIPTION_BYTES = 190;

	private String descriptionOfEvidence = "The requesting moderator has no evidence on file.";

	public String getEndonymToken() {
		return endonymToken;
	}

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

	public void setModeratorUid(URI moderatorUid) {
		this.moderatorUid = moderatorUid;
	}

	public ArrayList<URI> getRuleUri() {
		return ruleUri;
	}

	public void setRuleUri(ArrayList<URI> ruleUri) {
		this.ruleUri = ruleUri;
	}

	public String getDescriptionOfEvidence() {
		return descriptionOfEvidence;
	}

	public void setDescriptionOfEvidence(String descriptionOfEvidence) {
		this.descriptionOfEvidence = descriptionOfEvidence;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}
}


