package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.net.URI;

@XmlRootElement(name="RegistrationParameters")
@XmlType(name = "RegistrationParameters", namespace = Namespace.EX)
public class RegistrationParameters {
	
	@XmlElement(name = "GroupUid", namespace = Namespace.EX)
	private URI groupUid;
	
	@XmlElement(name = "Country", namespace = Namespace.EX)
	private String country;

	@XmlElement(name = "RSAModulus", namespace = Namespace.EX)
	private BigInteger accumulatorModulus;

	@XmlElement(name = "u", namespace = Namespace.EX)
	private BigInteger accumulatorBase;
	
	@XmlElement(name = "p", namespace = Namespace.EX)
	private BigInteger p;
	
	@XmlElement(name = "q", namespace = Namespace.EX)
	private BigInteger q;
	
	@XmlElement(name = "g0", namespace = Namespace.EX)
	private BigInteger g0;
	
	@XmlElement(name = "g1", namespace = Namespace.EX)
	private BigInteger g1;
	
	@XmlElement(name = "g2", namespace = Namespace.EX)
	private BigInteger g2;
	
	@XmlElement(name = "g3", namespace = Namespace.EX)
	private BigInteger g3;
	
	@XmlElement(name = "gQRn", namespace = Namespace.EX)
	private BigInteger gQRn;
	
	@XmlElement(name = "hQRn", namespace = Namespace.EX)
	private BigInteger hQRn;
	
	@XmlElement(name = "B", namespace = Namespace.EX)
	private BigInteger b;


	public URI getGroupUid() {
		return groupUid;
	}

	public void setGroupUid(URI groupUid) {
		this.groupUid = groupUid;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public BigInteger getAccumulatorModulus() {
		return accumulatorModulus;
	}

	public void setAccumulatorModulus(BigInteger accumulatorModulus) {
		this.accumulatorModulus = accumulatorModulus;
	}

	public BigInteger getAccumulatorBase() {
		return accumulatorBase;
	}

	public void setAccumulatorBase(BigInteger accumulatorBase) {
		this.accumulatorBase = accumulatorBase;
	}

	public BigInteger getP() {
		return p;
	}

	public void setP(BigInteger p) {
		this.p = p;
	}

	public BigInteger getQ() {
		return q;
	}

	public void setQ(BigInteger q) {
		this.q = q;
	}

	public BigInteger getG0() {
		return g0;
	}

	public void setG0(BigInteger g0) {
		this.g0 = g0;
	}

	public BigInteger getG1() {
		return g1;
	}

	public void setG1(BigInteger g1) {
		this.g1 = g1;
	}

	public BigInteger getG2() {
		return g2;
	}

	public void setG2(BigInteger g2) {
		this.g2 = g2;
	}

	public BigInteger getG3() {
		return g3;
	}

	public void setG3(BigInteger g3) {
		this.g3 = g3;
	}

	public BigInteger getB() {
		return b;
		
	}

	public void setB(BigInteger b) {
		this.b = b;
		
	}

	public BigInteger getgQRn() {
		return gQRn;
	}

	public void setgQRn(BigInteger gQRn) {
		this.gQRn = gQRn;
	}

	public BigInteger gethQRn() {
		return hQRn;
	}

	public void sethQRn(BigInteger hQRn) {
		this.hQRn = hQRn;
	}

}
