package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;

@XmlRootElement(name="LocalLedgerGroup")
@XmlType(name = "LocalLedgerGroup", namespace = Namespace.EX)
public class LocalLedgerGroup {
	
	@XmlElement(name = "GroupUID", namespace = Namespace.EX)
	private URI groupUid;

	@XmlElement(name = "VerboseCredentials", namespace = Namespace.EX)
	private ArrayList<MintedAnonCredential> fullCredentials = new ArrayList<>();
	
	@XmlElement(name = "ShortCredentials", namespace = Namespace.EX)
	private ArrayList<BigInteger> credentials = new ArrayList<>();
	
	@XmlElement(name = "Accumulator", namespace = Namespace.EX)
	private BigInteger accumulator = BigInteger.ZERO;

	public URI getGroupUid() {
		return groupUid;
	}

	public void setGroupUid(URI groupUid) {
		this.groupUid = groupUid;
	}

	public ArrayList<MintedAnonCredential> getFullCredentials() {
		return fullCredentials;
	}
	public ArrayList<BigInteger> getCredentials() {
		return credentials;
	}

	public BigInteger getAccumulator() {
		return accumulator;
	}

	public void setAccumulator(BigInteger accumulator) {
		this.accumulator = accumulator;
	}

	public void setCredentials(ArrayList<BigInteger> credentials) {
		this.credentials = credentials;
	}
	
	
	
}
