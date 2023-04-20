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
package com.ibm.zurich.idmx.buildingBlock.inspector.cs;

import java.util.logging.Logger;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.inspector.InspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
//import com.ibm.zurich.idmx.parameters.inspector.InspectorPublicKeyTemplateWrapper;

import eu.abc4trust.xml.InspectorPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

public class CsInspectorBuildingBlock extends InspectorBuildingBlock {
  //TODO(ksa) externalize
  private static final int SECURITY_PARAMETER = 80;

  @Inject
  public CsInspectorBuildingBlock(final RandomGeneration rg, final BuildingBlockFactory bbFactory,
                                  final BigIntFactory bigIntFactory, final GroupFactory groupFactory) {
    super(rg, bbFactory, Logger.getGlobal(), bigIntFactory, groupFactory);

  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return super.getBuildingBlockIdSuffix().concat(":cs");
  }

  @Override
  public final String getImplementationIdSuffix() {
    return super.getBuildingBlockIdSuffix().concat(":cs");
  }

  @Override
  public void addToInspectorParametersTemplate(final InspectorPublicKeyTemplate inspectorPublicKeyTemplate) {
    // nothing to do here
  }

  @Override
public KeyPair generateInspectorBuildingBlockKeyPair(final SystemParameters systemParameters,
                                                     final InspectorPublicKeyTemplate template) throws ConfigurationException {
    final KeyPair keyPair = super.generateInspectorBuildingBlockKeyPair(systemParameters, template);
    final CsKeyPairWrapper kpWrapper = new CsKeyPairWrapper(keyPair);
//    InspectorPublicKeyTemplateWrapper ipkTemplateWrapper =
//        new InspectorPublicKeyTemplateWrapper(template);

//    final EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);

//    final int rsaModulusLength =
//        (Integer) spWrapper.getParameter(EcryptSystemParametersGenerator.RSA_MODULUS_LENGTH_NAME);
//    final int primeProbability = spWrapper.getPrimeProbability();

    final CsSecretKeyWrapper skWrapper = kpWrapper.getCSSecretKeyWrapper();
    final CsPublicKeyWrapper pkWrapper = kpWrapper.getCSPublicKeyWrapper();

    generateEncryptionKeyPair(pkWrapper, skWrapper, systemParameters);
    generateAuxiliaryKeys(pkWrapper, skWrapper, systemParameters);

    return keyPair;
  }

  @SuppressWarnings("rawtypes")
  private void generateEncryptionKeyPair(final CsPublicKeyWrapper pkWrapper,
                                         final CsSecretKeyWrapper skWrapper, final SystemParameters systemParameters)
      throws ConfigurationException {

    final EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    // compute random RSA modulus
    final int primeCertainty = spWrapper.getPrimeProbability();
    final int modulusLength =
        (Integer) spWrapper.getParameter(EcryptSystemParametersGenerator.RSA_MODULUS_LENGTH_NAME);
    final BigInt n = getSafeRSAModulus(randomGeneration, modulusLength, primeCertainty);
    pkWrapper.setModulus(n);
    skWrapper.setModulus(n);

    // choose g'
    final PaillierGroup group = groupFactory.createPaillierGroup(n);
    final GroupElement gPrime = group.createRandomElement(randomGeneration);

    // g = g'^(2n)
    final GroupElement g = gPrime.multOp(n.add(n));
    pkWrapper.setG(g.toBigInt());

    // y_i = g^(x_i), where x_i is random in [0 .. n^2/4]
    final BigInt nSquareOver4 = n.multiply(n).shiftRight(2);
    final BigInt x_1 = randomGeneration.generateRandomNumber(nSquareOver4);
    final BigInt x_2 = randomGeneration.generateRandomNumber(nSquareOver4);
    final BigInt x_3 = randomGeneration.generateRandomNumber(nSquareOver4);
    final GroupElement y_1 = g.multOp(x_1);
    final GroupElement y_2 = g.multOp(x_2);
    final GroupElement y_3 = g.multOp(x_3);
    pkWrapper.setY1(y_1.toBigInt());
    pkWrapper.setY2(y_2.toBigInt());
    pkWrapper.setY3(y_3.toBigInt());
    skWrapper.setX1(x_1);
    skWrapper.setX2(x_2);
    skWrapper.setX3(x_3);

    // set hash function
    pkWrapper.setHashFunction("SHA-256");
    skWrapper.setHashFunction("SHA-256");

    // choose hash key
    final BigInt hk = randomGeneration.generateRandomNumber(SECURITY_PARAMETER);
    pkWrapper.setHashKey(hk);
    skWrapper.setHashKey(hk);
  }

  private void generateAuxiliaryKeys(final CsPublicKeyWrapper pkWrapper, final CsSecretKeyWrapper skWrapper,
                                     final SystemParameters systemParameters) throws ConfigurationException {

    final EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    // compute random auxiliary RSA modulus
    final int primeCertainty = spWrapper.getPrimeProbability();
    final int modulusLength =
        (Integer) spWrapper.getParameter(EcryptSystemParametersGenerator.RSA_MODULUS_LENGTH_NAME);
    final BigInt auxN = getSafeRSAModulus(randomGeneration, modulusLength, primeCertainty);
    pkWrapper.setAuxiliaryModulus(auxN);
    skWrapper.setAuxiliaryModulus(auxN);

    final HiddenOrderGroup group = groupFactory.createSignedQuadraticResiduesGroup(auxN);
    final HiddenOrderGroupElement auxG = group.createRandomGenerator(randomGeneration);
    final HiddenOrderGroupElement auxH = group.createRandomGenerator(randomGeneration);

    pkWrapper.setAuxiliaryG(auxG.toBigInt());
    pkWrapper.setAuxiliaryH(auxH.toBigInt());
    skWrapper.setAuxiliaryG(auxG.toBigInt());
    skWrapper.setAuxiliaryH(auxH.toBigInt());
  }

  /**
   * To return a pair of p, q such that n = p*q and p = 2p' + 1, q = 2q' + 1 with p, q, p', q'
   * prime.
   * 
   * @param modulusLength Length of modulus.
   * @param primeCertainty Probability for prime testing.
   * 
   */
  private static final BigInt getSafeRSAModulus(final RandomGeneration rg, final int modulusLength,
                                                final int primeCertainty) {
    BigInt p, n, q;
    //TODO(ksa) |n| < 512/1024 ?
    do {
      p = rg.generateRandomSafePrime(modulusLength / 2, primeCertainty);
      do {
        q = rg.generateRandomSafePrime(modulusLength - (modulusLength / 2), primeCertainty);
        // make sure p != q
      } while (p.equals(q));
      n = p.multiply(q);
    } while (n.bitLength() != modulusLength);
    return n;
  }
}
