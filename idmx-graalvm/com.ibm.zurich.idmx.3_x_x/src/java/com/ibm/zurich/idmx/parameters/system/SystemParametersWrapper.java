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

package com.ibm.zurich.idmx.parameters.system;

import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.AbcUriConfigurator;
import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.configuration.ParameterBaseName;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.SystemParameters;

/**
 * 
 */
public class SystemParametersWrapper {

  // System parameter element names
  private static final String SYSTEM_PARAMETERS_ID_NAME = "spId";
  private static final String HASH_FUNCTION_NAME = "hashFunction";

  // Delegatee
  private final JAXBElement<SystemParameters> systemParameters;

  public SystemParametersWrapper() {
    this(new ObjectFactory().createSystemParameters());
  }

  public SystemParametersWrapper(final SystemParameters systemParameters) {
    this.systemParameters = new ObjectFactory().createSystemParameters(systemParameters);
  }

  @SuppressWarnings("unchecked")
  private SystemParametersWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = SystemParameters.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.systemParameters = (JAXBElement<SystemParameters>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  public String getImplementationVersion() {
    return getSystemParameters().getVersion();
  }

  public void setImplementationVersion(final String version) {
    getSystemParameters().setVersion(version);
  }

  public URI getSystemParametersId() throws ConfigurationException {
    return (URI) getParameter(SYSTEM_PARAMETERS_ID_NAME);
  }

  public void setSystemParametersId(final URI systemParametersId) {
    setParameter(SYSTEM_PARAMETERS_ID_NAME, systemParametersId);
    // May be overwitten
    getSystemParameters().setSystemParametersUID(systemParametersId);
  }

  public String getHashFunction() throws ConfigurationException {
    final String hashFunctionUri = (String) getParameter(HASH_FUNCTION_NAME);
    return AbcUriConfigurator.removeBasicUri(hashFunctionUri);
  }

  public void setHashFunction(final String hashFunctionName) {
    final String hashFunctionUri = AbcUriConfigurator.prependBasicUri(hashFunctionName);
    setParameter(HASH_FUNCTION_NAME, hashFunctionUri);
  }

  private List<Object> getCryptoParameterList() {
    CryptoParams cryptoParams = getSystemParameters().getCryptoParams();
    if (cryptoParams == null) {
      cryptoParams = new ObjectFactory().createCryptoParams();
      getSystemParameters().setCryptoParams(cryptoParams);
    }
    XmlUtils.fixNestedContent(cryptoParams);
    return cryptoParams.getContent();
  }

  /**
   * Creates a parameter and adds it to the list of parameters in the system parameters.
   * 
   */
  public void setParameter(final String parameterName, final Object parameterValue) {
    final Parameter parameter =
        ParameterListHelper.createParameter(
            ParameterBaseName.systemParametersParameterName(parameterName), parameterValue);
    final JAXBElement<Parameter> ofParameter = new ObjectFactory().createParameter(parameter);
    getCryptoParameterList().add(ofParameter);
  }

  /**
   * Retrieves a parameter using the parameter's name.
   * 
   * @throws ConfigurationException
   */
  public Object getParameter(final String parameterName) throws ConfigurationException {
    final List<Parameter> listOfParameters =
        ParameterListHelper.extractElements(getCryptoParameterList(), Parameter.class);
    return new ParameterListHelper(listOfParameters)
        .getParameterValueUsingParameterName(ParameterBaseName
            .systemParametersParameterName(parameterName));
  }

  /**
   * Computes a cryptographic hash of the system parameters
   * 
   * @throws ConfigurationException
   */
  public byte[] getHash(final String hashAlgorithm) throws ConfigurationException {
    try {
      final byte[] toHash = JaxbHelperClass.canonicalXml(systemParameters);
      final MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
      md.update(toHash);
      final byte[] digest = md.digest();
      return digest;
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new ConfigurationException(e);
    }
  }

  /**
   * @return
   */
  public SystemParameters getSystemParameters() {
    return (SystemParameters) JAXBIntrospector.getValue(systemParameters);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(systemParameters);
  }

  public static SystemParametersWrapper deserialize(final String systemParameters)
      throws SerializationException {
    return new SystemParametersWrapper(JaxbHelperClass.deserialize(systemParameters));
  }

  public static SystemParametersWrapper deserialize(final InputStream inputStream)
      throws SerializationException {
    return new SystemParametersWrapper(JaxbHelperClass.deserialize(inputStream));
  }
}
