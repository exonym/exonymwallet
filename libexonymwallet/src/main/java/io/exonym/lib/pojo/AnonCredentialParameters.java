package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.net.URI;

@XmlRootElement(name="AnonCredentialParameters")
@XmlType(name = "AnonCredentialParameters", namespace = Namespace.EX)
public class AnonCredentialParameters {
	
	@XmlElement(name = "GroupUID", namespace = Namespace.EX)
	private URI groupUid;
	
	@XmlElement(name = "Country", namespace = Namespace.EX)
	private String countryG2;
	
	@XmlElement(name = "Endonym", namespace = Namespace.EX)
	private BigInteger endonymG3;

	@XmlElement(name = "Credential", namespace = Namespace.EX)
	private BigInteger anonCredential;
	
	@XmlElement(name = "SetNymSk", namespace = Namespace.EX)
	private BigInteger setNymSk;
	
	@XmlElement(name = "SetNym", namespace = Namespace.EX)
	private BigInteger setNym;
	
	@XmlElement(name = "CredentialSk", namespace = Namespace.EX)
	private BigInteger credentialSk;
	
	@XmlElement(name = "Witness", namespace = Namespace.EX)
	private BigInteger witness;

	@XmlElement(name = "Accumulator", namespace = Namespace.EX)
	private BigInteger accumulator;

	public URI getGroupUid() {
		return groupUid;
	}
	public void setGroupUid(URI groupUid) {
		this.groupUid = groupUid;
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
	public BigInteger getAnonCredential() {
		return anonCredential;
	}
	public void setAnonCredential(BigInteger anonCredential) {
		this.anonCredential = anonCredential;
	}
	public BigInteger getCredentialSk() {
		return credentialSk;
	}
	public void setCredentialSk(BigInteger credentialSk) {
		this.credentialSk = credentialSk;
	}
	public BigInteger getSetNymSk() {
		return setNymSk;
	}
	public void setSetNymSk(BigInteger setNymSk) {
		this.setNymSk = setNymSk;
	}
	public BigInteger getSetNym() {
		return setNym;
	}
	public void setSetNym(BigInteger setNym) {
		this.setNym = setNym;
	}
	public BigInteger getBaseWitness() {
		return witness;
	}
	public void setWitness(BigInteger witness) {
		this.witness = witness;
	}
	public BigInteger getAccumulator() {
		return accumulator;
	}
	public void setAccumulator(BigInteger accumulator) {
		this.accumulator = accumulator;
	}
	
}
