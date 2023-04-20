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

package com.ibm.zurich.idmx.buildingBlock.revocation;

import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.RevocationHistoryFacade;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationStateWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.revocation.StateRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationState;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

/**
 * 
 */
public class StateRevocationAuthorityImpl implements StateRevocationAuthority {

  /**
   * 
   */
  private static final long serialVersionUID = -5173409103262558695L;

  private final EcryptSystemParametersWrapper spWrapper;
 
  //TODO(ksa) ?
  private ClRevocationStateWrapper revocationStateWrapper;
  private RevocationHistoryFacade revocationHistoryFacade;
  private final List<BigInt> revocationHandles;

  private RandomGeneration randomGeneration;

  public StateRevocationAuthorityImpl(final SystemParameters systemParameters,
                                      final RevocationState revocationState, final RevocationHistory revocationHistory,
      final RandomGeneration randomGeneration) {
    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);

    this.revocationStateWrapper = new ClRevocationStateWrapper(revocationState);
    this.revocationHistoryFacade = new RevocationHistoryFacade(revocationHistory);
    this.revocationHandles = new ArrayList<BigInt>();

    this.randomGeneration = randomGeneration;
  }



  @Override
  public RevocationState getRevocationState() {
    return revocationStateWrapper.getRevocationState();
  }

  @Override
  public void setRevocationState(final RevocationState revocationState) {
    this.revocationStateWrapper = new ClRevocationStateWrapper(revocationState);
  }

  @Override
  public BigInt generateRevocationHandle(final PublicKey publicKey) throws ConfigurationException {

    final ClPublicKeyWrapper rapkWrapper = new ClPublicKeyWrapper(publicKey);
    if (!revocationStateWrapper.getRaPublicKeyId().equals(rapkWrapper.getPublicKeyId())) {
      throw new ConfigurationException(
          ErrorMessages.elementIdsMismatch("revocation authority public key."));
    }

    BigInt revocationHandle = getRandomPrime(rapkWrapper.getPublicKey());
    while (revocationHandles.contains(revocationHandle)) {
      revocationHandle = getRandomPrime(rapkWrapper.getPublicKey());
    }
    revocationHandles.add(revocationHandle);

    return revocationHandle;
  }

  /**
   * Generate a random prime number suitable for adding to the accumulator.
   * 
   * @throws ConfigurationException
   * @throws KeyManagerException
   */
  private BigInt getRandomPrime(final PublicKey publicKey) throws ConfigurationException {

    final int messageLength = spWrapper.getAttributeLength();
    final int primeProbability = spWrapper.getPrimeProbability();
    return randomGeneration.generateRandomPrime(messageLength, primeProbability);
  }


  @Override
  public RevocationHistory getHistory() {
    return revocationHistoryFacade.getDelegateeElement();
  }

  @Override
  public void setHistory(RevocationHistory revocationHistory) {
    this.revocationHistoryFacade = new RevocationHistoryFacade(revocationHistory);
  }


  // TODO restructure to make the state not contain those methods
  @Override
  public int getStepOfNextExpectedPhase() {
    throw new RuntimeException(ErrorMessages.wrongUsage("this method may not be called."));
  }

  // TODO restructure to make the state not contain those methods
  @Override
  public VerifierParameters getVerifierParameters() {
    throw new RuntimeException(ErrorMessages.wrongUsage("this method may not be called."));
  }

  // TODO restructure to make the state not contain those methods
  @Override
  public Object getPhaseDependantObject() {
    throw new RuntimeException(ErrorMessages.wrongUsage("this method may not be called."));
  }

}
