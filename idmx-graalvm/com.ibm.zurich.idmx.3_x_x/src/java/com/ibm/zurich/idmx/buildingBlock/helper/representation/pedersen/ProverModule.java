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

package com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.ProverModulePresentation;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofCommitment;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupMultOpSequence;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;

import eu.abc4trust.xml.SystemParameters;

public class ProverModule
    extends ProverModulePresentation<PedersenRepresentationBuildingBlock, KnownOrderGroup, KnownOrderGroupElement, KnownOrderGroupMultOpSequence> {

  private final int NO_DEVICE = 0;
  private final int DEVICE_PROOF = 1;
  private final int SCOPE_EXCLUSIVE_PSEUDONYM_PROOF = 2;
  private final int ONE_BASE_CREDENTIAL_PROOF = 3;
  private final int TWO_BASE_CREDENTIAL_PROOF = 4;

  public ProverModule(final PedersenRepresentationBuildingBlock parent, final String identifierOfModule,
                      final SystemParameters systemParameters, final List<BaseForRepresentation> bases, final KnownOrderGroup group,
      final @Nullable URI deviceUid, final String username, final @Nullable URI identifierOfCredentialForSecret, final URI scope,
      final @Nullable KnownOrderGroupElement commitment, final BuildingBlockFactory bbFactory,
      final ExternalSecretsManager esManager, final RandomGeneration randomGeneration, final Logger logger,
      final BigIntFactory bigIntFactory) {

    super(parent, identifierOfModule, systemParameters, bases, group, deviceUid,
        username, identifierOfCredentialForSecret, scope, commitment, false, bbFactory, esManager,
        randomGeneration, logger, bigIntFactory);
  }

  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    findProofTypeAndIndicesOfExternalVariables(zkBuilder);
    /*
     * register all attributes that are to be used in the building block, where those on the
     * smartcard are defined as external (i.e., the secret and the external randomizer); tell the
     * proof engine which attributes will be provided (all that are chosen internally), and choose
     * these latter attributes randomly between 0 and p
     */
    for (int i = 0; i < bases.size(); i++) {
      if (i == indexOfSecret || i == indexOfExternalRandomizer) {
        zkBuilder.registerAttribute(identifierOfAttribute(i), true);
      } else {
        zkBuilder.registerAttribute(identifierOfAttribute(i), false);
      }

      if (indicesOfRandomlyChosenAttributes.indexOf(i) != -1) {
        zkBuilder.providesAttribute(identifierOfAttribute(i));
        // choose and assign random attributes
        final BigInt r =
            group.createRandomIterationcounter(rg, spWrapper.getStatisticalInd()).mod(
                group.getOrder());
        zkBuilder.setValueOfAttribute(identifierOfAttribute(i), r, null);
        valuesOfRandomlyChosenAttributes.add(r);
      } else if (!(i == indexOfSecret || i == indexOfExternalRandomizer)) {
        zkBuilder.requiresAttributeValue(identifierOfAttribute(i));
      }
    }
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) {
    /*
     * Non-Smartcard-Secrets and their sValues etc. are fully handled by the Proof Engine, so only
     * the sValues for secrets remaining on the device need to be handled here The output still
     * needs to be reduced modulo the group order
     */
    final List<BigInt> devSValues = getDeviceSValues(zkBuilder.getChallenge(), zkBuilder);
    if (devSValues.size() > 0) {
      zkBuilder.setSValueOfExternalAttribute(identifierOfAttribute(indexOfSecret),
          devSValues.get(0));
      if (devSValues.size() > 1) {
        zkBuilder.setSValueOfExternalAttribute(identifierOfAttribute(indexOfExternalRandomizer),
            devSValues.get(1).mod(group.getOrder()));
      }
    }
  }

  /**
   * This function computes the part of the TValue that comes from the device
   */

  @Override
  protected GroupElement<KnownOrderGroup, KnownOrderGroupElement, KnownOrderGroupMultOpSequence> getDeviceTValuePortion(final ZkProofStateFirstRound zkBuilder)
      throws ConfigurationException, ProofException {
    final BigInt tPrime;
    KnownOrderGroupElement tValuePortion = group.neutralElement();
    switch (typeOfProofOnDevice) {
      case NO_DEVICE: // NO DEVICE IN USE
        break;
      case DEVICE_PROOF: // PROVING DEVICE PUBLIC KEY
        final DeviceProofCommitment deviceProofCommitment = zkBuilder.getDeviceProofCommitment();
        final BigInteger commitmentForPK = deviceProofCommitment.getCommitmentForPublicKey(deviceUid);
        tPrime = bigIntFactory.valueOf(commitmentForPK);
        tValuePortion = group.valueOf(tPrime);
        break;
      case SCOPE_EXCLUSIVE_PSEUDONYM_PROOF: // SCOPE EXCLUSIVE PSEUDONYM
        tPrime =
            bigIntFactory.valueOf(zkBuilder.getDeviceProofCommitment()
                .getCommitmentForScopeExclusivePseudonym(deviceUid, scope));
        tValuePortion = group.valueOf(tPrime);
        // check if the base in BaseForRepresentations is the same as used
        // by the smartcard, and that base != null
        if (bases.get(indexOfSecret).getBase(zkBuilder, group) == null
            || (bases.get(indexOfSecret).getBase(zkBuilder, group) != null && !((esManager
                .getBaseForScopeExclusivePseudonym(username, deviceUid, scope)).equals(bases
                .get(indexOfSecret).getBase(zkBuilder, group).toBigInt().getValue())))) {
          throw new ConfigurationException(
              "Base for scope-exclusive pseudonym was set incorrectly (different values found on smartcard and given, or NULL)!");
        }
        break;
      case ONE_BASE_CREDENTIAL_PROOF:// PROVING ONE BASE CREDENTIAL
        final DeviceProofCommitment dpc = zkBuilder.getDeviceProofCommitment();
        final BigInteger cfc = dpc.getCommitmentForCredential(deviceUid, identifierOfCredentialForSecret);
        tPrime = bigIntFactory.valueOf(cfc);
        tValuePortion = group.valueOf(tPrime);
        break;
      case TWO_BASE_CREDENTIAL_PROOF:// PROVING TWO BASE CREDENTIAL
        tPrime =
            bigIntFactory.valueOf(zkBuilder.getDeviceProofCommitment().getCommitmentForCredential(
                deviceUid, identifierOfCredentialForSecret));
        tValuePortion = group.valueOf(tPrime);
        if (bases.get(indexOfExternalRandomizer).externalRandomizerOffset != null) {

          rOffset =
              group.createRandomIterationcounter(rg, spWrapper.getStatisticalInd()).mod(
                  group.getOrder());
          tValuePortion =
              tValuePortion.opMultOp(
                  (KnownOrderGroupElement)(bases.get(indexOfExternalRandomizer).getBase(zkBuilder, group)), rOffset);
        }
        break;
    }
    return tValuePortion;
  }
}
