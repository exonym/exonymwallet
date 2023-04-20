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

package com.ibm.zurich.idmx.orchestration;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.inspector.InspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.revocation.RevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.SystemParameterGeneratorBuildingBlock;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.parameters.inspector.InspectorPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.parameters.issuer.IssuerPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.parameters.ra.RevocationAuthorityPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersTemplateWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.IdemixVerifierParameters;
import eu.abc4trust.xml.InspectorPublicKeyTemplate;
import eu.abc4trust.xml.IssuerPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate;
import eu.abc4trust.xml.SupportedBuildingBlock;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.SystemParametersTemplate;
import eu.abc4trust.xml.VerifierParametersTemplate;

import javax.inject.Inject;

/**
 * 
 */
public class KeyGenerationOrchestrationImpl implements KeyGenerationOrchestration {

  private final BuildingBlockFactory buildingBlockFactory;
  private final KeyManager keyManager;
  private final RandomGeneration randomGeneration;

  @Inject
  public KeyGenerationOrchestrationImpl(final BuildingBlockFactory buildingBlockFactory,
                                        final KeyManager keyManager, final RandomGeneration randomGeneration) {
    this.buildingBlockFactory = buildingBlockFactory;
    this.keyManager = keyManager;
    this.randomGeneration = randomGeneration;
  }

  @Override
  public SystemParametersTemplate createSystemParametersTemplate() throws ConfigurationException {

    final SystemParametersTemplateWrapper templateWrapper =
        new SystemParametersTemplateWrapper(new ObjectFactory().createSystemParametersTemplate());

    // Set the system required system parameters
    templateWrapper.setSystemParametersId(URI.create(randomGeneration.generateRandomUid()));

    // Set the basic system parameters using the Ecrypt parameter generator
    final SystemParameterGeneratorBuildingBlock systemParametersBuildingBlock =
        buildingBlockFactory.getGeneralSystemParametersBuildingBlock();
    systemParametersBuildingBlock.addBuildingBlockSystemParametersTemplate(templateWrapper
        .getSystemParametersTemplate());

    // Get the template values from the building blocks
    final List<SystemParameterGeneratorBuildingBlock> listOfSignatureBuildingBlocks =
        buildingBlockFactory.getSystemParametersRelevantBuildingBlocks();
    for (final SystemParameterGeneratorBuildingBlock bb : listOfSignatureBuildingBlocks) {
      bb.addBuildingBlockSystemParametersTemplate(templateWrapper.getSystemParametersTemplate());
    }

    return templateWrapper.getSystemParametersTemplate();
  }

  @Override
  public SystemParameters setupSystemParameters(final SystemParametersTemplate template)
      throws ConfigurationException {

    final SystemParametersTemplateWrapper spTemplateWrapper =
        new SystemParametersTemplateWrapper(template);

    final List<SystemParameterGeneratorBuildingBlock> listOfSignatureBuildingBlocks =
        buildingBlockFactory.getSystemParametersRelevantBuildingBlocks();

    // Create system parameters
    final SystemParametersWrapper spWrapper = new SystemParametersWrapper();

    // Set the system required system parameters
    spWrapper.setSystemParametersId(spTemplateWrapper.getSystemParametersId());

    // Set the basic system parameters using the Ecrypt system parameters generator
    final SystemParameterGeneratorBuildingBlock systemParametersBuildingBlock =
        buildingBlockFactory.getGeneralSystemParametersBuildingBlock();
    systemParametersBuildingBlock.generateBuildingBlockSystemParameters(
        spTemplateWrapper.getSystemParametersTemplate(), spWrapper.getSystemParameters());

    // Let the building blocks contribute their parameters
    for (final SystemParameterGeneratorBuildingBlock bb : listOfSignatureBuildingBlocks) {
      bb.generateBuildingBlockSystemParameters(template, spWrapper.getSystemParameters());
    }

    spWrapper.setImplementationVersion(Constants.IMPLEMENTATION_VERSION);
    return spWrapper.getSystemParameters();
  }

  @Override
  public IssuerPublicKeyTemplate createIssuerPublicKeyTemplate() throws ConfigurationException,
      KeyManagerException {

    final SystemParametersWrapper spWrapper =
        new SystemParametersWrapper(keyManager.getSystemParameters());

    final IssuerPublicKeyTemplateWrapper ipkTemplateWrapper = new IssuerPublicKeyTemplateWrapper();

    // Set the general issuer template parameter values
    ipkTemplateWrapper.setSystemParametersId(spWrapper.getSystemParametersId());
    ipkTemplateWrapper.setTechnology(Configuration.defaultSignatureTechnology());

    // Set further parameters with default values (to make them show up in the template)
    ipkTemplateWrapper.setMaximalNumberOfAttributes(Configuration
        .defaultMaximalNumberOfAttributes());
    ipkTemplateWrapper.setPublicKeyPrefix(Configuration.defaultIssuerPublicKeyPrefix());

    final List<SignatureBuildingBlock> listOfSignatureBuildingBlocks =
        buildingBlockFactory.getIssuerKeyRelevantBuildingBlocks();
    for (final SignatureBuildingBlock bb : listOfSignatureBuildingBlocks) {
      bb.addToIssuerParametersTemplate(ipkTemplateWrapper.getIssuerPublicKeyTemplate());
    }

    return ipkTemplateWrapper.getIssuerPublicKeyTemplate();
  }

  @Override
  public KeyPair setupIssuerKeyPair(final SystemParameters systemParameters,
                                    final IssuerPublicKeyTemplate issuerParametersTemplate) throws KeyManagerException,
      ConfigurationException, CredentialManagerException {

    final IssuerPublicKeyTemplateWrapper templateFacade =
        new IssuerPublicKeyTemplateWrapper(issuerParametersTemplate);

    // Set the technology-specific parameters
    final URI technology = templateFacade.getTechnology();
    final SignatureBuildingBlock issuersParametersBuildingBlock =
        buildingBlockFactory.getSignatureBuildingBlockById(technology);

    final KeyPair issuerKeyPair =
        issuersParametersBuildingBlock.generateBuildingBlockIssuerKeyPair(systemParameters,
            issuerParametersTemplate);

    // TODO remove the version!
    issuerKeyPair.getPublicKey().setVersion(Constants.IMPLEMENTATION_VERSION);

    return issuerKeyPair;
  }


  @Override
  public RevocationAuthorityPublicKeyTemplate createRevocationAuthorityPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException {

    final SystemParametersWrapper spWrapper =
        new SystemParametersWrapper(keyManager.getSystemParameters());

    final RevocationAuthorityPublicKeyTemplateWrapper rapkTemplateWrapper =
        new RevocationAuthorityPublicKeyTemplateWrapper();

    // Set the general issuer template parameter values
    rapkTemplateWrapper.setSystemParametersId(spWrapper.getSystemParametersId());
    // Set further parameters with default values (to make them show up in the template)
    rapkTemplateWrapper.setTechnology(Configuration.defaultRevocationTechnology());

    final List<RevocationBuildingBlock> listOfRevocationBuildingBlocks =
        buildingBlockFactory.getRevocationRelevantBuildingBlocks();
    for (final RevocationBuildingBlock bb : listOfRevocationBuildingBlocks) {
      bb.addToRevocationParametersTemplate(rapkTemplateWrapper
          .getRevocationAuthorityPublicKeyTemplate());
    }

    return rapkTemplateWrapper.getRevocationAuthorityPublicKeyTemplate();
  }

  @Override
  public KeyPair setupRevocationAuthorityKeyPair(final SystemParameters systemParameters,
                                                 final RevocationAuthorityPublicKeyTemplate revocationAuthorityPublicKeyTemplate)
      throws ConfigurationException {


    final RevocationAuthorityPublicKeyTemplateWrapper templateFacade =
        new RevocationAuthorityPublicKeyTemplateWrapper(revocationAuthorityPublicKeyTemplate);

    // Set the technology-specific parameters
    final URI technology = templateFacade.getTechnology();
    final RevocationBuildingBlock revocationParametersBuildingBlock =
        (RevocationBuildingBlock) buildingBlockFactory.getBuildingBlockById(technology);

    final KeyPair raKeyPair =
        revocationParametersBuildingBlock.generateBuildingBlockRevocationAuthorityKeyPair(
            systemParameters, revocationAuthorityPublicKeyTemplate);

    return raKeyPair;
  }

  @Override
  public InspectorPublicKeyTemplate createInspectorPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException {
    final SystemParametersWrapper spWrapper =
        new SystemParametersWrapper(keyManager.getSystemParameters());

    final InspectorPublicKeyTemplateWrapper ipkTemplateWrapper = new InspectorPublicKeyTemplateWrapper();

    // Set the general inspector template parameter values
    ipkTemplateWrapper.setSystemParametersId(spWrapper.getSystemParametersId());
    ipkTemplateWrapper.setTechnology(Configuration.defaultSignatureTechnology());

    // Set further parameters with default values (to make them show up in the template)
    ipkTemplateWrapper.setPublicKeyPrefix(Configuration.defaultInspectorPublicKeyPrefix());

    final List<InspectorBuildingBlock> listOfInspectorBuildingBlocks =
        buildingBlockFactory.getInspectorKeyRelevantBuildingBlocks();
    for (final InspectorBuildingBlock bb : listOfInspectorBuildingBlocks) {
      bb.addToInspectorParametersTemplate(ipkTemplateWrapper.getInspectorPublicKeyTemplate());
    }

    return ipkTemplateWrapper.getInspectorPublicKeyTemplate();
  }

  @Override
  public KeyPair setupInspectorKeyPair(final SystemParameters systemParameters,
                                       final InspectorPublicKeyTemplate inspectorPublicKeyTemplate) throws ConfigurationException {

    final InspectorPublicKeyTemplateWrapper templateFacade =
        new InspectorPublicKeyTemplateWrapper(inspectorPublicKeyTemplate);

    // Set the technology-specific parameters
    final URI technology = templateFacade.getTechnology();
    final InspectorBuildingBlock inspectorParametersBuildingBlock =
        (InspectorBuildingBlock) buildingBlockFactory.getBuildingBlockById(technology); // TODO ???
                                                                                        // FIXME

    final KeyPair issuerKeyPair =
        inspectorParametersBuildingBlock.generateInspectorBuildingBlockKeyPair(systemParameters,
            inspectorPublicKeyTemplate);

    issuerKeyPair.getPublicKey().setVersion(Constants.IMPLEMENTATION_VERSION);

    return issuerKeyPair;
  }

  @Override
  public VerifierParametersTemplate generateVerifierParameterConfigurationTemplate()
      throws ConfigurationException, KeyManagerException {

    final VerifierParametersTemplate vpt = new ObjectFactory().createVerifierParametersTemplate();
    final SystemParametersWrapper spWrapper =
        new SystemParametersWrapper(keyManager.getSystemParameters());
    vpt.setSystemParametersId(spWrapper.getSystemParametersId());
    vpt.setVersion(Constants.IMPLEMENTATION_VERSION);
    vpt.setVerifierParametersPrefix(URI.create("vp/"));

    for (final GeneralBuildingBlock gbb : buildingBlockFactory.getAllBuildingBlocks()) {
      SupportedBuildingBlock sbb = new ObjectFactory().createSupportedBuildingBlock();
      sbb.setBlockId(gbb.getBuildingBlockId());
      sbb.setValue(gbb.getImplementationId());
      vpt.getSupportedBuildingBlock().add(sbb);
    }

    for (final GeneralBuildingBlock gbb : buildingBlockFactory
        .getVerifierParameterRelevantBuildingBlocks()) {
      gbb.populateVerifierParameterTemplate(spWrapper, vpt.getParameter());
    }

    return vpt;
  }

  @Override
  public IdemixVerifierParameters generateVerifierParameters(final SystemParameters systemParameters,
                                                             final VerifierParametersTemplate verifierParametersTemplate) throws ConfigurationException {
    final SystemParametersWrapper spWrapper = new SystemParametersWrapper(systemParameters);
    final IdemixVerifierParameters ivp = new ObjectFactory().createIdemixVerifierParameters();
    ivp.setSystemParametersId(systemParameters.getSystemParametersUID());
    ivp.setVersion(verifierParametersTemplate.getVersion());
    ivp.setVerifierParametersId(URI.create(verifierParametersTemplate.getVerifierParametersPrefix()
        .toString() + UUID.randomUUID().toString()));
    for (final SupportedBuildingBlock s : verifierParametersTemplate.getSupportedBuildingBlock()) {
      ivp.getSupportedBuildingBlock().add(s);
      final GeneralBuildingBlock gbb = buildingBlockFactory.getBuildingBlockById(s.getBlockId());
      if (gbb.contributesToVerifierParameterTemplate()) {
        gbb.populateVerifierParameters(spWrapper, verifierParametersTemplate, ivp.getParameter());
      }
    }

    return ivp;
  }

}
