/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValueDateTime;

import java.math.BigInteger;
import java.net.URI;


public class MyEncodingDateTimeSigned extends MyAttributeValueDateTime
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:signed");

  public MyEncodingDateTimeSigned(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
  }

  @Override
  public BigInteger getIntegerValue() {
    long unixTime = getValue().toGregorianCalendar().getTimeInMillis() / 1000;
    return BigInteger.valueOf(unixTime).add(MyAttributeEncodingFactory.SIGNED_OFFSET);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    return MyEncodingDateTimeUnsigned.recoverValueFromIntegerValue(integerValue.subtract(MyAttributeEncodingFactory.SIGNED_OFFSET), eav);
  }
}
