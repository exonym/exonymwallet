/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValueDate;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.net.URI;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class MyEncodingDateUnsigned extends MyAttributeValueDate
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:date:unix:unsigned");

  public MyEncodingDateUnsigned(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
    if(getIntegerValue().compareTo(BigInteger.ZERO) < 0) {
      throw new RuntimeException("Unsigned dates must be >= 1970");
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    long unixTime = getValue().toGregorianCalendar().getTimeInMillis() / 1000;
    long result = unixTime/secondsInDay;
    return BigInteger.valueOf(result);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    try {
      long unixTime = integerValue.longValue() * secondsInDay;
      GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      cal.setGregorianChange(new Date(Long.MIN_VALUE));
      cal.setTimeInMillis(unixTime*1000);
      XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
      xmlCal.setHour(DatatypeConstants.FIELD_UNDEFINED);
      xmlCal.setMinute(DatatypeConstants.FIELD_UNDEFINED);
      xmlCal.setSecond(DatatypeConstants.FIELD_UNDEFINED);
      xmlCal.setFractionalSecond(null);
      return xmlCal;
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
}
