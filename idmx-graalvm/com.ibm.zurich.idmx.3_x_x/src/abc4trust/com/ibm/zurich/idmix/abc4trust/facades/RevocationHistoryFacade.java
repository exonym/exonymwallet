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
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationEventWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.RevocationAuthorityConfiguration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationEvent;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationLogEntry;

/**
 * 
 */
public class RevocationHistoryFacade implements Constants {

  //private static final String NRE_VALUE = "nonRevocationEvidenceValue";
  //private static final String RH_VALUE = "revocationHandleValue";


  // Delegatee
  private final JAXBElement<RevocationHistory> delegatee;



  public RevocationHistoryFacade() {
    this(new ObjectFactory().createRevocationHistory());
  }

  public RevocationHistoryFacade(final RevocationHistory delegatee) {
    this.delegatee = new ObjectFactory().createRevocationHistory(delegatee);
  }

  private RevocationHistoryFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.delegatee = verifyTypeOfJaxbElement(jaxbElement);
  }

  /**
   * This constructor creates a new revocation history based on the given revocation authority
   * public key id.
   */
  public RevocationHistoryFacade(final URI publicKeyId) {
    this();

    final URI raParametersUID =
        RevocationAuthorityParametersFacade.getRevocationAuthorityParametersUID(publicKeyId);
    setRevocationHistoryId(getRevocationHistoryUID(raParametersUID));
    setRevocationAuthorityParametersUID(RevocationAuthorityParametersFacade
        .getRevocationAuthorityParametersUID(publicKeyId));

    // TODO add revocation log entry for creation of accumulator?

  }

  // public RevocationHistoryFacade(RevocationState revocationState,
  // BigInt nonRevocationEvidenceValue, PublicKey raPublicKey) throws ConfigurationException {
  // this();
  //
  // ClRevocationStateWrapper revocationStateWrapper = new
  // ClRevocationStateWrapper(revocationState);
  // RevocationAuthorityPublicKeyWrapper rapkWrapper =
  // new RevocationAuthorityPublicKeyWrapper(raPublicKey);
  // setRevocationAuthorityParametersUID(revocationStateWrapper.getRaPublicKeyId());
  //
  // // set epoch
  // setEpoch(revocationStateWrapper.getEpoch());
  // // set the values
  // setNonRevocationEvidenceValue(nonRevocationEvidenceValue);
  // }

  // public void fillIn(URI nonRevocationEvidenceId, BigInt revocationHandleValue, Attribute
  // attribute)
  // throws ConfigurationException {
  //
  // // nonRevocationEvidenceUId = urn:abc4trust:1.0:nonrevocation:evidence/3w3dlh1zg9k4hnw1
  // setNonRevocationEvidenceId(nonRevocationEvidenceId);
  //
  // // TODO get the credential uid from the actual credential (where from ???)
  // // credentialUid = credentialDescription.getCredentialUid()
  // setCredentialId(URI.create("urn:abc4trust:1.0:tobesetbyuser"));
  //
  // // created = now (but in day granularity (or even larger?)
  // Calendar now = now(RevocationAuthorityConfiguration.getCreationDateGranularity());
  // setCreated(now);
  // // expires = now + 1y?
  // now.roll(RevocationAuthorityConfiguration.getTimeToLive(), true);
  // setExpires(now);
  //
  // // add value and UID to attribute
  // attribute.setAttributeValue(revocationHandleValue.getValue());
  // attribute.setAttributeUID(URI.create(
  // // TODO: this should actually be provided in the issuer set attributes!!
  // // getCredentialId().toString() +
  // "" + new Random().nextInt()));
  // // register attribute in nonRevocationEvidence
  // setAttribute(attribute);
  // // add revocation handle value to crypto params // TODO remove this - could be taken from the
  // // attribute
  // setRevocationHandleValue(revocationHandleValue);
  //
  // }


  public URI getRevocationAuthorityParametersId() throws ConfigurationException {
    return getDelegateeElement().getRevocationAuthorityParametersUID();
  }

  public void setRevocationAuthorityParametersUID(final URI revocationAuthorityParametersId) {
    getDelegateeElement().setRevocationAuthorityParametersUID(revocationAuthorityParametersId);
  }

  public URI getRevocationHistoryId() {
    return getDelegateeElement().getRevocationHistoryUID();
  }

  public void setRevocationHistoryId(final URI revocationHistoryId) {
    getDelegateeElement().setRevocationHistoryUID(revocationHistoryId);
  }

  public RevocationLogEntry getRevocationLogEntry(final URI revocationLogId) {

    final List<RevocationLogEntry> revocationLogEntries = getRevocationLogEntries();

    for (final RevocationLogEntry revocationLogEntry : revocationLogEntries) {
      if (revocationLogEntry.getRevocationLogEntryUID().equals(revocationLogId)) {
        return revocationLogEntry;
      }
    }
    return null;
  }

  public void addRevocationLogEntry(final RevocationLogEntry revocationLogEntry) {
    getRevocationLogEntries().add(0, revocationLogEntry);
  }

  /**
   * @return
   */
  public List<RevocationLogEntry> getRevocationLogEntries() {
    return getDelegateeElement().getRevocationLogEntry();
  }

  public RevocationLogEntryFacade addRevocationEvent(final RevocationEvent revocationEvent)
      throws ConfigurationException {

    //TODO(ksa) get rid
    final RevocationLogEntryFacade revocationLogEntryFacade =
        new RevocationLogEntryFacade(revocationEvent, true);

    addRevocationLogEntry(revocationLogEntryFacade.getDelegateeValue());

    return revocationLogEntryFacade;
  }

  public int getEpoch() throws ConfigurationException {
    // no revocation event has been applied so far
    if (getRevocationLogEntries().size() == 0) {
      return RevocationAuthorityConfiguration.initialEpoch();
    }
    final RevocationLogEntryFacade revocationLogEntryFacade =
        new RevocationLogEntryFacade(getRevocationLogEntries().get(0));
    return revocationLogEntryFacade.getNewEpoch();
  }

  public RevocationHistory getDelegateeElement() {
    return (RevocationHistory) JAXBIntrospector.getValue(delegatee);
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<RevocationHistory> verifyTypeOfJaxbElement(final JAXBElement<?> jaxbElement)
      throws SerializationException {
    final Class<?> delegateeClass = NonRevocationEvidence.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<RevocationHistory>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  public static RevocationHistoryFacade deserialize(final String issuerParameters)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(issuerParameters);

    return new RevocationHistoryFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }


  public static URI getRevocationHistoryUID(final URI raParametersUID) {
    return URI.create(raParametersUID.toString() + ":history");
  }


  public int getLatestEpoch() throws ConfigurationException {
    final RevocationLogEntry newestRevocationLogEntry = getLatestRevocationLogEntry();
    if (newestRevocationLogEntry != null) {
      final RevocationLogEntryFacade revocationLogEntryFacade =
          new RevocationLogEntryFacade(getLatestRevocationLogEntry());
      return revocationLogEntryFacade.getNewEpoch();
    }
    // no revocation log entries have been saved to the log so far
    return RevocationAuthorityConfiguration.initialEpoch();
  }

  /**
   * @return
   * @throws ConfigurationException
   */
  private RevocationLogEntry getLatestRevocationLogEntry() throws ConfigurationException {
    int newestEpoch = RevocationAuthorityConfiguration.initialEpoch();
    RevocationLogEntry newestRevocationLogEntry = null;

    for (final RevocationLogEntry revocationLogEntry : getRevocationLogEntries()) {
      final RevocationLogEntryFacade revocationLogEntryFacade =
          new RevocationLogEntryFacade(revocationLogEntry);

      final int revocationLogEntryEpoch = revocationLogEntryFacade.getNewEpoch();

      if (revocationLogEntryEpoch > newestEpoch) {
        newestEpoch = revocationLogEntryEpoch;
        newestRevocationLogEntry = revocationLogEntry;
      }
    }

    return newestRevocationLogEntry;
  }

  /**
   * @throws ConfigurationException
   */
  public boolean revocationHandleHasBeenRevoked(final BigInt revocationHandleValue)
      throws ConfigurationException {
    for (final RevocationLogEntry revocationLogEntry : getRevocationLogEntries()) {
      final ClRevocationEventWrapper revocationEventWrapper =
          new ClRevocationEventWrapper(
              new RevocationLogEntryFacade(revocationLogEntry).getRevocationEvent());
      if (revocationEventWrapper.getRevocationHandle().equals(revocationHandleValue)) {
        return true;
      }
    }
    return false;
  }

}
