/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValueDate;

import java.math.BigInteger;
import java.net.URI;


public class MyEncodingDateSince2010 extends MyAttributeValueDate
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:date:since2010:unsigned");
  // There were leap years every 4 years between 1970 and 2010 (including 2000)
  private static final long daysSince1970 = 365*(2010-1970) + (2010-1970)/4;

  public MyEncodingDateSince2010(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
    if(getIntegerValue().compareTo(BigInteger.ZERO) < 0) {
      throw new RuntimeException("Unsigned dates must be >= 2010");
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    long unixTime = getValue().toGregorianCalendar().getTimeInMillis() / 1000;
    long secondsInDay = 60*60*24;
    long result = unixTime/secondsInDay;
    

    
    return BigInteger.valueOf(result-daysSince1970);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    return MyEncodingDateUnsigned.recoverValueFromIntegerValue(integerValue.add(BigInteger.valueOf(daysSince1970)), eav);
  }
}
