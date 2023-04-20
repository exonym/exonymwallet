/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeType;

import org.w3c.dom.Element;

public class MyAttributeValueBoolean extends MyAttributeValue {

  private boolean value;
  
  public MyAttributeValueBoolean(Object attributeValue,  /*IsNull*/ EnumAllowedValues allowedValues) {
    super(allowedValues);
    if(attributeValue instanceof Boolean) {
      value = ((Boolean)attributeValue).booleanValue();
    } else if(attributeValue instanceof Element) {
      String svalue = ((Element)attributeValue).getTextContent();
      value = Boolean.parseBoolean(svalue);
    } else {
      throw new RuntimeException("Cannot parse attribute value as boolean");
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueBoolean) {
      return ((MyAttributeValueBoolean)lhs).value == value;
    } else {
      return false;
    }
  }

  @Override
  public boolean isLess(MyAttributeValue myAttributeValue) {
    throw new UnsupportedOperationException("Can't call 'less' on an URI");
  }
  
  protected boolean getValue() {
    return value;
  }

  @Override
  public Object getValueAsObject() {
    return Boolean.valueOf(value);
  }

}
