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

package com.ibm.zurich.idmix.abc4trust.facades;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.RevocationHandle;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationMessage;

/**
 * 
 */
public class RevocationMessageFacade implements Constants {


  // private final String CONTEXT = "context";
  // private final String COMMAND = "command";

  private final static String REQUEST_REVOCATION_HANDLE = "requestRevocationHandle";
  private final static String REQUEST_REVOCATION_INFORMATION = "requestRevocationInformation";
  private final static String GET_CURRENT_REVOCATION_INFORMATION = "getCurrentRevocationInformation";
  private final static String UPDATE_REVOCATION_EVIDENCE = "updateRevocationEvidence";

  private final String REVOCATION_INFORMATION_ID = "revocationInformationId";

  // Delegatee
  private final JAXBElement<RevocationMessage> delegatee;



  public RevocationMessageFacade() {
    this(new ObjectFactory().createRevocationMessage());
  }

  public RevocationMessageFacade(final RevocationMessage delegatee) {
    this.delegatee = new ObjectFactory().createRevocationMessage(delegatee);
  }

  private RevocationMessageFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.delegatee = verifyTypeOfJaxbElement(jaxbElement);
  }

  public RevocationMessage getDelegateeValue() {
    return (RevocationMessage) JAXBIntrospector.getValue(delegatee);
  }



  public void setContext(final URI context) {
    getDelegateeValue().setContext(context);
  }

  public URI getContext() throws ConfigurationException {
    return getDelegateeValue().getContext();
  }

  public void setRevocationAuthorityParametersUID(final URI revocationAuthorityParamtersId) {
    getDelegateeValue().setRevocationAuthorityParametersUID(revocationAuthorityParamtersId);
  }

  public URI getRevocationAuthorityParametersUID() throws ConfigurationException {
    return getDelegateeValue().getRevocationAuthorityParametersUID();
  }

  public void setRevocationInformationUID(final URI revocationInformationId) {
    setParameter(REVOCATION_INFORMATION_ID, revocationInformationId);
  }

  public URI getRevocationInformationUID() throws ConfigurationException {
    return (URI) getParameter(REVOCATION_INFORMATION_ID);
  }
  
  /**
   * Sets the command of the revocation message that is queried by the revocation proxy of the other
   * side.
   */
  private void setCommand(final String commandName) {
    getDelegateeValue().setRevocationMessageType(commandName);
  }

  private boolean commandEquals(final String commandName) {
    return getDelegateeValue().getRevocationMessageType().equals(commandName);
  }

  public void setRequestRevocationHandle() {
    setCommand(REQUEST_REVOCATION_HANDLE);
  }

  public boolean revocationHandleRequested() {
    return commandEquals(REQUEST_REVOCATION_HANDLE);
  }

  public void setRequestRevocationInformation() {
    setCommand(REQUEST_REVOCATION_INFORMATION);
  }

  public boolean revocationInformationRequested() {
    return commandEquals(REQUEST_REVOCATION_INFORMATION);
  }

  public void setRequestLatestRevocationInformation() {
    setCommand(GET_CURRENT_REVOCATION_INFORMATION);
  }

  public boolean getCurrentRevocationInformation() {
    return commandEquals(GET_CURRENT_REVOCATION_INFORMATION);
  }

  public void setUpdateRevocationEvidence() {
    setCommand(UPDATE_REVOCATION_EVIDENCE);
  }

  public boolean updateRevocationEvidence() {
    return commandEquals(UPDATE_REVOCATION_EVIDENCE);
  }



  @SuppressWarnings("unchecked")
  public List<Attribute> getAttributeList() {
    final JAXBElement<Attribute> jaxb = (JAXBElement<Attribute>) getCryptoParameterList().get(1);
    final List<Attribute> attributes = new LinkedList<Attribute>();
    attributes.add(jaxb.getValue());
    return null;
  }



  public void setCryptoParams(final CryptoParams cryptoParams) {
    // getCryptoParameterList().add(cryptoParams);
    getDelegateeValue().setCryptoParams(cryptoParams);
    // Object object = cryptoParams.getAny().get(0);
    // if (cryptoParams.getAny().get(0) instanceof RevocationHandle) {
    // setRevocationHandle((RevocationHandle) object);
    // }
  }

  // TODO used in RevocationMessageFacade and CredentialFacade (should be used in a general facade)
  @SuppressWarnings("unchecked")
  public <T> T getContainedObject(final Class<T> type) {
    final List<Object> cryptoParameterList = getCryptoParameterList();
    for (final Object object : cryptoParameterList) {
      Object containedObject = JAXBIntrospector.getValue(object);

      if (type.isAssignableFrom(containedObject.getClass())) {
        return (T) containedObject;
      }
    }
    return null;
  }

  public NonRevocationEvidence getNonRevocationEvidence() throws ConfigurationException {
    return getContainedObject(NonRevocationEvidence.class);
  }

  public void setNonRevocationEvidence(final NonRevocationEvidence nonRevocationEvidence) {
    getCryptoParameterList().add(
        new ObjectFactory().createNonRevocationEvidence(nonRevocationEvidence));
  }

  public RevocationHandle getRevocationHandle() throws ConfigurationException {
    return getContainedObject(RevocationHandle.class);
  }

  public void setRevocationHandle(final RevocationHandle revocationHandle) {
    getCryptoParameterList().add(new ObjectFactory().createRevocationHandle(revocationHandle));
  }

  public List<Object> getAdditionalObjectList() {
    return getCryptoParameterList();
  }
 
  public RevocationInformation getRevocationInformation() throws ConfigurationException {
    return getContainedObject(RevocationInformation.class);
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
  public void setParameter(final String parameterName, final Object parameterValue) {
    final Parameter parameter = ParameterListHelper.createParameter(parameterName, parameterValue);
    final JAXBElement<Parameter> ofParameter = new ObjectFactory().createParameter(parameter);
    getCryptoParameterList().add(ofParameter);
  }

  // TODO remove duplicate code (this is the same as the system parameters use - note that the
  // name composition changes!!!)
  public Object getParameter(final String parameterName) throws ConfigurationException {
    final List<Parameter> listOfParameters =
        ParameterListHelper.extractElements(getCryptoParameterList(), Parameter.class);
    return new ParameterListHelper(listOfParameters)
        .getParameterValueUsingParameterName(parameterName);
  }


  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<RevocationMessage> verifyTypeOfJaxbElement(final JAXBElement<?> jaxbElement)
      throws SerializationException {
    final Class<?> delegateeClass = RevocationMessage.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<RevocationMessage>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  public static RevocationMessageFacade deserialize(final String delegatee)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(delegatee);
    return new RevocationMessageFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }



}
