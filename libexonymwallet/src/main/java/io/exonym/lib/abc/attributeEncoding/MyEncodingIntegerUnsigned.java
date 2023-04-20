/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValueInteger;

import java.math.BigInteger;
import java.net.URI;

public class MyEncodingIntegerUnsigned extends MyAttributeValueInteger
    implements
      MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:integer:unsigned");

  public MyEncodingIntegerUnsigned(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
    if (getValue().compareTo(BigInteger.ZERO) < 0) {
      throw new RuntimeException("Unsigned integer must be larger than 0");
    } else if (getValue().compareTo(MyAttributeEncodingFactory.MAX_VALUE) >= 0) {
      throw new RuntimeException("Unsigned integer must be smaller than 2^256");
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    return getValue();
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    return integerValue;
  }
}
