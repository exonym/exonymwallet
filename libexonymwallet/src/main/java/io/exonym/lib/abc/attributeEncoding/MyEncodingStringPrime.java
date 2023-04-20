/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.EnumIndexer;
import io.exonym.lib.abc.attributeType.MyAttributeValue;
import io.exonym.lib.abc.attributeType.MyAttributeValueString;

import java.math.BigInteger;
import java.net.URI;

public class MyEncodingStringPrime extends MyAttributeValueString implements MyAttributeEncoding {

  public static final URI ENCODING = URI.create("urn:abc4trust:1.0:encoding:string:prime");
  private static PrimeIndexer primeIndexer = new PrimeIndexer();
  
  public MyEncodingStringPrime(Object attributeValue, /*Nullable*/ EnumAllowedValues eav) {
    super(attributeValue, eav);
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if (lhs instanceof MyEncodingStringPrime) {
      return getIntegerValue().equals(((MyEncodingStringPrime) lhs).getIntegerValue());
    } else {
      return super.isEquals(lhs);
    }
  }

  @Override
  public BigInteger getIntegerValue() {
    EnumAllowedValues eav = getAllowedValues();
    if (eav == null) {
      throw new RuntimeException("Enum encoding comes without allowed values. Abort");
    }
    int index = eav.getPosition(getValue());
    return primeIndexer.getNthPrime(index);
  }
  
  @Override
  public URI getEncoding() {
    return ENCODING;
  }
  
  public static Object recoverValueFromIntegerValue(BigInteger integerValue, EnumAllowedValues eav) {
    Integer index = primeIndexer.getIndexOfPrime(integerValue);
    if(index == null) {
      throw new RuntimeException("Cannot recover enum value: not a prime");
    }
    if (eav == null) {
      throw new RuntimeException("EnumAllowedValues is null");
    }
    return eav.getAllowedValues().get(index);
  }
  
  public static EnumIndexer getEnumIndexer() {
    return primeIndexer;
  }
  
  @Override
  protected PrimeIndexer getIndexer() {
    return primeIndexer;
  }
}
