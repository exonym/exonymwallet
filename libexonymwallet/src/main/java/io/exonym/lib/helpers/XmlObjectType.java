package io.exonym.lib.helpers;

public class XmlObjectType {
	
	private Class<?> clazz = null;
	private boolean exonym = false;
	
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public boolean isExonym() {
		return exonym;
	}
	public void setExonym(boolean exonym) {
		this.exonym = exonym;
	}

}
