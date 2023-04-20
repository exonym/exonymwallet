package io.exonym.lib.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

@XmlRootElement(name="RegisteredDevices")
@XmlType(name = "RegisteredDevices", namespace = Namespace.EX)
public class RegisteredDevices {
	
	@XmlElement(name = "Devices", namespace = Namespace.EX)
	private ArrayList<Device> devices = new ArrayList<>();;

	public ArrayList<Device> getDevices() {
		return devices;
	}

}
