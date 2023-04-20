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

package com.ibm.zurich.idmix.abc4trust.util;

import java.math.BigInteger;


import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;

import javax.inject.Inject;

/**
 * 
 */
public class AttributeConverterBasic implements AttributeConverter {

  @SuppressWarnings("unused")
  private CredentialManager credentialManager;

  @Inject
  public AttributeConverterBasic(final CredentialManager credentialManager) {
    this.credentialManager = credentialManager;
  }



  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.util.AttributeConverter#getIntegerValueOrNull(eu.abc4trust.xml.Attribute)
   */
  @Override
  public BigInteger getIntegerValueOrNull(final Attribute att) {
    if (att.getAttributeValue() == null) {
      return null;
    }
    return new BigInteger(att.getAttributeValue().toString().trim());
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.util.AttributeConverter#getValueUnderEncoding(java.lang.Object, java.net.URI,
   * java.net.URI)
   */
  @Override
  public BigInteger getValueUnderEncoding(final Object attributeOrConstant, final AttributeDescription ad) {
    final Attribute att = new Attribute();
    att.setAttributeValue(attributeOrConstant);
    att.setAttributeDescription(ad);
    return getIntegerValueOrNull(att);
  }



  @Override
  public Object recoverValueFromEncodedValue(final BigInteger value, final AttributeDescription ad) {
    return value;
  }

}
