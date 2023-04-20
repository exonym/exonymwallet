/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValueTime;

import java.math.BigInteger;
import java.net.URI;

public class MyEncodingTime extends MyAttributeValueTime
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:time:sinceMidnight:unsigned");

  public MyEncodingTime(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
  }

  @Override
  public BigInteger getIntegerValue() {
    long unixTime = getValue().toGregorianCalendar().getTimeInMillis() / 1000;
    long secondsInDay = 60*60*24;
    long result = unixTime%secondsInDay;
    // Fix modulo for negative numbers
    if(result < 0) {
      result += secondsInDay;
    }
    return BigInteger.valueOf(result);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    int seconds = integerValue.mod(BigInteger.valueOf(60)).intValue();
    integerValue = integerValue.divide(BigInteger.valueOf(60));
    int minutes = integerValue.mod(BigInteger.valueOf(60)).intValue();
    integerValue = integerValue.divide(BigInteger.valueOf(60));
    int hours = integerValue.intValue();
    return String.format("%02d:%02d:%02dZ", hours, minutes, seconds);
  }
}
