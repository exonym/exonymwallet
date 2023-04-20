/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValue;
import io.exonym.lib.abc.attributeType.MyAttributeValueString;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;

public class MyEncodingStringUtf8 extends MyAttributeValueString implements MyAttributeEncoding {

  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:string:utf-8");
  // 256 bits = 32 bytes, of which we need one to encode the length, so 31 bytes available
  private final int MAX_STRING_LENGTH = 31;
  
  public MyEncodingStringUtf8(Object attributeValue, /*IsNull*/ EnumAllowedValues av) {
    super(attributeValue, av);
    
    try {
      if (getValue().getBytes("UTF-8").length > MAX_STRING_LENGTH) {
        throw new RuntimeException("String too long: cannot use UTF-8 encoding!");
      }
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if (lhs instanceof MyEncodingStringUtf8) {
      return getIntegerValue().equals(((MyEncodingStringUtf8) lhs).getIntegerValue());
    } else {
      return super.isEquals(lhs);
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    try {
      return MyAttributeEncodingFactory.byteArrayToIntegerWithLength(getValue().getBytes("UTF-8"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, /*IsNull*/ EnumAllowedValues eav) {
    byte[] arr = MyAttributeEncodingFactory.decodeByteArrayWithLength(integerValue);
    try {
      String ret =  new String(arr, "UTF-8");
      return ret;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
