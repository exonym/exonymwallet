/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeType;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.FriendlyDescription;
import io.exonym.lib.abc.attributeEncoding.MyAttributeEncodingFactory;

import java.net.URI;
import java.util.List;

/**
 * A class providing better methods that the JAXB Attribute class.
 * @author enr
 *
 */
public class MyAttribute {
  
  private final Attribute attribute;
  private final MyAttributeValue value;
  
  public MyAttribute(Attribute att) {
    this.attribute = att;
    EnumAllowedValues eav = new EnumAllowedValues(att.getAttributeDescription());
    checkCompatibleEncoding();
    this.value = MyAttributeEncodingFactory.parseValueFromEncoding(getEncoding(), att.getAttributeValue(), eav);
  }
  
  private void checkCompatibleEncoding() {
    if(!MyAttributeEncodingFactory.getDatatypeFromEncoding(getEncoding()).equals(getDataType())) {
      throw new RuntimeException("Attribute with wrong encoding");
    }
  }

  public URI getDataType() {
    return attribute.getAttributeDescription().getDataType();
  }
  
  public URI getEncoding() {
    return attribute.getAttributeDescription().getEncoding();
  }

  public MyAttributeValue getValue() {
    return value;
  }

  public URI getType() {
    return attribute.getAttributeDescription().getType();
  }

  public Object getAttributeValue() {
    return attribute.getAttributeValue();
  }

  public List<FriendlyDescription> getFriendlyAttributeName() {
    return attribute.getAttributeDescription().getFriendlyAttributeName();
  }
  
  public Attribute getXmlAttribute() {
    return attribute;
  }
}
