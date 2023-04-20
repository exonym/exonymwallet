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

package com.ibm.zurich.idmx.parameters.verifier;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.configuration.ParameterBaseName;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

/**
 * 
 */
public class VerifierParametersWrapper {

  // Verifier parameters element names
  private static final String VERIFIER_PARAMETERS_ID_NAME = "vp";

  // Delegatee
  private final JAXBElement<VerifierParameters> verifierParameters;

  public VerifierParametersWrapper() {
    this(new ObjectFactory().createVerifierParameters());
  }

  public VerifierParametersWrapper(final VerifierParameters verifierParameters) {
    this.verifierParameters = new ObjectFactory().createVerifierParameters(verifierParameters);
  }

  @SuppressWarnings("unchecked")
  private VerifierParametersWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = SystemParameters.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.verifierParameters = (JAXBElement<VerifierParameters>) jaxbElement;
    } else {
      throw new SerializationException("Idmx: " + delegateeClass.getSimpleName()
          + " are malformed.");
    }
  }

  public String getVerifierParametersId() throws ConfigurationException {
    return (String) getParameter(VERIFIER_PARAMETERS_ID_NAME);
  }

  public void setVerifierParametersId(String verifierParametersId) {
    setParameter(VERIFIER_PARAMETERS_ID_NAME, verifierParametersId);
  }

  /**
   * Creates a parameter and adds it to the list of parameters in the template.
   * 
   * @param parameterName
   * @param parameterValue
   */
  public void setParameter(final String parameterName, final Object parameterValue) {
    final Parameter parameter =
        ParameterListHelper.createParameter(
            ParameterBaseName.systemParametersParameterName(parameterName), parameterValue);
    // JAXBElement<Parameter> ofParameter = new ObjectFactory().createParameter(parameter);
    getCryptoParameterList().add(parameter);
  }

  /**
   * Retrieves a parameter using the parameter's name.
   * 
   * @param parameterName
   * @param parameterValue
   * @throws ConfigurationException
   */
  public Object getParameter(final String parameterName) throws ConfigurationException {
    final List<Parameter> listOfParameters =
        ParameterListHelper.extractElements(getCryptoParameterList(), Parameter.class);
    return new ParameterListHelper(listOfParameters)
        .getParameterValueUsingParameterName(ParameterBaseName
            .systemParametersParameterName(parameterName));
  }

  private List<Object> getCryptoParameterList() {
    CryptoParams cryptoParams = getVerifierParameters().getCryptoParams();
    if (cryptoParams == null) {
      cryptoParams = new ObjectFactory().createCryptoParams();
      getVerifierParameters().setCryptoParams(cryptoParams);
    }
    XmlUtils.fixNestedContent(cryptoParams);
    return cryptoParams.getContent();
  }

  /**
   * @return
   */
  public VerifierParameters getVerifierParameters() {
    return (VerifierParameters) JAXBIntrospector.getValue(verifierParameters);
  }

  /**
   * Computes a cryptographic hash of the system parameters
   * 
   * @throws ConfigurationException
   */
  public byte[] getHash(final String hashAlgorithm) throws ConfigurationException {
    try {
      final byte[] toHash = JaxbHelperClass.canonicalXml(verifierParameters);
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

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(verifierParameters);
  }

  public static VerifierParametersWrapper deserialize(final String verifierParameters)
      throws SerializationException {
    return new VerifierParametersWrapper(JaxbHelperClass.deserialize(verifierParameters));
  }

  public static VerifierParametersWrapper deserialize(final InputStream inputStream)
      throws SerializationException {
    return new VerifierParametersWrapper(JaxbHelperClass.deserialize(inputStream));
  }
}
