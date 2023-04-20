package io.exonym.lib.helpers;

import java.net.URI;

public class UriClaimType {
	
	public final static URI FIRST_NAME = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");
	public final static URI LAST_NAME = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname");
	public final static URI EMAIL = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");
	public final static URI AD_STREET = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/streetaddress");
	public final static URI AD_LOCALITY = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/locality");
	public final static URI AD_STATE = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/stateorprovince");
	public final static URI AD_POST = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/postalcode");
	public final static URI AD_COUNTRY = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/country");
	public final static URI PHONE_HOME = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/homephone");
	public final static URI PHONE_OTHER = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/otherphone");
	public final static URI PHONE_MOBILE = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/mobilephone");
	public final static URI DOB = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/dateofbirth");
	public final static URI GENDER = URI.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/gender");	
	
}
