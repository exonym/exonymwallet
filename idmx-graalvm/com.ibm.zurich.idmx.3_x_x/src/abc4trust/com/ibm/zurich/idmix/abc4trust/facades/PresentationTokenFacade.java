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

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.orchestration.presentation.MechanismSpecificationWrapper;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.MechanismSpecification;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.ZkProof;

/**
 * 
 */
public class PresentationTokenFacade implements Constants {

  // Delegatee
  private final JAXBElement<PresentationToken> presentationToken;



  public PresentationTokenFacade() {
    this(new ObjectFactory().createPresentationToken());
  }

  public PresentationTokenFacade(PresentationToken presentationToken) {
    this.presentationToken = new ObjectFactory().createPresentationToken(presentationToken);
  }

  private PresentationTokenFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.presentationToken = verifyTypeOfJaxbElement(jaxbElement);
  }



  public PresentationTokenDescription getPresentationTokenDescription() {
    return getPresentationToken().getPresentationTokenDescription();
  }

  public void setPresentationTokenDescription(final PresentationTokenDescription ptd) {
    getPresentationToken().setPresentationTokenDescription(ptd);
  }

  public void addMechanismSpecification(final MechanismSpecificationWrapper ms) {
    addElementToIssuanceMessage(MechanismSpecification.class, ms.getMechanismSpecification());
  }

  public MechanismSpecificationWrapper getMechanismSpecification() {
    return new MechanismSpecificationWrapper(
        getElementFromIssuanceMessage(MechanismSpecification.class));
  }

  public ZkProof getZkProof() {
    return getElementFromIssuanceMessage(ZkProof.class);
  }

  public void addZkProof(final ZkProof zkp) {
    addElementToIssuanceMessage(ZkProof.class, zkp);
  }


  // TODO remove duplicate method - (duplicate method also used in other facades with an ANY
  // element)
  private <T> void addElementToIssuanceMessage(final Class<T> elementClass, final T element) {
    final T object = getElementFromIssuanceMessage(elementClass);
    // remove previously stored object of the same type
    if (object != null) {
      getExtensionList().remove(object);
    }
    // add new object to the list of elements
    if (elementClass.equals(ZkProof.class)) {
      getExtensionList().add(new ObjectFactory().createZkProof((ZkProof) element));
    } else if (elementClass.equals(MechanismSpecification.class)) {
      getExtensionList().add(
          new ObjectFactory().createMechanismSpecification((MechanismSpecification) element));
    } else {
      getExtensionList().add(element);
    }
  }

  // TODO remove duplicate method - (duplicate method also used in other facades with an ANY
  // element)
  @SuppressWarnings("unchecked")
  private <T> T getElementFromIssuanceMessage(final Class<T> elementClass) {
    for (final Object object : getExtensionList()) {
      if (object != null
          && JAXBIntrospector.getValue(object).getClass().isAssignableFrom(elementClass)) {
        return (T) JAXBIntrospector.getValue(object);
      }
    }
    return null;
  }

  // TODO remove duplicate method - (duplicate method also used in other facades with an ANY
  // element)
//  private boolean removeElementFromIssuanceMessage(final Class<?> elementClass) {
//    final Object object = getElementFromIssuanceMessage(elementClass);
//    if (object != null) {
//      getExtensionList().remove(object);
//      return true;
//    }
//    return false;
//  }



  public PresentationToken getPresentationToken() {
    return (PresentationToken) JAXBIntrospector.getValue(presentationToken);
  }

  private List<Object> getExtensionList() {
    CryptoParams cryptoParameters = getPresentationToken().getCryptoEvidence();
    if (cryptoParameters == null) {
      final ObjectFactory objectFactory = new ObjectFactory();
      cryptoParameters = objectFactory.createCryptoParams();
      getPresentationToken().setCryptoEvidence(cryptoParameters);
    }

    XmlUtils.fixNestedContent(getPresentationToken().getCryptoEvidence());
    return getPresentationToken().getCryptoEvidence().getContent();
  }


  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<PresentationToken> verifyTypeOfJaxbElement(final JAXBElement<?> jaxbElement)
      throws SerializationException {
    final Class<?> delegateeClass = PresentationToken.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<PresentationToken>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }


  public static PresentationTokenFacade deserialize(final String presentationToken)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(presentationToken);

    return new PresentationTokenFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(presentationToken);
  }

  public void setVersion(final String implementationVersion) {
    getPresentationToken().setVersion(implementationVersion);
  }
  
  public String getVersion() {
    return getPresentationToken().getVersion();
  }

}
