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

package com.ibm.zurich.idmx.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

//import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

import eu.abc4trust.xml.BigIntegerParameter;
import eu.abc4trust.xml.Parameter;

/**
 * 
 */
public class ParameterListWrapper<T> extends ParametersHelper {

  // Delegatee
  private final JAXBElement<T> delegatee;


  /**
   * Constructor.
   */
  public ParameterListWrapper(final JAXBElement<T> delegatee, final List<Parameter> delegateeParameterList) {
    this.delegatee = delegatee;
    this.parameterListHelper = new ParameterListHelper(delegateeParameterList);
  }


  public List<BigInt> getParameterListElements() {
    final List<BigInt> bigIntList = new ArrayList<BigInt>();
    final List<BigIntegerParameter> parameterList =
        parameterListHelper.extractElements(BigIntegerParameter.class);
    for (final BigIntegerParameter parameter : parameterList) {
      final String parameterName = parameter.getName();
      try {
        bigIntList.add((BigInt) parameterListHelper
            .getParameterValueUsingParameterName(parameterName));
      } catch (ConfigurationException e) {
        throw new RuntimeException(e);
      }
    }
    return bigIntList;
  }



  @SuppressWarnings("unchecked")
  public T getDelegatee() {
    final Object toCast = JAXBIntrospector.getValue(delegatee);
    return (T)toCast;
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return parameterName;
  }



  /**
   * @param jaxbElement
   * @throws SerializationException
   */
//  private <T> JAXBElement<T> verifyTypeOfJaxbElement(final JAXBElement<?> jaxbElement)
//      throws SerializationException {
//    final Class<?> delegateeClass = delegatee.getClass();
//    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
//      return (JAXBElement<T>) jaxbElement;
//    } else {
//      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
//    }
//  }


  // public static ParameterListWrapper deserialize(String parameterList)
  // throws SerializationException, ConfigurationException {
  //
  // JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(parameterList);
  //
  // return new ParameterListWrapper(jaxbElement);
  // }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }

}
