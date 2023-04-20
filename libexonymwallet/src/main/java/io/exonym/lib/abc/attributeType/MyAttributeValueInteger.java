/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeType;

import org.w3c.dom.Element;

import java.math.BigInteger;

public class MyAttributeValueInteger extends MyAttributeValue {

  private BigInteger value;
  
  public MyAttributeValueInteger(Object attributeValue,  /*IsNull*/ EnumAllowedValues allowedValues) {
    super(allowedValues);
    if(attributeValue instanceof BigInteger) {
      value = ((BigInteger)attributeValue);
    } else if(attributeValue instanceof String) {
        value = new BigInteger((String)attributeValue);
    } else if(attributeValue instanceof Integer) {
      value = BigInteger.valueOf((Integer)attributeValue);
    } else if(attributeValue instanceof Long) {
      value = BigInteger.valueOf((Long)attributeValue);
    } else if(attributeValue instanceof Element) {
      String svalue = ((Element)attributeValue).getTextContent();
      value = new BigInteger(svalue);
    } else {
      throw new RuntimeException("Cannot parse attribute value as integer " + attributeValue.getClass());
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueInteger) {
      return ((MyAttributeValueInteger)lhs).value.equals(value);
    } else {
      return false;
    }
  }
  
  @Override
  public boolean isLess(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueInteger) {
      BigInteger lhsInt = ((MyAttributeValueInteger)lhs).value;
      return (value.compareTo(lhsInt) < 0);
    } else {
      return false;
    }
  }
  
  protected BigInteger getValue() {
    return value;
  }
  
  @Override
  public Object getValueAsObject() {
    return getValue();
  }

}
