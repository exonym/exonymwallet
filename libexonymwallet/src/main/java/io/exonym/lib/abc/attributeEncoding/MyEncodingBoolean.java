/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValue;
import io.exonym.lib.abc.attributeType.MyAttributeValueBoolean;

import java.math.BigInteger;
import java.net.URI;


public class MyEncodingBoolean extends MyAttributeValueBoolean implements MyAttributeEncoding {
  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:boolean:unsigned");
  
  public MyEncodingBoolean(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if (lhs instanceof MyEncodingBoolean) {
      return getIntegerValue().equals(((MyEncodingBoolean) lhs).getIntegerValue());
    } else {
      return super.isEquals(lhs);
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    if (getValue()) {
      return BigInteger.ONE;
    } else {
      return BigInteger.ZERO;
    }
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    if(integerValue.equals(BigInteger.ONE)) {
      return true;
    } else if(integerValue.equals(BigInteger.ZERO)) {
      return false;
    } else {
      throw new RuntimeException("Cannot recover boolean value");
    }
  }
}
