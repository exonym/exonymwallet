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
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationStateWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.RevocationAuthorityConfiguration;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationLogEntry;
import eu.abc4trust.xml.RevocationState;

/**
 * 
 */
public class RevocationInformationFacade extends ParametersHelper {

  // // Delegatee element names
  // private static final String CREATED_DATE = "createdDate";
  // private static final String EXPIRY_DATE = "expiryDate";

  // Delegatee
  private final JAXBElement<RevocationInformation> delegatee;


  public RevocationInformationFacade() {
    this(new ObjectFactory().createRevocationInformation());
  }

  public RevocationInformationFacade(final RevocationInformation delegatee) {
    this.delegatee = new ObjectFactory().createRevocationInformation(delegatee);
  }

  private RevocationInformationFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.delegatee = verifyTypeOfJaxbElement(jaxbElement);
  }

  /**
   * @deprecated Use the constructor {@link RevocationInformationFacade(RevocationState
   *             revocationState, RevocationHistory revocationHistory)} instead.
   */
  @Deprecated
  public RevocationInformationFacade(final URI revocationInformationId,
                                     final URI revocationAuthorityParametersId,
                                     final RevocationState revocationState,
      final RevocationHistory revocationHistory) throws ConfigurationException {
    this();

    setRevocationInformationId(revocationInformationId);
    setRevocationAuthorityParametersId(revocationAuthorityParametersId);

    // this is very similar to NonRevocationEvidence code...
    final Calendar now =
        RevocationAuthorityConfiguration.now(RevocationAuthorityConfiguration
            .getCreationDateGranularity());
    setCreated(now);
    now.roll(RevocationAuthorityConfiguration.getRevocationInformationTimeToLive(), true);
    setExpires(now);

    setRevocationState(revocationState);
    setRevocationHistory(revocationHistory);
  }


  public RevocationInformationFacade(final RevocationState revocationState,
                                     final RevocationHistory revocationHistory) throws ConfigurationException {
    this();

    final ClRevocationStateWrapper revocationStateWrapper = new ClRevocationStateWrapper(revocationState);
    final RevocationHistoryFacade revocationHistoryFacade =
        new RevocationHistoryFacade(revocationHistory);

    // make sure the state is consistent with the history
    final URI raParametersUID =
        RevocationAuthorityParametersFacade
            .getRevocationAuthorityParametersUID(revocationStateWrapper.getRaPublicKeyId());
    assert (raParametersUID.equals(revocationHistoryFacade.getRevocationAuthorityParametersId()));
    assert (revocationStateWrapper.getEpoch().equals(revocationHistoryFacade.getEpoch()));

    setRevocationAuthorityParametersId(raParametersUID);

    // this is very similar to NonRevocationEvidence code...
    // Calendar now =
    // RevocationAuthorityConfiguration.now(RevocationAuthorityConfiguration
    // .getCreationDateGranularity());
    // setCreated(now);
    final Calendar date = revocationStateWrapper.getLastChangeDate().toGregorianCalendar();
    setCreated(date);
    date.roll(RevocationAuthorityConfiguration.getRevocationInformationTimeToLive(), true);
    setExpires(date);

    setRevocationState(revocationState);
    // also sets the revocation information uid
    setRevocationHistory(revocationHistory);
    // setRevocationInformationId(getRevocationInformationUID(raParametersUID,
    // revocationStateWrapper.getEpoch()));
  }



  public URI getRevocationInformationId() {
    return getDelegateeElement().getRevocationInformationUID();
  }

  public void setRevocationInformationId(final URI revocationInformationId) {
    getDelegateeElement().setRevocationInformationUID(revocationInformationId);
  }

  public URI getRevocationAuthorityParametersId() throws ConfigurationException {
    return getDelegateeElement().getRevocationAuthorityParametersUID();
  }

  public void setRevocationAuthorityParametersId(final URI revocationAuthorityParametersId) {
    getDelegateeElement().setRevocationAuthorityParametersUID(revocationAuthorityParametersId);
  }

  public Calendar getCreated() {
    return getDelegateeElement().getCreated();
  }

  public void setCreated(final Calendar creationDate) {
    getDelegateeElement().setCreated(creationDate);
  }

  public Calendar getExpires() {
    return getDelegateeElement().getExpires();
  }

  public void setExpires(final Calendar expirationDate) {
    getDelegateeElement().setExpires(expirationDate);
  }

  // public XMLGregorianCalendar getCreationDate() throws ConfigurationException {
  //
  // String eventDateString = (String) getParameter(CREATED_DATE);
  // return EncodeDecode.decodeDateFromString(eventDateString);
  // }
  //
  // public void setCreationDate(XMLGregorianCalendar creationDate) {
  //
  // String dateString = EncodeDecode.encodeDateAsString(creationDate);
  // setParameter(CREATED_DATE, dateString);
  // }
  //
  // public XMLGregorianCalendar getExpiryDate() throws ConfigurationException {
  //
  // String dateString = (String) getParameter(EXPIRY_DATE);
  // return EncodeDecode.decodeDateFromString(dateString);
  // }
  //
  // public void setExpiryDate(XMLGregorianCalendar expiryDate) {
  //
  // String dateString = EncodeDecode.encodeDateAsString(expiryDate);
  // setParameter(EXPIRY_DATE, dateString);
  // }


  // // TODO same code in ClRevocationStateWrapper and RevocationInformationFacade
  // private static XMLGregorianCalendar now() {
  // try {
  // return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
  // } catch (DatatypeConfigurationException e) {
  // throw new RuntimeException(e);
  // }
  // }

  public RevocationState getRevocationState() {
    final List<Object> listOfObjects = getCryptoParamsList();
    for (final Object object : listOfObjects) {
      final Object containedObject = JAXBIntrospector.getValue(object);

      if (RevocationState.class.isAssignableFrom(containedObject.getClass())) {
        return (RevocationState) containedObject;
      }
    }
    return null;
  }

  // TODO this is the same as in CredentialFacade
  private <T> void replaceContainedObject(final Class<T> type, final JAXBElement<T> object) {
    final List<Object> cryptoParameterList = getCryptoParamsList();
    for (final Object jaxbObject : cryptoParameterList) {
      final Object containedObject = JAXBIntrospector.getValue(jaxbObject);

      if (type.isAssignableFrom(containedObject.getClass())) {
        cryptoParameterList.remove(jaxbObject);
        break;
      }
    }
    cryptoParameterList.add(object);
  }

  // TODO used in RevocationMessageFacade and CredentialFacade (should be used in a general facade)
  @SuppressWarnings("unchecked")
  private <T> T getContainedObject(final Class<T> type) {
    final List<Object> cryptoParameterList = getCryptoParamsList();
    for (final Object object : cryptoParameterList) {
      final Object containedObject = JAXBIntrospector.getValue(object);

      if (type.isAssignableFrom(containedObject.getClass())) {
        return (T) containedObject;
      }
    }
    return null;
  }

  public void setRevocationState(final RevocationState revocationState) {

    replaceContainedObject(RevocationState.class,
        new ObjectFactory().createRevocationState(revocationState));
    // getCryptoParamsList().add(new ObjectFactory().createRevocationState(revocationState));
  }

  public RevocationHistory getRevocationHistory() {
    // List<Object> listOfObjects = getCryptoParamsList();
    // for (Object object : listOfObjects) {
    // Object containedObject = JAXBIntrospector.getValue(object);
    //
    // if (RevocationHistory.class.isAssignableFrom(containedObject.getClass())) {
    // return (RevocationHistory) containedObject;
    // }
    // }
    // return null;
    return getContainedObject(RevocationHistory.class);
  }

  public void setRevocationHistory(final RevocationHistory revocationHistory) {

    final RevocationHistoryFacade revocationHistoryFacade =
        new RevocationHistoryFacade(revocationHistory);

    // update the revocation information according to the history
    try {
      setRevocationInformationId(getRevocationInformationUID(
          revocationHistoryFacade.getRevocationAuthorityParametersId(),
          revocationHistoryFacade.getLatestEpoch()));
    } catch (final ConfigurationException e) {
      if (Configuration.printStackTraces()) e.printStackTrace();
      throw new RuntimeException(e);
    }

    replaceContainedObject(RevocationHistory.class,
        new ObjectFactory().createRevocationHistory(revocationHistory));
    // getCryptoParamsList().add(new ObjectFactory().createRevocationHistory(revocationHistory));
  }


  public List<Object> getCryptoParamsList() {
    CryptoParams cryptoParams = getDelegateeElement().getCryptoParams();
    if (cryptoParams == null) {
      final ObjectFactory objectFactory = new ObjectFactory();
      cryptoParams = objectFactory.createCryptoParams();
      getDelegateeElement().setCryptoParams(cryptoParams);
    }
    XmlUtils.fixNestedContent(cryptoParams);
    return cryptoParams.getContent();
  }


  public RevocationInformation getDelegateeElement() {
    return (RevocationInformation) JAXBIntrospector.getValue(delegatee);
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<RevocationInformation> verifyTypeOfJaxbElement(
    final JAXBElement<?> jaxbElement) throws SerializationException {
    final Class<?> delegateeClass = NonRevocationEvidence.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<RevocationInformation>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  public static RevocationInformationFacade deserialize(final String issuerParameters)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(issuerParameters);

    return new RevocationInformationFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return parameterName;
  }

  /**
   * @return
   */
  public List<RevocationLogEntry> getRevocationLogEntries() {

    final RevocationHistoryFacade revocationHistoryFacade =
        new RevocationHistoryFacade(getRevocationHistory());
    return revocationHistoryFacade.getRevocationLogEntries();
  }


  public static URI getRevocationInformationUID(final URI raParametersUID, final int epoch) {
    return URI.create(raParametersUID.toString() + ":revocationInformation:"
        + String.valueOf(epoch));
  }
}
