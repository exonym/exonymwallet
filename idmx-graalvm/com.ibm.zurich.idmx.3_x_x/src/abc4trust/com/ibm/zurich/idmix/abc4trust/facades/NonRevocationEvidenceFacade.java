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
import java.util.Random;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationAuthorityPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationEventWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationStateWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.RevocationAuthorityConfiguration;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.RevocationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.util.Arithmetic;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.BigIntegerParameter;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationEvent;
import eu.abc4trust.xml.RevocationState;

/**
 * 
 */
public class NonRevocationEvidenceFacade implements Constants {

  private static final String ACCUMULATOR_VALUE = "accumulatorValue";
  private static final String NRE_VALUE = "nonRevocationEvidenceValue";
  private static final String RH_VALUE = "revocationHandleValue";
  private static final String MODULUS_VALUE = "modulus";


  // Delegatee
  private final JAXBElement<NonRevocationEvidence> delegatee;



  public NonRevocationEvidenceFacade() {
    this(new ObjectFactory().createNonRevocationEvidence());
  }

  public NonRevocationEvidenceFacade(final NonRevocationEvidence delegatee) {
    this.delegatee = new ObjectFactory().createNonRevocationEvidence(delegatee);
  }

  private NonRevocationEvidenceFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.delegatee = verifyTypeOfJaxbElement(jaxbElement);
  }

  public NonRevocationEvidenceFacade(final RevocationState revocationState,
                                     final BigInt nonRevocationEvidenceValue, final PublicKey raPublicKey) throws ConfigurationException {
    this();

    ClRevocationStateWrapper revocationStateWrapper = new ClRevocationStateWrapper(revocationState);
    ClRevocationAuthorityPublicKeyWrapper rapkWrapper =
        new ClRevocationAuthorityPublicKeyWrapper(raPublicKey);
    setRevocationAuthorityParametersUID(revocationStateWrapper.getRaPublicKeyId());

    // set epoch
    setEpoch(revocationStateWrapper.getEpoch());
    // set the values
    setNonRevocationEvidenceValue(nonRevocationEvidenceValue);
    setAccumulatorValue(revocationStateWrapper.getAccumulatorValue());
    setModulus(rapkWrapper.getModulus());
  }

  public void fillIn(final URI nonRevocationEvidenceId, final BigInt revocationHandleValue, final Attribute attribute)
      throws ConfigurationException {
    final URI copy;
    // nonRevocationEvidenceUId = urn:abc4trust:1.0:nonrevocation:evidence/3w3dlh1zg9k4hnw1
    if (nonRevocationEvidenceId == null) {
      // TODO(enr): The non-revocation evidence Id is a mandatory field
      // setting to a random value here if null
      copy = URI.create(UUID.randomUUID().toString());
    } else {
      copy = nonRevocationEvidenceId;
    }
    setNonRevocationEvidenceId(copy);

    // TODO get the credential uid from the actual credential (where from ???)
    // credentialUid = credentialDescription.getCredentialUid()
    setCredentialId(URI.create("urn:abc4trust:1.0:tobesetbyuser"));

    // created = now (but in day granularity (or even larger?)
    final Calendar now =
        RevocationAuthorityConfiguration.now(RevocationAuthorityConfiguration
            .getCreationDateGranularity());
    setCreated(now);
    // expires = now + 1y?
    now.roll(RevocationAuthorityConfiguration.getRevocationHandleTimeToLive(), true);
    setExpires(now);

    // add value and UID to attribute
    attribute.setAttributeValue(revocationHandleValue.getValue());
    attribute.setAttributeUID(URI.create(
    // TODO: this should actually be provided in the issuer set attributes!!
    // getCredentialId().toString() +
        "" + new Random().nextInt()));
    // register attribute in nonRevocationEvidence
    setAttribute(attribute);
    // add revocation handle value to crypto params // TODO remove this - could be taken from the
    // attribute
    setRevocationHandleValue(revocationHandleValue);

  }


  private NonRevocationEvidenceFacade(final NonRevocationEvidenceFacade previous, final int newEpoch,
                                      final BigInt newNonRevocationEvidenceValue) throws ConfigurationException {
    this();

    // set the changes things
    setEpoch(newEpoch);
    setNonRevocationEvidenceValue(newNonRevocationEvidenceValue);
    // created = now (but in day granularity (or even larger?)
    Calendar now =
        RevocationAuthorityConfiguration.now(RevocationAuthorityConfiguration
            .getCreationDateGranularity());
    setCreated(now);
    // expires = now + 1y?
    now.roll(RevocationAuthorityConfiguration.getRevocationHandleTimeToLive(), true);
    setExpires(now);

    // set the stuff that remained the same
    setRevocationAuthorityParametersUID(previous.getRevocationAuthorityParametersId());
    setNonRevocationEvidenceId(previous.getNonRevocationEvidenceId());
    setCredentialId(previous.getCredentialId());
    setAttributeList(previous.getAttributeList());
    setRevocationHandleValue(previous.getRevocationHandleValue());

    // set the modulus (maybe that should be moved)
    setModulus(previous.getModulus());
  }


  public URI getCredentialId() {
    return getDelegateeElement().getCredentialUID();
  }

  public void setCredentialId(final URI credentialId) {
    getDelegateeElement().setCredentialUID(credentialId);
  }

  public URI getNonRevocationEvidenceId() {
    return getDelegateeElement().getNonRevocationEvidenceUID();
  }

  public void setNonRevocationEvidenceId(final URI nonRevocationEvidenceId) {
    getDelegateeElement().setNonRevocationEvidenceUID(nonRevocationEvidenceId);
  }

  public URI getRevocationAuthorityParametersId() throws ConfigurationException {
    return getDelegateeElement().getRevocationAuthorityParametersUID();
  }

  public void setRevocationAuthorityParametersUID(final URI revocationAuthorityParametersId) {
    getDelegateeElement().setRevocationAuthorityParametersUID(revocationAuthorityParametersId);
  }

  public int getEpoch() {
    return getDelegateeElement().getEpoch();
  }

  public void setEpoch(final int epoch) {
    getDelegateeElement().setEpoch(epoch);
  }

  public List<Attribute> getAttributeList() {
    return getDelegateeElement().getAttribute();
  }

  public void setAttributeList(final List<Attribute> attributeList) {
    getDelegateeElement().getAttribute().addAll(attributeList);
  }

  public void setAttribute(final Attribute attribute) {
    getAttributeList().add(attribute);
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



  private JAXBElement<BigIntegerParameter> getBigIntegerParameter(final String name, final BigInt value) {
    final ObjectFactory objectFactory = new ObjectFactory();
    final BigIntegerParameter bigIntParameter = objectFactory.createBigIntegerParameter();
    bigIntParameter.setName(name);
    bigIntParameter.setValue(value.getValue());
    return objectFactory.createBigIntegerParameter(bigIntParameter);
  }

  // TODO use the ParametersHelper class here
  private BigInt getBigIntegerParameter(final String name) {
    for (final Object object : getCryptoParamsList()) {
      final Object containedObject = JAXBIntrospector.getValue(object);
      if (BigIntegerParameter.class.isAssignableFrom(containedObject.getClass())) {
        final BigIntegerParameter bigIntegerParameter = (BigIntegerParameter) containedObject;
        if (bigIntegerParameter.getName().equals(name)) {
          return new BigIntFactoryImpl().valueOf(bigIntegerParameter.getValue());
        }
      }
    }
    return null;
  }

  public void setAccumulatorValue(final BigInt accumulatorValue) {
    getCryptoParamsList().add(getBigIntegerParameter(ACCUMULATOR_VALUE, accumulatorValue));
  }

  public BigInt getAccumulatorValue() throws ConfigurationException {
    final BigInt accumulatorValue = getBigIntegerParameter(ACCUMULATOR_VALUE);
    if (accumulatorValue == null) {
      throw new ConfigurationException(ErrorMessages.parameterNotFound(ACCUMULATOR_VALUE));
    }
    return accumulatorValue;
  }

  public void setRevocationHandleValue(final BigInt revocationHandleValue) {
    getCryptoParamsList().add(getBigIntegerParameter(RH_VALUE, revocationHandleValue));
  }

  public BigInt getRevocationHandleValue() throws ConfigurationException {
    final BigInt revocationHandleValue = getBigIntegerParameter(RH_VALUE);
    if (revocationHandleValue == null) {
      throw new ConfigurationException(ErrorMessages.parameterNotFound(RH_VALUE));
    }
    return revocationHandleValue;
  }

  public void setNonRevocationEvidenceValue(BigInt nonRevocationEvidenceValue) {

    final JAXBElement<BigIntegerParameter> bigIntegerParameter =
        getBigIntegerParameter(NRE_VALUE, nonRevocationEvidenceValue);
    getCryptoParamsList().add(bigIntegerParameter);
  }

  public BigInt getNonRevocationEvidenceValue() throws ConfigurationException {
    final BigInt revocationHandleValue = getBigIntegerParameter(NRE_VALUE);
    if (revocationHandleValue == null) {
      throw new ConfigurationException(ErrorMessages.parameterNotFound(NRE_VALUE));
    }
    return revocationHandleValue;
  }

  public void setModulus(BigInt modulus) {

    final JAXBElement<BigIntegerParameter> bigIntegerParameter =
        getBigIntegerParameter(MODULUS_VALUE, modulus);
    getCryptoParamsList().add(bigIntegerParameter);
  }

  public BigInt getModulus() throws ConfigurationException {
    final BigInt modulus = getBigIntegerParameter(MODULUS_VALUE);
    if (modulus == null) {
      throw new ConfigurationException(ErrorMessages.parameterNotFound(NRE_VALUE));
    }
    return modulus;
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


  public NonRevocationEvidence getDelegateeElement() {
    return (NonRevocationEvidence) JAXBIntrospector.getValue(delegatee);
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<NonRevocationEvidence> verifyTypeOfJaxbElement(
    final JAXBElement<?> jaxbElement) throws SerializationException {
    final Class<?> delegateeClass = NonRevocationEvidence.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<NonRevocationEvidence>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }



  public static NonRevocationEvidenceFacade deserialize(final String issuerParameters)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(issuerParameters);

    return new NonRevocationEvidenceFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }


  public NonRevocationEvidenceFacade updateWitness(final RevocationEvent revocationEvent,
                                                   final GroupFactory groupFactory) throws RevocationException, ConfigurationException {

    final ClRevocationEventWrapper revocationEventWrapper = new ClRevocationEventWrapper(revocationEvent);

    final BigInt revocationHandleValue = getRevocationHandleValue();

    final BigInt accumulatorValue = revocationEventWrapper.getAccumulatorValue();
    final BigInt removedPrime = revocationEventWrapper.getRevocationHandle();
    final BigInt modulus = getModulus();
    final BigIntFactory bigIntFactory = removedPrime.getFactory();

    if (!revocationHandleValue.gcd(removedPrime).equals(bigIntFactory.one())) {
      throw new RevocationException(ErrorMessages.valueHasBeenRevoked());
    }

    // find a, b st. a*ownprime + b*removedprime = 1
    final Pair<BigInt, BigInt> euclid = Arithmetic.extendedEuclid(revocationHandleValue, removedPrime);
    // newWit = oldWit^b * newAcc^a (mod n)
    final HiddenOrderGroup group = ClRevocationAuthorityPublicKeyWrapper.getGroup(groupFactory, modulus);

    final HiddenOrderGroupElement term1 =
        group.valueOf(getNonRevocationEvidenceValue()).multOp(euclid.second);
    final HiddenOrderGroupElement term2 = group.valueOf(accumulatorValue).multOp(euclid.first);
    final HiddenOrderGroupElement newNonRevocationEvidenceValue = term1.op(term2);

    System.out.println("New Values:");
    System.out.println("u:" + newNonRevocationEvidenceValue.toBigInt());
    System.out.println("e:" + revocationHandleValue);
    System.out.println("u^e:" + newNonRevocationEvidenceValue.multOp(revocationHandleValue).toBigInt());
    System.out.println("v:" + accumulatorValue);
    
    // BigInt term1 = getNonRevocationEvidenceValue().modPow(euclid.second, modulus);
    // BigInt term2 = accumulatorValue.modPow(euclid.first, modulus);
    // BigInt newNonRevocationEvidenceValue = (term1.multiply(term2).mod(modulus));
    
    if (Configuration.debug()) {
      if (!isConsistent(accumulatorValue, newNonRevocationEvidenceValue)) {
        throw new RuntimeException("Witness update failed in Accumulator");
      }
    }

    return new NonRevocationEvidenceFacade(this, revocationEventWrapper.getNewEpoch(),
        newNonRevocationEvidenceValue.toBigInt());
  }

  /**
   * Check if the witness/value pair is consistent with the current state.
   * 
   * @throws ConfigurationException
   */
  public boolean isConsistent(final BigInt accumulatorValue,
                              final HiddenOrderGroupElement newNonRevocationEvidenceValue) throws ConfigurationException {
    final BigInt calculatedAccumulatorValue =
        newNonRevocationEvidenceValue.multOp(getRevocationHandleValue()).toBigInt();
    // getNonRevocationEvidenceValue().modPow(getRevocationHandleValue(), getModulus());
    if (Configuration.debug()) {
      System.err.println("");
      System.err.println("u: " + newNonRevocationEvidenceValue.toBigInt());
      System.err.println("e: " + getRevocationHandleValue());
      System.err.println("u^e: " + calculatedAccumulatorValue);
      System.err.println("v: " + accumulatorValue);
    }
    return calculatedAccumulatorValue.equals(accumulatorValue);
  }


}
