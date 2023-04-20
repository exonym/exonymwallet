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
package com.ibm.zurich.idmx.buildingBlock.helper.representation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.MultOpSequence;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.SystemParameters;

public abstract class ProverModulePresentation<P extends GeneralBuildingBlock, G extends Group<G, GE, M>, GE extends GroupElement<G, GE, M>, M extends MultOpSequence<G, GE, M>>
    extends ZkModuleImpl implements ZkModuleProverCommitment<GE> {
  protected final EcryptSystemParametersWrapper spWrapper; // systemParameters
  protected final @Nullable
  URI identifierOfCredentialForSecret;
  protected final List<BaseForRepresentation> bases;
  protected final G group;
  protected final BuildingBlockFactory bbFactory;
  protected final @Nullable
  ExternalSecretsManager esManager;
  protected final List<Integer> indicesOfRandomlyChosenAttributes;
  protected final List<BigInt> valuesOfRandomlyChosenAttributes;
  protected final List<Boolean> revealedAttr;
  protected GE commitment;
  protected final RandomGeneration rg;
  protected final Logger logger;
  protected final BigIntFactory bigIntFactory;
  protected final URI deviceUid;
  protected final URI scope;
  protected int indexOfSecret;
  protected int indexOfExternalRandomizer;
  protected final String username;

  public ProverModulePresentation(final P parent, final String identifierOfModule,
                                  final SystemParameters systemParameters, final List<BaseForRepresentation> bases, final G group,
      final @Nullable URI deviceUid, final String username, final @Nullable URI identifierOfCredentialForSecret, final URI scope,
      final @Nullable GE commitment, final boolean takeAbsoluteValue,
      final BuildingBlockFactory bbFactory, final ExternalSecretsManager esManager,
      final RandomGeneration randomGeneration, final Logger logger, final BigIntFactory bigIntFactory) {

    super(parent, identifierOfModule);

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.bases = bases;
    this.group = group;
    this.rg = randomGeneration;
    this.revealedAttr = new ArrayList<Boolean>();
    this.indicesOfRandomlyChosenAttributes = new ArrayList<Integer>();
    this.valuesOfRandomlyChosenAttributes = new ArrayList<BigInt>();
    this.logger = logger;
    this.bigIntFactory = bigIntFactory;
    this.identifierOfCredentialForSecret = identifierOfCredentialForSecret;
    this.deviceUid = deviceUid;
    this.username = username;
    this.scope = scope;
    this.commitment = commitment;
    this.indexOfExternalRandomizer = -1;
    this.indexOfSecret = -1;

    this.esManager = esManager;
    this.bbFactory = bbFactory;
  }

  @Override
  public void collectAttributesForProof(ZkProofStateCollect zkBuilder) {
    // nothing to do here
  }


  //@SuppressWarnings({"unchecked", "rawtypes"})
  @SuppressWarnings("unchecked")
  @Override
  public void firstRound(ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    checkConfiguration(zkBuilder);
    // get number of attributes
    int numberOfAttributes = bases.size();
    // collect values of non-external attributes
    List<BigInt> valuesOfAttributes = new ArrayList<BigInt>();
    for (int i = 0; i < numberOfAttributes; i++) {
      if (!(bases.get(i).hasCredentialSecretKey || bases.get(i).hasExternalSecret)) {
        valuesOfAttributes.add(zkBuilder.getValueOfAttribute(identifierOfAttribute(i)));
      } else {
        valuesOfAttributes.add(null);
      }
    }
    // determine which attributes are revealed, and add them to DValues
    // simultaneously register all bases as NValues
    for (int i = 0; i < numberOfAttributes; i++) {
      if (zkBuilder.isRevealedAttribute(identifierOfAttribute(i))) {
        revealedAttr.add(true);
      } else {
        revealedAttr.add(false);
      }
      zkBuilder.addNValue(getIdentifier() + ":base:" + i, bases.get(i).getBase(zkBuilder, group));
    }
    /*
     * compute the commitment C = PROD_attributes base^attribute, get the randomnesses for the
     * unrevealed exponents, and compute the Sigma-protocol commitment as t = PROD_unrevealed
     * bases^randomnessForAttribute. Note that the commitment only has to be computed if it has not
     * been given as an input.
     */
    if (commitment == null) {
      M CSeq = group.initializeSequence();
      for (int i = 0; i < numberOfAttributes; i++) {
        if ((!bases.get(i).hasExternalSecret) && (!bases.get(i).hasCredentialSecretKey)) {
          CSeq.putMultOp((GE) bases.get(i).getBase(zkBuilder, group), valuesOfAttributes.get(i));
        }
      }
      commitment = CSeq.finalizeSequence();
      commitment = commitment.op(getDeviceCValuePortion(username, zkBuilder));
    }

    M tSeq = group.initializeSequence();

    for (int i = 0; i < numberOfAttributes; i++) {
      if (!(revealedAttr.get(i)) && (!bases.get(i).hasExternalSecret)
          && (!bases.get(i).hasCredentialSecretKey)) {
        tSeq.putMultOp((GE) bases.get(i).getBase(zkBuilder, group),
            zkBuilder.getRValueOfAttribute(identifierOfAttribute(i)));
      }
    }
    GE t = tSeq.finalizeSequence();
    // multiply device-portion for t and C
    t = t.op((GE) getDeviceTValuePortion(zkBuilder));


    zkBuilder.addTValue(getIdentifier() + ":t", t);
    zkBuilder.addDValue(identifierOfCommitment(), commitment);
    zkBuilder.addNValue(getIdentifier() + ":groupDescription", group.getGroupDescription());
  }

  @Override
public GE recoverCommitment() {
    return (GE) commitment;
  }

  @Override
public List<BigInt> recoverRandomizers() {
    return valuesOfRandomlyChosenAttributes;
  }

  protected int typeOfProofOnDevice = 0;
  private final int NO_DEVICE = 0;
  private final int DEVICE_PROOF = 1;
  private final int SCOPE_EXCLUSIVE_PSEUDONYM_PROOF = 2;
  private final int ONE_BASE_CREDENTIAL_PROOF = 3;
  private final int TWO_BASE_CREDENTIAL_PROOF = 4;
  protected BigInt rOffset;

  protected void findProofTypeAndIndicesOfExternalVariables(ZkProofStateInitialize zkBuilder) {
    /*
     * find the indices of those attributes which have to be chosen randomly. also, the indices of
     * the device secret and randomizer are determined
     */
    for (int i = 0; i < bases.size(); i++) {
      if (bases.get(i).chooseExponentRandomly) {
        indicesOfRandomlyChosenAttributes.add(i);
      } else if (bases.get(i).hasExternalSecret) {
        indexOfSecret = i;
      } else if (bases.get(i).hasCredentialSecretKey) {
        indexOfExternalRandomizer = i;
      }
    }

    /*
     * The next determines which kind of proof has to be performed:
     * 
     * typeOfProofOnDevice = NO_DEVICE: if no device is specified, the device does not have a
     * contribution
     * 
     * typeOfProofOnDevice = DEVICE_PROOF: if no device-credential is used and no scope is
     * specified, a proof for the device-public-key is to be performed.
     * 
     * typeOfProofOnDevice = SCOPE_EXCLUSIVE_PSEUDONYM_PROOF: if there is a non-null scope, a
     * scope-exclusive pseudonym has to be proved.
     * 
     * typeOfProofOnDevice = ONE_BASE_CREDENTIAL_PROOF: if a device-credential but no external
     * randomizer is specified, a single-base credential has to be proved.
     * 
     * typeOfProofOnDevice = TWO_BASE_CREDENTIAL_PROOF: otherwise, a device-credential with external
     * randomizer is given, and a two-base credential has to be proved.
     */
    if (deviceUid == null) {
      typeOfProofOnDevice = NO_DEVICE;
    } else if (identifierOfCredentialForSecret == null && scope == null) {
      typeOfProofOnDevice = DEVICE_PROOF;
    } else if (scope != null) {
      typeOfProofOnDevice = SCOPE_EXCLUSIVE_PSEUDONYM_PROOF;
    } else if (identifierOfCredentialForSecret != null && indexOfExternalRandomizer == -1) {
      typeOfProofOnDevice = ONE_BASE_CREDENTIAL_PROOF;
    } else {
      typeOfProofOnDevice = TWO_BASE_CREDENTIAL_PROOF;
    }

    /*
     * register the proof
     */
    if (typeOfProofOnDevice == DEVICE_PROOF) {
      zkBuilder.getDeviceProofSpecification().addPublicKeyProof(deviceUid);
    } else if (typeOfProofOnDevice == SCOPE_EXCLUSIVE_PSEUDONYM_PROOF) {
      zkBuilder.getDeviceProofSpecification().addScopeExclusivePseudonymProof(deviceUid, scope);
    } else if (typeOfProofOnDevice == ONE_BASE_CREDENTIAL_PROOF
        || typeOfProofOnDevice == TWO_BASE_CREDENTIAL_PROOF) {
      zkBuilder.getDeviceProofSpecification().addCredentialProof(deviceUid,
          identifierOfCredentialForSecret);
    }
  }

  protected List<BigInt> getDeviceSValues(final BigInt c, final ZkProofStateSecondRound zkBuilder) {
    final List<BigInt> sValuesFromDevice = new ArrayList<BigInt>();
    if (typeOfProofOnDevice == NO_DEVICE) {
      return sValuesFromDevice;
    }

    sValuesFromDevice.add(bigIntFactory.valueOf(zkBuilder.getDeviceProofResponse()
        .getResponseForDeviceSecretKey(deviceUid)));

    // For two-base credentials, we have to adjust the response for the randomizer depending on its
    // offset
    if (typeOfProofOnDevice == TWO_BASE_CREDENTIAL_PROOF) {
      BigInt sOfCredRandomizer =
          bigIntFactory.valueOf(zkBuilder.getDeviceProofResponse()
              .getResponseForCredentialSecretKey(deviceUid, identifierOfCredentialForSecret));
      if (bases.get(indexOfExternalRandomizer).externalRandomizerOffset != null) {
        // -c*externalRandomizerOffset + rOffset
        BigInt offSetShift = c.negate();
        offSetShift =
            offSetShift.multiply(bases.get(indexOfExternalRandomizer).externalRandomizerOffset);
        offSetShift = offSetShift.add(rOffset);
        sOfCredRandomizer = sOfCredRandomizer.add(offSetShift);
      }
      sValuesFromDevice.add(sOfCredRandomizer);
    }
    // TODO check randomizerSizeByte and co.

    return sValuesFromDevice;
  }

  //@SuppressWarnings({"rawtypes", "unchecked"})
  @SuppressWarnings("unchecked")
  protected GE getDeviceCValuePortion(final String username, final ZkProofStateFirstRound zkBuilder)
      throws ConfigurationException, ProofException {
    GE CValuePortion = group.neutralElement();
    final BigInt cPrime;
    switch (typeOfProofOnDevice) {
      case NO_DEVICE: // NO DEVICE IN USE
        break;
      case DEVICE_PROOF: // PROVING DEVICE PUBLIC KEY
        cPrime = bigIntFactory.valueOf(esManager.getDevicePublicKey(username, deviceUid));
        CValuePortion = group.valueOfNoCheck(cPrime);
        break;
      case SCOPE_EXCLUSIVE_PSEUDONYM_PROOF: // SCOPE EXCLUSIVE PSEUDONYM
        cPrime = bigIntFactory.valueOf(esManager.getScopeExclusivePseudonym(username, deviceUid, scope));
        CValuePortion = group.valueOf(cPrime);
        break;
      case ONE_BASE_CREDENTIAL_PROOF:// PROVING ONE BASE CREDENTIAL
        cPrime =
            bigIntFactory.valueOf(esManager.getCredentialPublicKey(username, deviceUid,
                identifierOfCredentialForSecret));
        CValuePortion = group.valueOf(cPrime);
        break;
      case TWO_BASE_CREDENTIAL_PROOF:// PROVING TWO BASE CREDENTIAL
        cPrime =
            bigIntFactory.valueOf(esManager.getCredentialPublicKey(username, deviceUid,
                identifierOfCredentialForSecret));
        CValuePortion = group.valueOf(cPrime);
        if (bases.get(indexOfExternalRandomizer).externalRandomizerOffset != null) {
          CValuePortion =
              CValuePortion.opMultOp(
                  (GE) bases.get(indexOfExternalRandomizer).getBase(zkBuilder, group),
                  bases.get(indexOfExternalRandomizer).externalRandomizerOffset);
        }
        break;
    }
    return group.valueOf(CValuePortion.toBigInt()); // TODO ???
  }

  /*
   * CHECK: If a secret is used, exactly one base must have hasExternalSecret set, and at most one
   * base may be a credential secret key, which further may have non-null externalRandomizerOffset.
   */
  protected void checkConfiguration(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException {
    int numOfHasExtSecret = 0;
    int numIsCredentialSecretKey = 0;
    String e = null;
    for (int i = 0; i < bases.size(); i++) {
      if (bases.get(i).hasExternalSecret) {
        numOfHasExtSecret++;
        if (bases.get(i).chooseExponentRandomly) {
          e = "External secret must not be chosen by building block!";
        }
        if (zkBuilder.isRevealedAttribute(identifierOfAttribute(i))) {
          e = "External secret must not be revealed!";
        }
      }
      if (bases.get(i).hasCredentialSecretKey) {
        numIsCredentialSecretKey++;
        if (bases.get(i).chooseExponentRandomly) {
          e = "Credential secret key must not be chosen by building block!";
        }
        if (zkBuilder.isRevealedAttribute(identifierOfAttribute(i))) {
          e = "External randomizer must not be revealed!";
        }
      } else if (bases.get(i).externalRandomizerOffset != null) {
        e = "A non-credential-secret-key has an external randomizer offset set!";
      }
    }
    if ((!(deviceUid == null)) && (numOfHasExtSecret != 1 || numIsCredentialSecretKey > 1)) {
      e = "Too many bases have external secrets or non-null external randomizer offsets!";
    }
    if ((deviceUid == null) && (numOfHasExtSecret > 0 || numIsCredentialSecretKey > 0)) {
      e = "Bases with external or credential secret keys were given, but no device was specified!";
    }
    if (e != null) {
      throw new ConfigurationException(e + "(" + getBuildingBlockId() + ")");
    }
    // TODO: check bases are the same for external device and specified bases
  }

  protected abstract GroupElement<?, ?, ?> getDeviceTValuePortion(final ZkProofStateFirstRound zkBuilder)
      throws ConfigurationException, ProofException;
  
}
