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

import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationEventWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeInLogEntry;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationEvent;
import eu.abc4trust.xml.RevocationLogEntry;

/**
 * 
 */
public class RevocationLogEntryFacade implements Constants {

  // Delegatee
  private RevocationLogEntry delegatee;



  public RevocationLogEntryFacade() {
    this.delegatee = new ObjectFactory().createRevocationLogEntry();
  }

  public RevocationLogEntryFacade(final RevocationLogEntry revocationLogEntry) {
    this.delegatee = revocationLogEntry;
  }

  public RevocationLogEntryFacade(final URI revocationEventId, final AttributeInLogEntry attributeInLogEntry,
                                  final Calendar eventDate, final boolean revoked) {
    this();

    setRevocationLogEntryId(revocationEventId);
    setRevocableAttribute(attributeInLogEntry);
    // TODO date is not set correctly
    setDateCreated(eventDate);
    setRevoked(revoked);
    setRevocationEvent(null);
  }

  /**
   * @deprecated {@link RevocationLogEntryFacade(NonRevocationEvidenceFacade nreFacade, boolean
   *             revoked)} should be used instead.
   */
  @Deprecated
  public RevocationLogEntryFacade(final RevocationEvent revocationEvent, final boolean revoked)
      throws ConfigurationException {
    this();

    initRevocationLogEntry(revocationEvent, revoked);
    setRevocableAttribute(new ClRevocationEventWrapper(revocationEvent));
  }

  /**
   * @param revocationEvent
   * @param revoked
   * @throws ConfigurationException
   */
  private void initRevocationLogEntry(final RevocationEvent revocationEvent, final boolean revoked)
      throws ConfigurationException {
    final ClRevocationEventWrapper revocationEventWrapper = new ClRevocationEventWrapper(revocationEvent);

    setRevocationLogEntryId(revocationEventWrapper.getEventId());
    setDateCreated(revocationEventWrapper.getEventDate().toGregorianCalendar());
    setRevoked(revoked);
    setRevocationEvent(revocationEvent);
  }

  public RevocationLogEntryFacade(final NonRevocationEvidenceFacade nreFacade, final boolean revoked)
      throws ConfigurationException {
    this();

    initRevocationLogEntry(createRevocationEvent(nreFacade), revoked);
    setRevocableAttribute(nreFacade.getAttributeList().get(0));
  }

  private RevocationEvent createRevocationEvent(final NonRevocationEvidenceFacade nreFacade)
      throws ConfigurationException {

    final ClRevocationEventWrapper revocationEventWrapper =
        new ClRevocationEventWrapper(nreFacade.getRevocationAuthorityParametersId(),
            nreFacade.getEpoch(), nreFacade.getRevocationHandleValue(), nreFacade.getCreated(),
            nreFacade.getAccumulatorValue());

    return revocationEventWrapper.getDelegateeValue();
  }


  public Calendar getDateCreated() {
    return getDelegateeValue().getDateCreated();
  }

  public void setDateCreated(Calendar date) {
    getDelegateeValue().setDateCreated(date);
  }

  public List<AttributeInLogEntry> getRevocableAttribute() {
    return getDelegateeValue().getRevocableAttribute();
  }

  public void setRevocableAttribute(final AttributeInLogEntry attributeInLog) {
    getDelegateeValue().getRevocableAttribute().add(attributeInLog);
  }

  private void setRevocableAttribute(final ClRevocationEventWrapper revocationEventWrapper)
      throws ConfigurationException {
    // Setup attribute to be used in revocation log entry
    final AttributeInLogEntry att = new ObjectFactory().createAttributeInLogEntry();
    att.setAttributeType(URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
    att.setAttributeValue(revocationEventWrapper.getRevocationHandle().getValue());

    setRevocableAttribute(att);
  }

  private void setRevocableAttribute(final Attribute revocableAttribute) throws ConfigurationException {
    // Setup attribute to be used in revocation log entry
    final AttributeInLogEntry att = new ObjectFactory().createAttributeInLogEntry();
    att.setAttributeType(revocableAttribute.getAttributeDescription().getType());
    att.setAttributeValue(revocableAttribute.getAttributeValue());

    setRevocableAttribute(att);
  }

  public URI getRevocationLogEntryId() {
    return getDelegateeValue().getRevocationLogEntryUID();
  }

  public void setRevocationLogEntryId(final URI revocationLogEntryId) {
    getDelegateeValue().setRevocationLogEntryUID(revocationLogEntryId);
  }

  public void setRevoked(boolean revoked) {
    getDelegateeValue().setRevoked(revoked);
  }


  public RevocationEvent getRevocationEvent() {
//	    Object accumulatorEventObject =
//	            JAXBIntrospector.getValue(getDelegateeValue().getCryptoParameters().getContent().get(0));
	  XmlUtils.fixNestedContent(getDelegateeValue().getCryptoParameters());
	  final Object accumulatorEventObject =
	            getDelegateeValue().getCryptoParameters().getContent().get(0);
    if (RevocationEvent.class.isAssignableFrom(accumulatorEventObject.getClass())) {
      return (RevocationEvent) accumulatorEventObject;
    } else {
      return null;
    }
  }

  public void setRevocationEvent(final RevocationEvent event) {

    final ObjectFactory objectFactory = new ObjectFactory();
    final CryptoParams cryptoParams = objectFactory.createCryptoParams();
    cryptoParams.getContent().add(objectFactory.createRevocationEvent(event));
    getDelegateeValue().setCryptoParameters(cryptoParams);
  }

  public RevocationLogEntry getDelegateeValue() {
    return (RevocationLogEntry) JAXBIntrospector.getValue(delegatee);
  }

  public int getNewEpoch() throws ConfigurationException {
    final ClRevocationEventWrapper revocationEventWrapper =
        new ClRevocationEventWrapper(getRevocationEvent());
    return revocationEventWrapper.getNewEpoch();
  }



  // public static RevocationLogEntryFacade deserialize(String delegatee)
  // throws SerializationException, ConfigurationException {
  //
  // JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(delegatee);
  //
  // return new RevocationLogEntryFacade(jaxbElement);
  // }
  //
  // public String serialize() throws SerializationException {
  // return JaxbHelperClass.serialize(delegatee);
  // }

  public static URI getRevocationLogEntryUID(final URI publicKeyId, final String epoch) {
    return URI.create(publicKeyId.toString() + ":" + epoch);
  }

}
