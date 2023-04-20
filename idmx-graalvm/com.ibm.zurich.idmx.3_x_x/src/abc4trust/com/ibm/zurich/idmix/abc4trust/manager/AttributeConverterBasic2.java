//* Licensed Materials - Property of IBM                                     *
//* com.ibm.zurich.idmx.3_x_x                                                *
//* (C) Copyright IBM Corp. 2015. All Rights Reserved.                       *
//* US Government Users Restricted Rights - Use, duplication or              *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.        *
//*                                                                          *
//* The contents of this file are subject to the terms of either the         *
//* International License Agreement for Identity Mixer Version 1.2 or the    *
//* Apache License Version 2.0.                                              *
//*                                                                          *
//* The license terms can be found in the file LICENSE.txt that is provided  *
//* together with this software.                                             *
//*/**/***********************************************************************
package com.ibm.zurich.idmix.abc4trust.manager;

import java.math.BigInteger;

import org.w3c.dom.Element;

import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;

/**
 * Only for unsigned integers
 */
public class AttributeConverterBasic2 implements AttributeConverter {

  @Override
  public BigInteger getIntegerValueOrNull(final Attribute att) {
    final Object o = att.getAttributeValue();
    if (o == null) {
      return null;
    } else {
      return getValueUnderEncoding(o, null);
    }
  }

  @Override
  public BigInteger getValueUnderEncoding(final Object attributeValue, final AttributeDescription ad) {
    if (attributeValue instanceof BigInteger) {
      return ((BigInteger) attributeValue);
    } else if (attributeValue instanceof String) {
      return new BigInteger(((String) attributeValue).trim());
    } else if (attributeValue instanceof Integer) {
      return BigInteger.valueOf((Integer) attributeValue);
    } else if (attributeValue instanceof Long) {
      return BigInteger.valueOf((Long) attributeValue);
    } else if (attributeValue instanceof Element) {
      final String svalue = ((Element) attributeValue).getTextContent().trim();
      return new BigInteger(svalue);
    } else {
      if(attributeValue == null) {
        throw new RuntimeException("Attribute value is null");
      } else {
        throw new RuntimeException("Cannot parse attribute value as integer "
            + attributeValue.getClass());
      }
    }
  }

  @Override
  public Object recoverValueFromEncodedValue(final BigInteger value, final AttributeDescription ad) {
    return value;
  }

}
