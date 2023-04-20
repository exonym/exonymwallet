package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;

@XmlRootElement(name="AnonPresentationToken")
@XmlType(name = "AnonPresentationToken", namespace = Namespace.EX)
public class AnonPresentationToken {
	
	@XmlElement(name = "GroupUID", namespace = Namespace.EX)
	private URI groupUid; 
	@XmlElement(name = "Context", namespace = Namespace.EX)
	private String context;
	@XmlElement(name = "TimeStamp", namespace = Namespace.EX)
	private String timeStamp;
	
	// jc - commitement to credential in group n 
	@XmlElement(name = "ZkpCred", namespace = Namespace.EX)	
	private byte[] zkp_cred;
	// jc - commitment to credential in group n to quadratic residue bases
	@XmlElement(name = "ZkpCe", namespace = Namespace.EX)
	private byte[] zkp_acc_ce;
	// jc - commitment to witness base
	@XmlElement(name = "ZkpCu", namespace = Namespace.EX)
	private byte[] zkp_acc_cu;
	// jc - commitment to r2 from previous base
	@XmlElement(name = "ZkpCr", namespace = Namespace.EX)
	private byte[] zkp_acc_cr;
	// dac - commitment to a new pseudonym
	@XmlElement(name = "ZkpNym", namespace = Namespace.EX)
	private byte[] zkp_nym;
	// dac - commitment to id within group n
	@XmlElement(name = "ZkpId", namespace = Namespace.EX)
	private byte[] zkp_id;
	
	@XmlElement(name = "CredT1", namespace = Namespace.EX)
	private byte[] cred_t1;
	@XmlElement(name = "CredT2", namespace = Namespace.EX)
	private byte[] cred_t2;
	@XmlElement(name = "CredT3", namespace = Namespace.EX)
	private byte[] cred_t3;
	
	@XmlElement(name = "AccT1", namespace = Namespace.EX)
	private byte[] acc_t1;
	@XmlElement(name = "AccT2", namespace = Namespace.EX)
	private byte[] acc_t2;
	@XmlElement(name = "AccT3", namespace = Namespace.EX)
	private byte[] acc_t3;
	@XmlElement(name = "AccT4", namespace = Namespace.EX)
	private byte[] acc_t4;
	
	@XmlElement(name = "IdT", namespace = Namespace.EX)
	private byte[] id_t;
	@XmlElement(name = "NymT", namespace = Namespace.EX)
	private byte[] nym_t;
	
	// Relates to the Dynamic Accumulator manuscript
	@XmlElement(name = "s-alpha", namespace = Namespace.EX)
	private byte[] sAlpha;
	@XmlElement(name = "s-phi", namespace = Namespace.EX)
	private byte[] sPhi;
	@XmlElement(name = "s-beta", namespace = Namespace.EX)
	private byte[] sBeta;
	@XmlElement(name = "s-zeta", namespace = Namespace.EX)
	private byte[] sZeta;
	@XmlElement(name = "s-epsilon", namespace = Namespace.EX)
	private byte[] sEpsilon;
	@XmlElement(name = "s-gamma", namespace = Namespace.EX)
	private byte[] sGamma; 
	@XmlElement(name = "s-kappa", namespace = Namespace.EX)
	private byte[] sKappa;
	@XmlElement(name = "s-psi", namespace = Namespace.EX)
	private byte[] sPsi;
	@XmlElement(name = "s-eta", namespace = Namespace.EX)
	private byte[] sEta;
	@XmlElement(name = "s-delta", namespace = Namespace.EX)
	private byte[] sDelta;
	@XmlElement(name = "s-sigma", namespace = Namespace.EX)
	private byte[] sSigma;
	@XmlElement(name = "s-rho", namespace = Namespace.EX)
	private byte[] sRho;
	@XmlElement(name = "s-xi", namespace = Namespace.EX)
	private byte[] sXi;

	// s-values for ZKP of credential and the pseudonym.
	@XmlElement(name = "s-id0", namespace = Namespace.EX)
	private byte[] s_id0;
	@XmlElement(name = "s-id1", namespace = Namespace.EX)
	private byte[] s_id1;
	@XmlElement(name = "s-id2", namespace = Namespace.EX)
	private byte[] s_id2;
	@XmlElement(name = "s-id3", namespace = Namespace.EX)
	private byte[] s_id3;
	@XmlElement(name = "s-nym0", namespace = Namespace.EX)
	private byte[] s_nym0;
	@XmlElement(name = "s-nym1", namespace = Namespace.EX)
	private byte[] s_nym1;
	
	public URI getGroupUid() {
		return groupUid;
	}
	public void setGroupUid(URI setUid) {
		this.groupUid = setUid;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public byte[] getZkp_cred() {
		return zkp_cred;
	}
	public void setZkp_cred(byte[] zkp_cred) {
		this.zkp_cred = zkp_cred;
	}
	public byte[] getZkp_acc_ce() {
		return zkp_acc_ce;
	}
	public void setZkp_acc_ce(byte[] zkp_acc_ce) {
		this.zkp_acc_ce = zkp_acc_ce;
	}
	public byte[] getZkp_acc_cu() {
		return zkp_acc_cu;
	}
	public void setZkp_acc_cu(byte[] zkp_acc_cu) {
		this.zkp_acc_cu = zkp_acc_cu;
	}
	public byte[] getZkp_acc_cr() {
		return zkp_acc_cr;
	}
	public void setZkp_acc_cr(byte[] zkp_acc_cr) {
		this.zkp_acc_cr = zkp_acc_cr;
	}
	public byte[] getZkp_nym() {
		return zkp_nym;
	}
	public void setZkp_nym(byte[] zkp_nym) {
		this.zkp_nym = zkp_nym;
	}
	public byte[] getZkp_id() {
		return zkp_id;
	}
	public void setZkp_id(byte[] zkp_id) {
		this.zkp_id = zkp_id;
	}
	public byte[] getCred_t1() {
		return cred_t1;
	}
	public void setCred_t1(byte[] cred_t1) {
		this.cred_t1 = cred_t1;
	}
	public byte[] getCred_t2() {
		return cred_t2;
	}
	public void setCred_t2(byte[] cred_t2) {
		this.cred_t2 = cred_t2;
	}
	public byte[] getCred_t3() {
		return cred_t3;
	}
	public void setCred_t3(byte[] cred_t3) {
		this.cred_t3 = cred_t3;
	}
	public byte[] getAcc_t1() {
		return acc_t1;
	}
	public void setAcc_t1(byte[] acc_t1) {
		this.acc_t1 = acc_t1;
	}
	public byte[] getAcc_t2() {
		return acc_t2;
	}
	public void setAcc_t2(byte[] acc_t2) {
		this.acc_t2 = acc_t2;
	}
	public byte[] getAcc_t3() {
		return acc_t3;
	}
	public void setAcc_t3(byte[] acc_t3) {
		this.acc_t3 = acc_t3;
	}
	public byte[] getAcc_t4() {
		return acc_t4;
	}
	public void setAcc_t4(byte[] acc_t4) {
		this.acc_t4 = acc_t4;
	}
	public byte[] getId_t() {
		return id_t;
	}
	public void setId_t(byte[] id_t) {
		this.id_t = id_t;
	}
	public byte[] getNym_t() {
		return nym_t;
	}
	public void setNym_t(byte[] nym_t) {
		this.nym_t = nym_t;
	}
	public byte[] getsAlpha() {
		return sAlpha;
	}
	public void setsAlpha(byte[] sAlpha) {
		this.sAlpha = sAlpha;
	}
	public byte[] getsPhi() {
		return sPhi;
	}
	public void setsPhi(byte[] sPhi) {
		this.sPhi = sPhi;
	}
	public byte[] getsBeta() {
		return sBeta;
	}
	public void setsBeta(byte[] sBeta) {
		this.sBeta = sBeta;
	}
	public byte[] getsZeta() {
		return sZeta;
	}
	public void setsZeta(byte[] sZeta) {
		this.sZeta = sZeta;
	}
	public byte[] getsEpsilon() {
		return sEpsilon;
	}
	public void setsEpsilon(byte[] sEpsilon) {
		this.sEpsilon = sEpsilon;
	}
	public byte[] getsGamma() {
		return sGamma;
	}
	public void setsGamma(byte[] sGamma) {
		this.sGamma = sGamma;
	}
	public byte[] getsKappa() {
		return sKappa;
	}
	public void setsKappa(byte[] sKappa) {
		this.sKappa = sKappa;
	}
	public byte[] getsPsi() {
		return sPsi;
	}
	public void setsPsi(byte[] sPsi) {
		this.sPsi = sPsi;
	}
	public byte[] getsEta() {
		return sEta;
	}
	public void setsEta(byte[] sEta) {
		this.sEta = sEta;
	}
	public byte[] getsDelta() {
		return sDelta;
	}
	public void setsDelta(byte[] sDelta) {
		this.sDelta = sDelta;
	}
	public byte[] getsSigma() {
		return sSigma;
	}
	public void setsSigma(byte[] sSigma) {
		this.sSigma = sSigma;
	}
	public byte[] getsRho() {
		return sRho;
	}
	public void setsRho(byte[] sRho) {
		this.sRho = sRho;
	}
	public byte[] getsXi() {
		return sXi;
	}
	public void setsXi(byte[] sXi) {
		this.sXi = sXi;
	}
	public byte[] getS_id0() {
		return s_id0;
	}
	public void setS_id0(byte[] s_id0) {
		this.s_id0 = s_id0;
	}
	public byte[] getS_id1() {
		return s_id1;
	}
	public void setS_id1(byte[] s_id1) {
		this.s_id1 = s_id1;
	}
	public byte[] getS_id2() {
		return s_id2;
	}
	public void setS_id2(byte[] s_id2) {
		this.s_id2 = s_id2;
	}
	public byte[] getS_id3() {
		return s_id3;
	}
	public void setS_id3(byte[] s_id3) {
		this.s_id3 = s_id3;
	}
	public byte[] getS_nym0() {
		return s_nym0;
	}
	public void setS_nym0(byte[] s_nym0) {
		this.s_nym0 = s_nym0;
	}
	public byte[] getS_nym1() {
		return s_nym1;
	}
	public void setS_nym1(byte[] s_nym1) {
		this.s_nym1 = s_nym1;
	}
}
