package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.net.URI;

@XmlRootElement(name="MintedAnonCredential")
@XmlType(name = "MintedAnonCredential", namespace = Namespace.EX)
public class MintedAnonCredential {
	
	/*
	 * Oranization identifier
	 */
	@XmlElement(name = "GroupUID", namespace = Namespace.EX)
	private URI groupUid;

	/*
	 * attributes
	 */
	@XmlElement(name = "Country", namespace = Namespace.EX)
	private String countryG2;
	
	@XmlElement(name = "Endonym", namespace = Namespace.EX)
	private BigInteger endonymG3;
	
	/*
	 * Non-interactive ZKP
	 */
	@XmlElement(name = "Context", namespace = Namespace.EX)
	private String context;
	
	@XmlElement(name = "Challenge", namespace = Namespace.EX)
	private BigInteger challenge;
	
	@XmlElement(name = "CredentialCommitment", namespace = Namespace.EX)
	private BigInteger cCommit;
	
	@XmlElement(name = "t-Commitment", namespace = Namespace.EX)
	private BigInteger tCommit;
	
	@XmlElement(name = "s-Commitment0", namespace = Namespace.EX)
	private BigInteger sCommit0;
	
	@XmlElement(name = "s-Commitment1", namespace = Namespace.EX)
	private BigInteger sCommit1;
	
	@XmlElement(name = "s-Commitment2", namespace = Namespace.EX)
	private BigInteger sCommit2;
	
	@XmlElement(name = "s-Commitment3", namespace = Namespace.EX)
	private BigInteger sCommit3;

	@XmlElement(name = "Nym", namespace = Namespace.EX)
	private BigInteger zNym;
	
	@XmlElement(name = "t-Nym", namespace = Namespace.EX)
	private BigInteger tNym;
	
	@XmlElement(name = "s-Nym0", namespace = Namespace.EX)
	private BigInteger sNym0;
	
	@XmlElement(name = "s-Nym1", namespace = Namespace.EX)
	private BigInteger sNym1;
	
	public URI getGroupUid() {
		return groupUid;
	}
	public void setGroupUid(URI setUid) {
		this.groupUid = setUid;
	}
	public String getCountryG2() {
		return countryG2;
	}
	public void setCountryG2(String countryG2) {
		this.countryG2 = countryG2;
	}
	public BigInteger getEndonymG3() {
		return endonymG3;
	}
	public void setEndonymG3(BigInteger endonymG3) {
		this.endonymG3 = endonymG3;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public BigInteger getChallenge() {
		return challenge;
	}
	public void setChallenge(BigInteger challenge) {
		this.challenge = challenge;
	}
	public BigInteger getCredentialCommitment() {
		return cCommit;
	}
	public void setCredentialCommitment(BigInteger cCommit) {
		this.cCommit = cCommit;
	}
	public BigInteger gettCommit() {
		return tCommit;
	}
	public void settCommit(BigInteger tCommit) {
		this.tCommit = tCommit;
	}
	public BigInteger getsCommit0() {
		return sCommit0;
	}
	public void setsCommit0(BigInteger sCommit0) {
		this.sCommit0 = sCommit0;
	}
	public BigInteger getsCommit1() {
		return sCommit1;
	}
	public void setsCommit1(BigInteger sCommit1) {
		this.sCommit1 = sCommit1;
	}
	public BigInteger getsCommit2() {
		return sCommit2;
	}
	public void setsCommit2(BigInteger sCommit2) {
		this.sCommit2 = sCommit2;
	}
	public BigInteger getsCommit3() {
		return sCommit3;
	}
	public void setsCommit3(BigInteger sCommit3) {
		this.sCommit3 = sCommit3;
	}
	public BigInteger getzNym() {
		return zNym;
	}
	public void setzNym(BigInteger zNym) {
		this.zNym = zNym;
	}
	public BigInteger gettNym() {
		return tNym;
	}
	public void settNym(BigInteger tNym) {
		this.tNym = tNym;
	}
	public BigInteger getsNym0() {
		return sNym0;
	}
	public void setsNym0(BigInteger sNym0) {
		this.sNym0 = sNym0;
	}
	public BigInteger getsNym1() {
		return sNym1;
	}
	public void setsNym1(BigInteger sNym1) {
		this.sNym1 = sNym1;
	}
}
