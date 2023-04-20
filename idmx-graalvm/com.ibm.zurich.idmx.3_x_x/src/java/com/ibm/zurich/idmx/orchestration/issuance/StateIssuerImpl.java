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
package com.ibm.zurich.idmx.orchestration.issuance;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Random;

import com.ibm.zurich.idmix.abc4trust.facades.NonRevocationEvidenceFacade;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.PhaseIssuer;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.VerifierParameters;

class StateIssuerImpl implements StateIssuer {

  private static final long serialVersionUID = 5501334708546417521L;
  
  private final PhaseIssuer phase;
  private final int nextStep;
  private final IssuancePolicy ip;
  private final Serializable phaseDependantObject;
  private final AttributeList attributes;
  private NonRevocationEvidence nre;

  public StateIssuerImpl(final PhaseIssuer phase, final int nextStep, final IssuancePolicy ip,
                         final List<Attribute> attributes, final Serializable phaseDependantObject, final NonRevocationEvidence nre) {
    this.phase = phase;
    this.nextStep = nextStep;
    this.ip = ip;
    this.phaseDependantObject = phaseDependantObject;
    this.attributes = new ObjectFactory().createAttributeList();
    this.attributes.getAttributes().addAll(attributes);
  }


  public StateIssuerImpl(final StateIssuer lastState, final PhaseIssuer phase, final int nextStep,
                         final Serializable phaseDependantObject) {
    this(phase, nextStep, lastState.getIssuancePolicy(), lastState.getIssuerSetAttributes()
        .getAttributes(), phaseDependantObject, null);
  }

  @Override
  public int getStepOfNextExpectedPhase() {
    return nextStep;
  }

  @Override
  public IssuancePolicy getIssuancePolicy() {
    return ip;
  }

  @Override
  public VerifierParameters getVerifierParameters() {
    return ip.getVerifierParameters();
  }

  @Override
  public Object getPhaseDependantObject() {
    return phaseDependantObject;
  }

  @Override
  public PhaseIssuer getNextExpectedPhase() {
    return phase;
  }

  @Override
  public AttributeList getIssuerSetAttributes() {
    return attributes;
  }

  @Override
  public void addRevocationHandle(final BigInt value, final AttributeDescription ad) {
    boolean attributePresent = false;
    final List<Attribute> attributeList = getIssuerSetAttributes().getAttributes();
    for (final Attribute attribute : attributeList) {
      if (attribute.getAttributeDescription().getType().equals(ad.getType())) {
        attribute.setAttributeValue(value.getValue());
        attributePresent = true;
        break;
      }
    }
    if (!attributePresent) {
      final Attribute attribute = new ObjectFactory().createAttribute();
      attribute.setAttributeUID(URI.create("" + new Random().nextInt()));
      attribute.setAttributeDescription(ad);
      attribute.setAttributeValue(value.getValue());
      attributeList.add(attribute);
    }
  }


  @Override
  public void setNonRevocationEvidence(final NonRevocationEvidence nre) throws ConfigurationException {
    final NonRevocationEvidenceFacade nreFacade = new NonRevocationEvidenceFacade(nre);

    addRevocationHandle(nreFacade.getRevocationHandleValue(), nreFacade.getAttributeList().get(0)
        .getAttributeDescription());
    this.nre = nreFacade.getDelegateeElement();

  }

  @Override
  public NonRevocationEvidence getNonRevocationEvidence() {
    return nre;
  }
}
