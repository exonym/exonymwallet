package io.exonym.lib.pojo;

import io.exonym.lib.helpers.DateHelper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.ArrayList;

@XmlRootElement(name="ConnectKeyContainer")
@XmlType(name = "ConnectKeyContainer", namespace = Namespace.EX, propOrder={"deviceUid", "utcDate", "keyPairs"})
public class ConnectKeyContainer {

	@XmlElement(name="KeyPairs", namespace=Namespace.EX)
	private ArrayList<XKey> keyPairs = new ArrayList<>();
	
	@XmlElement(name="UtcDate", namespace=Namespace.EX)
	private String utcDate;

	@XmlElement(name="DeviceUid", namespace=Namespace.EX)
	private URI deviceUid;

	public ArrayList<XKey> getKeyPairs() {
		return keyPairs;
		
	}

	public void setKeyPairs(ArrayList<XKey> keyPairs) {
		this.keyPairs = keyPairs;
		
	}

	public URI getDeviceUid() {
		return deviceUid;
	}

	public void setDeviceUid(URI deviceUid) {
		this.deviceUid = deviceUid;
	}

	public String getFileName(){
		return utcDate.replaceAll("-", "") + "-" + deviceUid + ".kc.xml";
		
	}
	public String getUtcDate() {
		return utcDate;
	}

	public void setUtcDate(String utcDate) {
		this.utcDate = utcDate;
	}

	public static ConnectKeyContainer make(URI deviceUid){
		ConnectKeyContainer connect = new ConnectKeyContainer();
		connect = new ConnectKeyContainer();
		connect.setDeviceUid(deviceUid);
		connect.setUtcDate(DateHelper.currentIsoUtcDate());
		return connect;
		
	}
}
