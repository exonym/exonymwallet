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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;

import eu.abc4trust.xml.BigIntegerParameter;
import eu.abc4trust.xml.IntegerParameter;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.StringParameter;
import eu.abc4trust.xml.UriParameter;

/**
 * 
 */
public class ParameterListHelper {

  // TODO(enr): FIXME Temporary solution to preserve method signatures.
  private static final BigIntFactory bigIntFactory = new BigIntFactoryImpl();

  private final List<Parameter> parameterList;


  public ParameterListHelper(List<Parameter> parameterList) {
    this.parameterList = parameterList;
  }

  public List<Parameter> getParameterList() {
    return parameterList;
  }


  /**
   * Returns a new list of the elements of a specified type <tt>type</tt> from a given list
   * <tt>listOfElements</tt>.
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> extractElements(final Class<T> type) {
    final List<T> result = new ArrayList<T>();
    for (final Object e : parameterList) {
      final Object object = JAXBIntrospector.getValue(e);
      if (type.isAssignableFrom(object.getClass())) {
        result.add((T) object);
      }
    }
    return result;
  }

  /**
   * Returns a new list of the elements of a specified type <tt>type</tt> from a given list
   * <tt>listOfElements</tt>.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> extractElements(final List<?> listOfElements, final Class<T> type) {
    final List<T> result = new ArrayList<T>();
    for (final Object e : listOfElements) {
      final Object object = JAXBIntrospector.getValue(e);
      if (type.isAssignableFrom(object.getClass())) {
        result.add((T) object);
      }
    }
    return result;
  }

  /**
   * Returns the parameter value of the given parameter list.
   * 
   * @throws ConfigurationException
   */
  public Object getParameterValueUsingParameterName(final String parameterName)
      throws ConfigurationException {

    final int parameterIndex = getParameterIndexUsingParameterName(parameterName);

    if (parameterIndex >= 0 && parameterIndex < parameterList.size()) {
      final Parameter parameter = parameterList.get(parameterIndex);
      return getParameterValue(parameter);

    } else {
      throw new ConfigurationException(ErrorMessages.parameterNotFound(parameterName));
    }
  }

  public boolean parameterIsInList(final String parameterName) {
    return -1 != getParameterIndexUsingParameterName(parameterName);
  }

  /**
   * Returns the index of the parameter with the given parameter name within the given list or -1 if
   * the parameter in not contained in the list.
   * 
   * @param parameterName
   * @return
   */
  public int getParameterIndexUsingParameterName(final String parameterName) {

    for (int i = 0; i < parameterList.size(); i++) {
      if (parameterList.get(i).getName().equals(parameterName)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the value of the parameter or null in case the parameter type does not match a known
   * parameter type.
   * 
   * @throws ConfigurationException
   */
  private Object getParameterValue(final Parameter parameter) throws ConfigurationException {

    if (parameter instanceof IntegerParameter) {
      return ((IntegerParameter) parameter).getValue();
    } else if (parameter instanceof BigIntegerParameter) {
      return bigIntFactory.valueOf(((BigIntegerParameter) parameter).getValue());
    } else if (parameter instanceof StringParameter) {
      return ((StringParameter) parameter).getValue();
    } else if (parameter instanceof UriParameter) {
      return ((UriParameter) parameter).getValue();
    }
    throw new ConfigurationException("Idemix: Unknown parameter type encountered.");
  }

  /**
   * Creates a parameter that may be added to a list of parameters.
   * 
   */
  public static Parameter createParameter(String parameterUri, Object parameterValue) {

    final Parameter parameter;
    if (parameterValue instanceof Integer) {
      parameter = ParameterListHelper.getIntegerParameter(parameterUri, (Integer) parameterValue);

    } else if (parameterValue instanceof BigInt) {
      parameter =
          ParameterListHelper.getBigIntegerParameter(parameterUri,
              ((BigInt) parameterValue).getValue());

    } else if (parameterValue instanceof String) {
      parameter = ParameterListHelper.getStringParameter(parameterUri, (String) parameterValue);

    } else if (parameterValue instanceof URI) {
      parameter = ParameterListHelper.getUriParameter(parameterUri, (URI) parameterValue);
    } else {
      parameter = null;
    }
    return parameter;
  }

  /**
   * Returns a JAXB parameter built from the given name and value.
   * 
   */
  public static IntegerParameter getIntegerParameter(final String parameterName, final int parameterValue) {
    final IntegerParameter parameter = new ObjectFactory().createIntegerParameter();
    parameter.setName(parameterName);
    parameter.setValue(parameterValue);
    return parameter;
  }

  /**
   * Returns a JAXB parameter built from the given name and value.
   * 
   */
  public static BigIntegerParameter getBigIntegerParameter(final String parameterName,
                                                           final BigInteger parameterValue) {
    final BigIntegerParameter parameter = new ObjectFactory().createBigIntegerParameter();
    parameter.setName(parameterName);
    parameter.setValue(parameterValue);
    return parameter;
  }

  /**
   * Returns a JAXB parameter built from the given name and value.
   * 
   */
  public static StringParameter getStringParameter(final String parameterName, final String parameterValue) {
    final StringParameter parameter = new ObjectFactory().createStringParameter();
    parameter.setName(parameterName);
    parameter.setValue(parameterValue);
    return parameter;
  }

  /**
   * Returns a JAXB parameter built from the given name and value.
   * 
   */
  public static UriParameter getUriParameter(final String parameterName, final URI parameterValue) {
    final UriParameter parameter = new ObjectFactory().createUriParameter();
    parameter.setName(parameterName);
    parameter.setValue(parameterValue);
    return parameter;
  }

}
