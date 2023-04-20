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

package com.ibm.zurich.idmx.buildingBlock.factory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.inspector.InspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.SafeRSAGroupInVerifierParameters;
import com.ibm.zurich.idmx.buildingBlock.revocation.RevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.SystemParameterGeneratorBuildingBlock;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.Timing;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;

import javax.inject.Inject;

public class BuildingBlockFactory {

  /**
   * The default system parameter generator class
   */
  private static final Class<? extends SystemParameterGeneratorBuildingBlock> DEFAULT_SYSTEM_PARAMETER_GENERATOR =
      EcryptSystemParametersGenerator.class;

  private final BuildingBlockDatabase database;

  @Inject
  public BuildingBlockFactory(final BuildingBlockList bbList, CredentialManager credentialManager,
                              KeyManager keyManager) {
    this.database = new BuildingBlockDatabase(bbList.getListOfBuildingBlocks(), this,
            credentialManager, keyManager);

  }

  public SystemParameterGeneratorBuildingBlock getGeneralSystemParametersBuildingBlock()
      throws ConfigurationException {
    return getBuildingBlockByClass(DEFAULT_SYSTEM_PARAMETER_GENERATOR);
  }

  /**
   * Returns the general building block of the specified building block ID.
   * 
   * @throws ConfigurationException
   */
  public GeneralBuildingBlock getBuildingBlockById(final URI buildingBlockId)
      throws ConfigurationException {
    final GeneralBuildingBlock buildingBlock = database.get(buildingBlockId);
    if (buildingBlock == null) {
      throw new ConfigurationException(ErrorMessages.technologyNotSupported());
    }
    return buildingBlock;
  }

  /**
   * Returns the general building block of the specified class.
   * 
   * @throws ConfigurationException
   */
  public <T extends GeneralBuildingBlock> T getBuildingBlockByClass(final Class<T> clazz)
      throws ConfigurationException {

    final T buildingBlock = database.get(clazz);
    if (buildingBlock == null) {
      throw new ConfigurationException(ErrorMessages.technologyNotSupported());
    }
    return buildingBlock;
  }

  /**
   * Returns the signature building block of the specified building block ID.
   * 
   * @return
   * @throws ConfigurationException
   */
  public SignatureBuildingBlock getSignatureBuildingBlockById(URI buildingBlockId)
      throws ConfigurationException {

    final GeneralBuildingBlock buildingBlock = getBuildingBlockById(buildingBlockId);

    // Check whether the building block is of type signature building block
    verifyCastOfBuildingBlock(buildingBlock, SignatureBuildingBlock.class);

    return (SignatureBuildingBlock) buildingBlock;
  }

  /**
   * Checks whether the given building block can be cast to the given class.
   * 
   * @param buildingBlock
   * @throws ConfigurationException
   */
  private void verifyCastOfBuildingBlock(final GeneralBuildingBlock buildingBlock,
                                         final Class<?> classOfBuildingBlock) throws ConfigurationException {

    if (!classOfBuildingBlock.isAssignableFrom(buildingBlock.getClass())) {
      throw new ConfigurationException(ErrorMessages.classCastMessage(classOfBuildingBlock,
          buildingBlock.getClass()));
    }
  }



  /**
   * Returns all building blocks that may contribute to the system parameters template as well as
   * the system parameters.
   * 
   * @throws ConfigurationException
   */
  public List<SystemParameterGeneratorBuildingBlock> getSystemParametersRelevantBuildingBlocks()
      throws ConfigurationException {

    final List<SystemParameterGeneratorBuildingBlock> bbList =
        new ArrayList();
    for (final Class<? extends GeneralBuildingBlock> clazz : database.getList()) {
      final GeneralBuildingBlock buildingBlock = database.get(clazz);
      // Signature building blocks may add their own parameters to the template
      if (buildingBlock.contributesToSystemParameters()) {
        if (SystemParameterGeneratorBuildingBlock.class.isAssignableFrom(buildingBlock.getClass())) {
          bbList.add((SystemParameterGeneratorBuildingBlock) buildingBlock);
        } else {
          throw new ConfigurationException(ErrorMessages.classCastMessage(
              SystemParameterGeneratorBuildingBlock.class, buildingBlock.getClass()));
        }
      }
    }
    return bbList;
  }

  /**
   * Returns all building blocks that may contribute to the issuer public key template.
   */
  public List<SignatureBuildingBlock> getIssuerKeyRelevantBuildingBlocks()
      throws ConfigurationException {

    final List<SignatureBuildingBlock> bbList = new ArrayList<SignatureBuildingBlock>();
    for (final Class<? extends GeneralBuildingBlock> clazz : database.getList()) {
      final GeneralBuildingBlock buildingBlock = database.get(clazz);
      // Signature building blocks may add their own parameters to the template
      if (buildingBlock.contributesToIssuerKeyTemplate()) {
        if (SignatureBuildingBlock.class.isAssignableFrom(buildingBlock.getClass())) {
          bbList.add((SignatureBuildingBlock) buildingBlock);
        } else {
          throw new ConfigurationException(ErrorMessages.classCastMessage(
              SignatureBuildingBlock.class, buildingBlock.getClass()));
        }
      }
    }
    return bbList;
  }

  /**
   * Returns all building blocks that may contribute to the inspector public key template.
   */
  public List<InspectorBuildingBlock> getInspectorKeyRelevantBuildingBlocks()
      throws ConfigurationException {

    final List<InspectorBuildingBlock> bbList = new ArrayList<InspectorBuildingBlock>();
    for (final Class<? extends GeneralBuildingBlock> clazz : database.getList()) {
      final GeneralBuildingBlock buildingBlock = database.get(clazz);
      // Inspector building blocks may add their own parameters to the template
      if (buildingBlock.contributesToInspectorPublicKeyTemplate()) {
        if (InspectorBuildingBlock.class.isAssignableFrom(buildingBlock.getClass())) {
          bbList.add((InspectorBuildingBlock) buildingBlock);
        } else {
          throw new ConfigurationException(ErrorMessages.classCastMessage(
              InspectorBuildingBlock.class, buildingBlock.getClass()));
        }
      }
    }
    return bbList;
  }

  /**
   * Returns all building blocks that may contribute to the revocation public key template.
   * 
   * @throws ConfigurationException
   */
  public List<RevocationBuildingBlock> getRevocationRelevantBuildingBlocks()
      throws ConfigurationException {

    final List<RevocationBuildingBlock> bbList = new ArrayList<RevocationBuildingBlock>();
    for (Class<? extends GeneralBuildingBlock> clazz : database.getList()) {
      final GeneralBuildingBlock buildingBlock = database.get(clazz);
      // Revocation building blocks may add their own parameters to the template
      if (buildingBlock.contributesToRevocationPublicKeyTemplate()) {
        if (RevocationBuildingBlock.class.isAssignableFrom(buildingBlock.getClass())) {
          bbList.add((RevocationBuildingBlock) buildingBlock);
        } else {
          throw new ConfigurationException(ErrorMessages.classCastMessage(
              RevocationBuildingBlock.class, buildingBlock.getClass()));
        }
      }
    }
    return bbList;
  }
  
  /**
   * Returns all building blocks that should be added to the verifier parameters.
   * 
   * @throws ConfigurationException
   */
  public List<GeneralBuildingBlock> getAllBuildingBlocks()
      throws ConfigurationException {
    final List<GeneralBuildingBlock> bbList = new ArrayList<GeneralBuildingBlock>();
    for (final Class<? extends GeneralBuildingBlock> clazz : database.getList()) {
      bbList.add(database.get(clazz));
    }
    return bbList;
  }
  
  /**
   * Returns all building blocks that may contribute to the verifier parameters.
   * 
   * @throws ConfigurationException
   */
  public List<GeneralBuildingBlock> getVerifierParameterRelevantBuildingBlocks()
      throws ConfigurationException {

    final List<GeneralBuildingBlock> bbList = new ArrayList<GeneralBuildingBlock>();
    for (final Class<? extends GeneralBuildingBlock> clazz : database.getList()) {
      final GeneralBuildingBlock buildingBlock = database.get(clazz);
      if (buildingBlock.contributesToVerifierParameterTemplate()) {
        bbList.add(database.get(clazz));
      }
    }
    return bbList;
  }

  // TODO Check risk profile
  public ExternalSecretsHelper getExternalSecretsHelper() {
    return database.getExternalSecretsHelper();
  }

  public ExternalSecretsManager getExternalSecretsManager() {
    return database.getExternalSecretsManager();
  }

  public SafeRSAGroupInVerifierParameters getSafeRSAGroupInVerifierParameters() {
    return database.getSafeRSAGroupInVerifierParameters();
  }

  public RandomGeneration getRandomGeneration(){return database.getRandomGeneration();}
  public BigIntFactory getBigIntFactory(){return database.getBigIntFactory();}
  public Timing getTiming(){return database.getTiming();}
  public GroupFactory getGroupFactory(){return database.getGroupFactory();}
  public KeyManager getKeyManager(){return database.getKeyManager();}
  public CredentialManager getCredentialManagerUser(){return database.getCredentialManagerUser();}
  public TestVectorHelper getTestVectorHelper(){return database.getTestVectorHelper();}
  public HashComputationForChallenge getHashComputationForChallenge(){return database.getHashComputationForChallenge();}
  public Logger getLogger(){return database.getLogger();}

}
