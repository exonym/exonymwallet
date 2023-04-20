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
package com.ibm.zurich.idmx.buildingBlock.inspector;

import java.math.BigInteger;
import java.net.URI;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.Decryption;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.ProverModuleDecryption;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.ProverModuleEncryption;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.VerifierModuleDecryption;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.VerifierModuleEncryption;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
//import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverVerifiableEncryption;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.keypair.inspector.InspectorKeyPairWrapper;
import com.ibm.zurich.idmx.keypair.inspector.InspectorPublicKeyWrapper;
import com.ibm.zurich.idmx.keypair.inspector.InspectorSecretKeyWrapper;
import com.ibm.zurich.idmx.parameters.inspector.InspectorPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.util.UriUtils;

import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.InspectorPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.ZkProof;


public abstract class InspectorBuildingBlock extends GeneralBuildingBlock {
  protected final RandomGeneration randomGeneration;
  protected final Logger logger;
  protected final BuildingBlockFactory bbFactory;
  protected final BigIntFactory bigIntFactory;
  protected final GroupFactory groupFactory;

  public InspectorBuildingBlock(final RandomGeneration rg, final BuildingBlockFactory bbFactory, final Logger logger,
                                final BigIntFactory bigIntFactory, final GroupFactory groupFactory)  {
    this.randomGeneration = rg;
    this.logger = logger;
    this.bbFactory = bbFactory;
    this.bigIntFactory = bigIntFactory;
    this.groupFactory = groupFactory;
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "ins";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "ins";
  }

  public ZkModuleProverVerifiableEncryption getZkModuleProverEncryption(final String identifierOfModule,
                                                                        final SystemParameters sp, final VerifierParameters vp, /* Inspector */final PublicKey ipk,
      final String idOfAttributeToEncrypt, final byte[] label) throws ConfigurationException {
    return new ProverModuleEncryption(this, identifierOfModule, sp, vp, ipk, label,
        idOfAttributeToEncrypt, bbFactory, bigIntFactory, groupFactory, randomGeneration);
  }

  public ZkModuleVerifier getZkModuleVerifierEncryption(final String identifierOfModule,
                                                        final SystemParameters sp, final VerifierParameters vp, /* Inspector */final PublicKey ipk,
      final String idOfAttributeToEncrypt, final byte[] label) throws ProofException, ConfigurationException {
    return new VerifierModuleEncryption(this, identifierOfModule, sp, vp, ipk,
        idOfAttributeToEncrypt, label, bbFactory, bigIntFactory, groupFactory, randomGeneration);
  }

  public ZkModuleProver getZkModuleProverDecryption(final String identifierOfModule, final SystemParameters sp,
                                                    final VerifierParameters vp, final InspectorPublicKey ipk, /* Inspector */final SecretKey isk,
      final String idOfAttributeToEncrypt, final byte[] ciphertext, final BigInt label) {
    return new ProverModuleDecryption(this, identifierOfModule, sp, vp, ipk, isk, ciphertext,
        label, bbFactory);
  }

  public ZkModuleVerifier getZkModuleVerifierDecryption(final String identifierOfModule,
                                                        final SystemParameters sp, final VerifierParameters vp, final InspectorPublicKey ipk,
      final String idOfAttributeToEncrypt, final BigInteger plaintext, final byte[] ciphertext, final BigInt label) {
    return new VerifierModuleDecryption(this, identifierOfModule, sp, vp, ipk, ciphertext, label,
        plaintext, bbFactory);
  }


  public BigInt getPlaintext(final PaillierGroupElement[] ciphertext, final byte[] label, /* Inspector */final PrivateKey isk)
      throws ProofException, ConfigurationException {
    final Decryption dec = new Decryption(bigIntFactory);
    return dec.decrypt(ciphertext, label, isk);
  }

  public BigInt getPlaintext(final ZkProof proof, final String identifierOfModule, final byte[] label, /* Inspector */
                             final PrivateKey isk) throws ProofException, ConfigurationException {
    final Decryption dec = new Decryption(bigIntFactory);
    return dec.decrypt(proof, identifierOfModule, label, isk, groupFactory);
  }

  @Override
  public boolean contributesToInspectorPublicKeyTemplate() {
    return true;
  }

  public abstract void addToInspectorParametersTemplate(
      InspectorPublicKeyTemplate inspectorPublicKeyTemplate);

  public KeyPair generateInspectorBuildingBlockKeyPair(final SystemParameters systemParameters,
                                                       final InspectorPublicKeyTemplate template) throws ConfigurationException {

    final InspectorKeyPairWrapper keyPair = new InspectorKeyPairWrapper();
    final InspectorPublicKeyTemplateWrapper inpkTemplateWrapper =
        new InspectorPublicKeyTemplateWrapper(template);
    final URI publicKeyId =
        UriUtils.concat(inpkTemplateWrapper.getPublicKeyPrefix(),
            randomGeneration.generateRandomUid());

    final InspectorPublicKeyWrapper pkWrapper = keyPair.getInspectorPublicKeyWrapper();
    pkWrapper.setSystemParametersId(inpkTemplateWrapper.getSystemParametersId());
    pkWrapper.setPublicKeyTechnology(inpkTemplateWrapper.getTechnology());
    pkWrapper.setPublicKeyId(publicKeyId);

    final InspectorSecretKeyWrapper skWrapper = keyPair.getInspectorPrivateKeyWrapper();
    skWrapper.setPublicKeyId(publicKeyId);

    return keyPair.getKeyPair();
  }
}
