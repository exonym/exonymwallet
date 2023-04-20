/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.util;

import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import io.exonym.lib.abc.attributeEncoding.MyAttributeEncodingFactory;
import io.exonym.lib.abc.attributeType.MyAttributeValue;
import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttribute;

import java.math.BigInteger;
import java.net.URI;

public class AttributeConverterImpl implements AttributeConverter {

  @Override
  public BigInteger getIntegerValueOrNull(Attribute attribute) {
    if(attribute.getAttributeValue() == null) {
      return null;
    }
    return new MyAttribute(attribute).getValue().getIntegerValueOrNull();
  }

  @Override
  public BigInteger getValueUnderEncoding(Object attributeOrConstant, AttributeDescription ad) {
    Attribute att = new Attribute();
    att.setAttributeValue(attributeOrConstant);
    att.setAttributeDescription(ad);
    return getIntegerValueOrNull(att);
  }

  @Override
  public Object recoverValueFromEncodedValue(BigInteger value, AttributeDescription ad) {
    URI encoding = ad.getEncoding();
    EnumAllowedValues eav = new EnumAllowedValues(ad);
    try {
      MyAttributeValue ret =
          MyAttributeEncodingFactory.recoverValueFromBigInteger(encoding, value, eav);
      return ret.getValueAsObject();
    } catch (RuntimeException ex) {
      return "!!! Could not inspect !!! " + value;
    }
  }

}
