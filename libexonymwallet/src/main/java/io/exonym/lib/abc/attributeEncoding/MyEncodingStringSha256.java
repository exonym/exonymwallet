/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValue;
import io.exonym.lib.abc.attributeType.MyAttributeValueString;

import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;

public class MyEncodingStringSha256 extends MyAttributeValueString implements MyAttributeEncoding {

  
  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:string:sha-256");
  public MyEncodingStringSha256(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if (lhs instanceof MyEncodingStringSha256) {
      return getIntegerValue().equals(((MyEncodingStringSha256) lhs).getIntegerValue());
    } else {
      return super.isEquals(lhs);
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(getValue().getBytes("UTF-8"));
      return MyAttributeEncodingFactory.byteArrayToInteger(md.digest());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    throw new RuntimeException("Cannot recover original value from hashed strings.");
  }
}
