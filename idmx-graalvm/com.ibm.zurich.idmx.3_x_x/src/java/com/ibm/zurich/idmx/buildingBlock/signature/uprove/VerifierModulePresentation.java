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

package com.ibm.zurich.idmx.buildingBlock.signature.uprove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

class VerifierModulePresentation extends ZkModuleImpl implements ZkModuleVerifier {

  private final GroupFactory groupFactory;

  private final EcryptSystemParametersWrapper sp;
  private final BrandsPublicKeyWrapper pk;
  private final BigInt credentialSpecificationId;
  private final int numberOfAttributes;
  private final boolean device;
  private final Logger logger;
  private final TestVectorHelper testVector;

  public VerifierModulePresentation(final BrandsSignatureBuildingBlock parent,
                                    final SystemParameters systemParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final BigInt credentialSpecificationId, final int numberOfAttributes, final boolean externalDevice,
      final GroupFactory groupFactory, final Logger logger, final TestVectorHelper testVector) {

    super(parent, identifierOfModule);

    this.groupFactory = groupFactory;

    this.sp = new EcryptSystemParametersWrapper(systemParameters);
    this.pk = new BrandsPublicKeyWrapper(issuerPublicKey);

    this.credentialSpecificationId = credentialSpecificationId;
    this.numberOfAttributes = numberOfAttributes;
    this.device = externalDevice;
    this.logger = logger;
    this.testVector = testVector;
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException {
    for (int i = 0; i < numberOfAttributes; ++i) {
      zkVerifier.registerAttribute(identifierOfAttribute(i), false);
      zkVerifier.setResidueClass(identifierOfAttribute(i), ResidueClass.RESIDUE_CLASS_MOD_Q);
    }
    if (device) {
      zkVerifier.registerAttribute(identifierOfSecretAttribute(), true);
    }
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    final KnownOrderGroup group =
        groupFactory.createPrimeOrderGroup(sp.getDHModulus(), sp.getDHSubgroupOrder());
    final KnownOrderGroupElement g0 = group.valueOfNoCheck(pk.getG0());

    // Recover D-values
    final byte[] a = zkVerifier.getDValueAsObject(getIdentifier() + ":a");
    final KnownOrderGroupElement h = zkVerifier.getDValueAsGroupElement(getIdentifier() + ":h", group);
    final KnownOrderGroupElement sigmaZPrime =
        group.valueOfNoCheck(zkVerifier.getDValueAsInteger(getIdentifier() + ":sigmaZPrime"));
    final BigInt sigmaCPrime = zkVerifier.getDValueAsInteger(getIdentifier() + ":sigmaCPrime");
    final BigInt sigmaRPrime = zkVerifier.getDValueAsInteger(getIdentifier() + ":sigmaRPrime");

    // Recover disclosed attributes
    final List<Integer> listOfDisclosed = new ArrayList<Integer>();
    final List<BigInt> valueOfDisclosed = new ArrayList<BigInt>();
    final List<BigInt> attributes = new ArrayList<BigInt>();
    for (int i = 0; i < numberOfAttributes; ++i) {
      final boolean disclosed = zkVerifier.isRevealedAttribute(identifierOfAttribute(i));
      final BigInt value;
      if (disclosed) {
        value = zkVerifier.getValueOfRevealedAttribute(identifierOfAttribute(i));
        listOfDisclosed.add(i + 1);
        valueOfDisclosed.add(value);
      } else {
        value = null;
      }
      attributes.add(value);
    }

    // Recover S-values
    final BigInt r0 = zkVerifier.getSValueAsInteger(getIdentifier() + ":r0");
    testVector.checkValue(r0, "r0");
    final List<BigInt> ri = new ArrayList<BigInt>();
    for (int i = 0; i < numberOfAttributes; ++i) {
      final BigInt sValue;
      if (attributes.get(i) == null) {
        sValue = zkVerifier.getSValueAsInteger(identifierOfAttribute(i));
        testVector.checkValue(sValue.mod(sp.getDHSubgroupOrder()), "r" + (i + 1));
      } else {
        sValue = null;
      }
      ri.add(sValue);
    }
    final BigInt rd;
    if (device) {
      rd = zkVerifier.getSValueAsInteger(identifierOfSecretAttribute());
    } else {
      rd = null;
    }

    // Recover challenge
    final BigInt c = zkVerifier.getChallenge();
    testVector.checkValue(c.mod(sp.getDHSubgroupOrder()), "c");

    // Verification - check token
    final byte[] proverInformation;
    if (zkVerifier.getDValueAsInteger(getIdentifier() + ":hasPI").intValue() != 0) {
      proverInformation = zkVerifier.getDValueAsObject(getIdentifier() + ":PI");
    } else {
      proverInformation = null;
    }
    if (!checkToken(h, sigmaZPrime, sigmaCPrime, sigmaRPrime, proverInformation)) {
      logger.warning("UProve token did not verify for module " + getIdentifier());
      return false;
    }

    // Verification - check A
    // aPreimage = (g0 * gt^xt * PROD_disclosed(gi^xi))^{-c} * h0^r0 *
    // PROD_undisclosed(gi^ri) * gr^rr * [gd^rd]
    KnownOrderGroupElement aPreimage = g0;

    final BigInt xt =
        BrandsSignatureHelper.computeXt(sp, pk, credentialSpecificationId, numberOfAttributes,
            device, testVector);
    final KnownOrderGroupElement gt = group.valueOfNoCheck(pk.getGT());
    aPreimage = aPreimage.opMultOp(gt, xt);

    for (int i = 0; i < numberOfAttributes; ++i) {
      final BigInt xi = attributes.get(i);
      if (xi != null) {
        final KnownOrderGroupElement gi = group.valueOfNoCheck(pk.getGI(i + 1));
        aPreimage = aPreimage.opMultOp(gi, xi);
      }
    }

    aPreimage = aPreimage.multOp(c.negate());

    aPreimage = aPreimage.opMultOp(h, r0);

    for (int i = 0; i < numberOfAttributes; ++i) {
      if (attributes.get(i) == null) {
        final KnownOrderGroupElement gi = group.valueOfNoCheck(pk.getGI(i + 1));
        aPreimage = aPreimage.opMultOp(gi, ri.get(i));
      }
    }

    if (device) {
      final KnownOrderGroupElement gd = group.valueOfNoCheck(pk.getGD());
      aPreimage = aPreimage.opMultOp(gd, rd);
    }

    final byte[] recomputedA = hash(aPreimage);
    if (!Arrays.equals(a, recomputedA)) {
      logger.warning("Value a in UProve proof is incorrect for module" + getIdentifier());
      return false;
    }
    // Message
    final BigInt hasMessage = zkVerifier.getDValueAsInteger(getIdentifier() + ":hasM");
    final BigInt message;
    if(hasMessage.intValue() != 0) {
      message = zkVerifier.getDValueAsInteger(getIdentifier() + ":m");
    } else {
      message = null;
    }
    
    if (!testVector.isActive()) {
      if (message != null) {
        throw new ProofException("Message not equal to xt. "
            + "This is permitted only in U-Prove compatibility mode.");
      }
    }

    // Verification - check hash contribution
    final byte[] tokenId = BrandsSignatureHelper.getTokenId(h, sigmaZPrime, sigmaCPrime, sigmaRPrime, sp);
    // As message we put the value xt
    final byte[] hashContribution =
        BrandsSignatureHelper.getHashContribution(tokenId, a, listOfDisclosed, valueOfDisclosed,
            message, sp);
    zkVerifier.checkHashContributionOfBuildingBlock(hashContribution);

    return true;
  }

  private boolean checkToken(final KnownOrderGroupElement h, final KnownOrderGroupElement sigmaZPrime,
                             final BigInt sigmaCPrime, final BigInt sigmaRPrime, final byte[] proverInformation)
      throws ConfigurationException {
    if (h.equals(h.getGroup().neutralElement())) {
      logger.warning("h is 1 in Uprove token");
      return false;
    }

    final KnownOrderGroup group = h.getGroup();
    final KnownOrderGroupElement g = group.valueOfNoCheck(sp.getDHGenerator1());
    final KnownOrderGroupElement g0 = group.valueOfNoCheck(pk.getG0());

    final KnownOrderGroupElement sigmaAPrime = g.multOp(sigmaRPrime).opMultOp(g0, sigmaCPrime.negate());
    final KnownOrderGroupElement sigmaBPrime =
        h.multOp(sigmaRPrime).opMultOp(sigmaZPrime, sigmaCPrime.negate());

    final BigInt result =
        BrandsSignatureHelper.hashToken(sp, h, proverInformation, sigmaZPrime, sigmaAPrime,
            sigmaBPrime);
    if (!result.equals(sigmaCPrime)) {
      logger.warning("sigmaCPrime is wrong in UProve token.");
      return false;
    }
    return true;
  }

  private byte[] hash(final KnownOrderGroupElement aPreimage) throws ConfigurationException {
    final BrandsSignatureHashing hashing = new BrandsSignatureHashing(sp);
    hashing.add(aPreimage);
    return hashing.digestRaw();
  }
}
