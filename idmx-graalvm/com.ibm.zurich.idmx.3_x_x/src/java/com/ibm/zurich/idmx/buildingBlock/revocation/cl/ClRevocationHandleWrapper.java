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

package com.ibm.zurich.idmx.buildingBlock.revocation.cl;

import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.RevocationHandle;
import eu.abc4trust.xml.RevocationState;

/**
 * 
 */
public class ClRevocationHandleWrapper extends ParametersHelper {

  // Delegatee element names
  private static final String REVOCATION_HANDLE = "revocationHandle";
  private static final String NON_REVOCATION_EVIDENCE = "nre";
  private static final String REVOCATION_INFORMATION = "revocationInformation";


  // Delegatee
  private final JAXBElement<RevocationHandle> delegatee;

  public ClRevocationHandleWrapper() {
    this(new ObjectFactory().createRevocationHandle());
  }

  public ClRevocationHandleWrapper(final RevocationHandle delegatee) {
    this.delegatee = new ObjectFactory().createRevocationHandle(delegatee);
    this.parameterListHelper = new ParameterListHelper(getDelegateeValue().getParameter());
  }

  @SuppressWarnings("unchecked")
  private ClRevocationHandleWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = RevocationState.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.delegatee = (JAXBElement<RevocationHandle>) jaxbElement;
      this.parameterListHelper = new ParameterListHelper(getDelegateeValue().getParameter());
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  public ClRevocationHandleWrapper(final BigInt revocationHandle, final NonRevocationEvidence nre,
                                   final URI revocationInformation) {
    this();

    setRevocationHandle(revocationHandle);
    setNonRevocationEvidence(nre);
    setRevocationInformation(revocationInformation);
  }



  public BigInt getRevocationHandle() throws ConfigurationException {
    return (BigInt) super.getParameter(REVOCATION_HANDLE);
  }

  public void setRevocationHandle(final BigInt revocationHandle) {
    super.setParameter(REVOCATION_HANDLE, revocationHandle);
  }

  public BigInt getRevocationInformation() throws ConfigurationException {
    return (BigInt) super.getParameter(REVOCATION_INFORMATION);
  }

  public void setRevocationInformation(final URI revocationInformation) {
    super.setParameter(REVOCATION_INFORMATION, revocationInformation);
  }

  public NonRevocationEvidence getNonRevocationEvidence() throws ConfigurationException {
    return (NonRevocationEvidence) getParameter(NON_REVOCATION_EVIDENCE);
  }

  public void setNonRevocationEvidence(NonRevocationEvidence nre) {
    setParameter(NON_REVOCATION_EVIDENCE, nre);
  }


  // TODO remove duplicate code (this is the same as the system parameters use)
  private List<Object> getCryptoParameterList() {
    CryptoParams cryptoParams = getDelegateeValue().getCryptoParams();
    if (cryptoParams == null) {
      cryptoParams = new ObjectFactory().createCryptoParams();
      getDelegateeValue().setCryptoParams(cryptoParams);
    }
    XmlUtils.fixNestedContent(cryptoParams);
    return cryptoParams.getContent();
  }

  // TODO remove duplicate code (this is the same as the system parameters use - note that the
  // name composition changes!!!)
  @Override
public void setParameter(final String parameterName, final Object parameterValue) {
    final Parameter parameter = ParameterListHelper.createParameter(parameterName, parameterValue);
    final JAXBElement<Parameter> ofParameter = new ObjectFactory().createParameter(parameter);
    getCryptoParameterList().add(ofParameter);
  }

  // TODO remove duplicate code (this is the same as the system parameters use - note that the
  // name composition changes!!!)
  @Override
public Object getParameter(final String parameterName) throws ConfigurationException {
    final List<Parameter> listOfParameters =
        ParameterListHelper.extractElements(getCryptoParameterList(), Parameter.class);
    return new ParameterListHelper(listOfParameters)
        .getParameterValueUsingParameterName(parameterName);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return parameterName;
  }

  /**
   * Returns the delegatee (JAXB object).
   */
  public RevocationHandle getDelegateeValue() {
    return (RevocationHandle) JAXBIntrospector.getValue(delegatee);
  }

  /**
   * Serializes the stored template using JAXB.
   * 
   * @param filename
   * @throws SerializationException
   */
  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }

  /**
   * De-serializes an issuer public key template formatted as a string into a JAXB object.
   * 
   * @throws SerializationException
   */
  public static ClRevocationHandleWrapper deserialize(final String delegatee)
      throws SerializationException {
    return new ClRevocationHandleWrapper(JaxbHelperClass.deserialize(delegatee));
  }
}
