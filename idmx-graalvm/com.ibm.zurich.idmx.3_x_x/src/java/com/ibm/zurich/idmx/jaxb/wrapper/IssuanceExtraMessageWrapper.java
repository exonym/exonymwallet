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

package com.ibm.zurich.idmx.jaxb.wrapper;

import java.util.List;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.jaxb.ParameterListWrapper;

import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.ObjectFactory;

/**
 * 
 */
public class IssuanceExtraMessageWrapper extends ParameterListWrapper<IssuanceExtraMessage> {


  public IssuanceExtraMessageWrapper() {
    this(new ObjectFactory().createIssuanceExtraMessage());
  }

  public IssuanceExtraMessageWrapper(final IssuanceExtraMessage issuanceExtraMessage) {
    super(new ObjectFactory().createIssuanceExtraMessage(issuanceExtraMessage),
        issuanceExtraMessage.getParameter());
  }

  // private ParameterListWrapper(JAXBElement<?> jaxbElement) throws SerializationException {
  // this.parameterList = verifyTypeOfJaxbElement(jaxbElement);
  // }


  public IssuanceExtraMessageWrapper(final List<BigInt> issuanceMessageElements) {
    this();
    int i = 0;
    for (final BigInt value : issuanceMessageElements) {
      setParameter(Integer.toString(i), value);
      i++;
    }
  }



}
