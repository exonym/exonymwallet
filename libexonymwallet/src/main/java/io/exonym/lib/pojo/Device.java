package io.exonym.lib.pojo;

import io.exonym.lib.exceptions.UxException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;

@XmlRootElement(name="Device")
@XmlType(name = "Device", namespace = Namespace.EX)
public class Device {
	
	@XmlElement(name = "DeviceUID", namespace = Namespace.EX)
	private URI deviceUid;
	
	@XmlElement(name = "Name", namespace = Namespace.EX)
	private String localName;
	
	@XmlElement(name = "AccessToken", namespace = Namespace.EX)
	private String accessToken;
	
	public URI getDeviceUid() {
		return deviceUid;
	}
	public void setDeviceUid(URI deviceUid) {
		this.deviceUid = deviceUid;
	}
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public static String createAuthToken(String pin, Cipher enc) throws UxException, IllegalBlockSizeException, BadPaddingException{
		if (pin.length()!=4){
			throw new UxException("The PIN is invalid cannot create AuthToken");
			
		} if (enc==null){
			throw new UxException("Bad Cipher");
			
		}
		String raw = pin.substring(2);
		return Base64.encodeBase64String(enc.doFinal(raw.getBytes()));
		
	}
	
	public static String decipherAuthToken(String token, Cipher dec)throws Exception{
		return new String(dec.doFinal(Base64.decodeBase64(token.getBytes())));
				
	}
}
