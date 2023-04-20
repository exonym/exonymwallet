/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValueDate;

import java.math.BigInteger;
import java.net.URI;


public class MyEncodingDateSince1870 extends MyAttributeValueDate
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:date:since1870:unsigned");
  
  // There were leap years every 4 years between 1870 and 2010 EXCEPT for 1900
  private final static long daysUntil1970 = 365*(1970-1870) + (1970-1870)/4 - 1;

  public MyEncodingDateSince1870(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
    if(getIntegerValue().compareTo(BigInteger.ZERO) < 0) {
      throw new RuntimeException("Unsigned dates must be >= 1870");
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    long unixTime = getValue().toGregorianCalendar().getTimeInMillis() / 1000;
    long secondsInDay = 60*60*24;
    long result = unixTime/secondsInDay;
    
    return BigInteger.valueOf(result+daysUntil1970);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    return MyEncodingDateUnsigned.recoverValueFromIntegerValue(integerValue.subtract(BigInteger.valueOf(daysUntil1970)), eav);
  }
}
