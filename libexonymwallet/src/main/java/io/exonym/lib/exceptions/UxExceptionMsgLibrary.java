package io.exonym.lib.exceptions;


import io.exonym.lib.pojo.Namespace;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="AssetLibrary")
@XmlType(name = "AssetLibrary", namespace = Namespace.EX)
public class UxExceptionMsgLibrary {

	public enum LANG { EN, DE }
	
	private LANG lang;
	

}
