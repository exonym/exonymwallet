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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.modNSquare.ModNSquareRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.CsInspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.notEqual.inv.InverseAttributeNotEqualBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.SafeRSAGroupInVerifierParameters;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq.FourSquaresRangeProofBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.setMembership.cg.CgAttributeSetMembershipBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.abc4TrustMessage.Abc4TrustMessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.attributeSource.AttributeSourceBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.constant.ConstantBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.credentialSpecification.CredentialSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.inspectorKey.InspectorPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombination.LinearCombinationLightBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombinationModQ.LinearCombinationModQBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.mechanismSpecification.MechanismSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.message.MessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.presentationTokenDescription.PresentationTokenDescriptionBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.SystemParameterGeneratorBuildingBlock;
import com.ibm.zurich.idmx.dagger.BasisComponent;
import com.ibm.zurich.idmx.dagger.DaggerBasisComponent;
import com.ibm.zurich.idmx.device.ExternalSecretsHelperImpl;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
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

/**
 * This class uses lazy initialization to break circular dependencies in Guice.
 *
 * Guice can EaD and who thinks circular dependencies are a good idea?
 */
class BuildingBlockDatabase {

  private final Map<URI, GeneralBuildingBlock> buildingBlocks;

  private final CredentialManager credentialManager;
  private final KeyManager keyManager;
  private final Map<Class<? extends GeneralBuildingBlock>, GeneralBuildingBlock> buildingBlocksByClass;
  private final List<Class<? extends GeneralBuildingBlock>> listOfBuildingBlocks;
  private final BasisComponent basis;

  private ExternalSecretsHelper externalSecretsHelper = null;
  private ExternalSecretsManager externalSecretsManager = null;

  private SafeRSAGroupInVerifierParameters safeRSAGroupInVerifierParameters;

//  private final Injector injector;
  private boolean isInitialized;
  private final BuildingBlockFactory bbf;

  private final HashMap<URI, Class<?>> blockURNToClassMap;

  public BuildingBlockDatabase(final List<Class<? extends GeneralBuildingBlock>> list, BuildingBlockFactory bbf,
                               CredentialManager credentialManager, KeyManager keyManager) {
    this.buildingBlocks = new HashMap<URI, GeneralBuildingBlock>();
    this.buildingBlocksByClass =
        new HashMap<Class<? extends GeneralBuildingBlock>, GeneralBuildingBlock>();
    this.listOfBuildingBlocks = list;
    this.keyManager=keyManager;
    this.credentialManager = credentialManager;
    this.basis = DaggerBasisComponent.builder().build();
    this.bbf=bbf;
    blockURNToClassMap = defineMap();

    isInitialized = false;

  }

  // Forced down this bug risky path.
  private HashMap<URI, Class<?>> defineMap() {
    HashMap<URI, Class<?>> result = new HashMap<>();

    /*
    urn:idmx:3.0.0:block:spGen
    urn:idmx:3.0.0:block:sig
    urn:idmx:3.0.0:block:sig:uprove
    urn:idmx:3.0.0:block:pseudonym:scopeExclusive
    urn:idmx:3.0.0:block:pseudonym:standard
    urn:idmx:3.0.0:block:h-rep-pedersen
    urn:idmx:3.0.0:block:h-rep-df
    urn:idmx:3.0.0:block:h-rep-paillier
    urn:idmx:3.0.0:block:revocation
    urn:idmx:3.0.0:block:ins:cs
    urn:idmx:3.0.0:block:s-constant
    urn:idmx:3.0.0:block:s-source
    urn:idmx:3.0.0:block:s-eq
    urn:idmx:3.0.0:block:s-param-iss
    urn:idmx:3.0.0:block:s-param-ins
    urn:idmx:3.0.0:block:s-param-ra
    urn:idmx:3.0.0:block:s-linear
    urn:idmx:3.0.0:block:s-linear-modq
    urn:idmx:3.0.0:block:s-ms
    urn:idmx:3.0.0:block:s-m
    urn:idmx:3.0.0:block:s-reveal
    urn:idmx:3.0.0:block:s-param-sys
    urn:idmx:3.0.0:block:s-param-v
    urn:idmx:3.0.0:block:rangeProof
    urn:idmx:3.0.0:block:s-nEq
    urn:idmx:3.0.0:block:s-setmem:cg
    urn:idmx:3.0.0:block:s-abc4trust-m-c14n
    urn:idmx:3.0.0:block:s-abc4trust-credspec
    urn:idmx:3.0.0:block:s-abc4trust-ptd
     */
    result.put(URI.create("urn:idmx:3.0.0:block:ecrypt2011"), EcryptSystemParametersGenerator.class);
    result.put(URI.create("urn:idmx:3.0.0:block:spGen"), EcryptSystemParametersGenerator.class);
    result.put(URI.create("urn:idmx:3.0.0:block:sig"), ClSignatureBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:sig:cl"), ClSignatureBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:uprove"), BrandsSignatureBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:sig:uprove"), BrandsSignatureBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:sig:uprove:sig:uprove"), BrandsSignatureBuildingBlock.class);


    result.put(URI.create("urn:idmx:3.0.0:block:pseudonym:scopeExclusive"), ScopeExclusivePseudonymBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:pseudonym:standard"), StandardPseudonymBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:h-rep-pedersen"), PedersenRepresentationBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:h-rep-df"), DamgardFujisakiRepresentationBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:h-rep-paillier"), ModNSquareRepresentationBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:ins:cs"), CsInspectorBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-constant"), ConstantBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-source"), AttributeSourceBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-eq"), AttributeEqualityBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-param-ins"), InspectorPublicKeyBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-param-iss"), IssuerPublicKeyBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-param-ra"), RevocationAuthorityPublicKeyBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:revocation"), ClRevocationBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:revocation:cl"), ClRevocationBuildingBlock.class);

    result.put(URI.create("urn:idmx:3.0.0:block:s-linear"), LinearCombinationLightBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-linear-modq"), LinearCombinationModQBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-ms"), MechanismSpecificationBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-m"), MessageBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-reveal"), RevealAttributeBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-param-sys"), SystemParametersBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-param-v"), VerifierParametersBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:rangeProof"), FourSquaresRangeProofBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-nEq"), InverseAttributeNotEqualBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-setmem:cg"), CgAttributeSetMembershipBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-abc4trust-m-c14n"), Abc4TrustMessageBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-abc4trust-credspec"), CredentialSpecificationBuildingBlock.class);
    result.put(URI.create("urn:idmx:3.0.0:block:s-abc4trust-ptd"), PresentationTokenDescriptionBuildingBlock.class);
    return result;

  }

  private GeneralBuildingBlock getInstanceByClass(Class<?> clazz) {
    if (clazz == EcryptSystemParametersGenerator.class) {
      return manageEcryptSystemParametersGenerator();

    } else if (clazz == ClSignatureBuildingBlock.class){
      return manageClSignatureBuildingBlock();

    } else if (clazz == BrandsSignatureBuildingBlock.class){
      return manageBrandsSignatureBuildingBlock();

    } else if (clazz == ScopeExclusivePseudonymBuildingBlock.class){
      return manageScopeExclusivePseudonymBuildingBlock();

    } else if (clazz == StandardPseudonymBuildingBlock.class){
      return manageStandardPseudonymBuildingBlock();

    } else if (clazz == PedersenRepresentationBuildingBlock.class){
      return managePedersenRepresentationBuildingBlock();

    } else if (clazz == DamgardFujisakiRepresentationBuildingBlock.class){
      return manageDamgardFujisakiRepresentationBuildingBlock();

    } else if (clazz == ModNSquareRepresentationBuildingBlock.class){
      return manageModNSquareRepresentationBuildingBlock();

    } else if (clazz == ClRevocationBuildingBlock.class){
      return manageClRevocationBuildingBlock();

    } else if (clazz == CsInspectorBuildingBlock.class){
      return manageCsInspectorBuildingBlock();

    } else if (clazz == ConstantBuildingBlock.class){
      return manageConstantBuildingBlock();

    } else if (clazz == AttributeSourceBuildingBlock.class){
      return manageAttributeSourceBuildingBlock();

    } else if (clazz == AttributeEqualityBuildingBlock.class){
      return manageAttributeEqualityBuildingBlock();

    } else if (clazz == IssuerPublicKeyBuildingBlock.class){
      return manageIssuerPublicKeyBuildingBlock();

    } else if (clazz == InspectorPublicKeyBuildingBlock.class){
      return manageInspectorPublicKeyBuildingBlock();

    } else if (clazz == RevocationAuthorityPublicKeyBuildingBlock.class){
      return manageRevocationAuthorityPublicKeyBuildingBlock();

    } else if (clazz == LinearCombinationLightBuildingBlock.class){
      return manageLinearCombinationLightBuildingBlock();

    } else if (clazz == LinearCombinationModQBuildingBlock.class){
      return manageLinearCombinationModQBuildingBlock();

    } else if (clazz == MechanismSpecificationBuildingBlock.class){
      return manageMechanismSpecificationBuildingBlock();

    } else if (clazz == MessageBuildingBlock.class){
      return manageMessageBuildingBlock();

    } else if (clazz == RevealAttributeBuildingBlock.class){
      return manageRevealAttributeBuildingBlock();

    } else if (clazz == SystemParametersBuildingBlock.class){
      return manageSystemParametersBuildingBlock();


    } else if (clazz == VerifierParametersBuildingBlock.class){
      return manageVerifierParametersBuildingBlock();

    } else if (clazz == FourSquaresRangeProofBuildingBlock.class){
      return manageFourSquaresRangeProofBuildingBlock();

    } else if (clazz == InverseAttributeNotEqualBuildingBlock.class){
      return manageInverseAttributeNotEqualBuildingBlock();

    } else if (clazz == CgAttributeSetMembershipBuildingBlock.class){
      return manageCgAttributeSetMembershipBuildingBlock();

    } else if (clazz == Abc4TrustMessageBuildingBlock.class){
      return manageAbc4TrustMessageBuildingBlock();

    } else if (clazz == CredentialSpecificationBuildingBlock.class){
      return manageCredentialSpecificationBuildingBlock();

    } else if (clazz == PresentationTokenDescriptionBuildingBlock.class){
      return managePresentationTokenDescriptionBuildingBlock();

    }else {
      throw new RuntimeException("Unable to find BuildingBlock " + clazz);
    }
  }


  private ExternalSecretsManager manageExternalSecretsManager() {
    if (this.externalSecretsManager==null){
      this.externalSecretsManager =
              new ExternalSecretsManagerImpl(basis.provideBigIntFactory(), basis.provideRandomGeneration(),
                      keyManager, bbf, credentialManager);

    }
    return this.externalSecretsManager;
  }

  private ExternalSecretsHelper manageExternalSecretsHelper() {
    if (this.externalSecretsHelper==null){
      this.externalSecretsHelper = new ExternalSecretsHelperImpl(basis.provideBigIntFactory());

    }
    return this.externalSecretsHelper;

  }

  private GeneralBuildingBlock manageEcryptSystemParametersGenerator() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(EcryptSystemParametersGenerator.class);
    if (block == null){
      block = new EcryptSystemParametersGenerator();
      map(block);

    }
    return block;
  }

  private GeneralBuildingBlock manageClSignatureBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(ClSignatureBuildingBlock.class);
    if (block == null){
      block = new ClSignatureBuildingBlock(
              basis.provideRandomGeneration(),
              basis.provideBigIntFactory(),
              basis.provideGroupFactory(),
              this.bbf,
              basis.provideTiming(),
              manageExternalSecretsManager());
      map(block);

    }
    return block;

  }


  private GeneralBuildingBlock manageBrandsSignatureBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(BrandsSignatureBuildingBlock.class);
    if (block == null){
      block = new BrandsSignatureBuildingBlock(basis.provideRandomGeneration(),
              basis.provideGroupFactory(),
              basis.provideBigIntFactory(),
              manageExternalSecretsManager(),
              this.bbf,
              basis.provideLogger(),
              basis.providesTestVectorHelper());
      map(block);

    }
    return block;
  }



  private GeneralBuildingBlock manageScopeExclusivePseudonymBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(ScopeExclusivePseudonymBuildingBlock.class);
    if (block == null){
      block = new ScopeExclusivePseudonymBuildingBlock(
              basis.provideRandomGeneration(),
              bbf,
              manageExternalSecretsManager(),
              manageExternalSecretsHelper(),
              basis.provideLogger(),
              basis.provideBigIntFactory());
      map(block);

    }
    return block;
  }


  private GeneralBuildingBlock manageStandardPseudonymBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(StandardPseudonymBuildingBlock.class);
    if (block == null){
      block = new StandardPseudonymBuildingBlock(
              basis.provideRandomGeneration(),
              bbf,
              manageExternalSecretsManager(),
              manageExternalSecretsHelper(),
              basis.provideLogger(),
              basis.provideBigIntFactory());

      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock managePedersenRepresentationBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(PedersenRepresentationBuildingBlock.class);
    if (block == null){
      block = new PedersenRepresentationBuildingBlock(
              basis.provideRandomGeneration(),
              bbf,manageExternalSecretsManager(),
              basis.provideLogger(),
              basis.provideBigIntFactory());
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageDamgardFujisakiRepresentationBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(DamgardFujisakiRepresentationBuildingBlock.class);
    if (block == null){
      block = new DamgardFujisakiRepresentationBuildingBlock(basis.provideRandomGeneration(),
              bbf,manageExternalSecretsManager(),basis.provideLogger(),
              basis.provideBigIntFactory());
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageModNSquareRepresentationBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(ModNSquareRepresentationBuildingBlock.class);
    if (block == null){
      block = new ModNSquareRepresentationBuildingBlock(basis.provideRandomGeneration(),
              bbf,manageExternalSecretsManager(),
              basis.provideLogger(), basis.provideBigIntFactory());
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageClRevocationBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(ClRevocationBuildingBlock.class);
    if (block == null){
      block = new ClRevocationBuildingBlock(basis.provideRandomGeneration(),
              basis.provideBigIntFactory(), basis.provideGroupFactory(), basis.provideTiming());
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageCsInspectorBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(CsInspectorBuildingBlock.class);
    if (block == null){
      block = new CsInspectorBuildingBlock(basis.provideRandomGeneration(),
              bbf, basis.provideBigIntFactory(), basis.provideGroupFactory());
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageConstantBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(ConstantBuildingBlock.class);
    if (block == null){
      block = new ConstantBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageAttributeSourceBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(AttributeSourceBuildingBlock.class);
    if (block == null){
      block = new AttributeSourceBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageAttributeEqualityBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(AttributeEqualityBuildingBlock.class);
    if (block == null){
      block = new AttributeEqualityBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageInspectorPublicKeyBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(InspectorPublicKeyBuildingBlock.class);
    if (block == null){
      block = new InspectorPublicKeyBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageIssuerPublicKeyBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(IssuerPublicKeyBuildingBlock.class);
    if (block == null){
      block = new IssuerPublicKeyBuildingBlock();
      map(block);

    }
    return block;


  }



  private GeneralBuildingBlock manageRevocationAuthorityPublicKeyBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(RevocationAuthorityPublicKeyBuildingBlock.class);
    if (block == null){
      block = new RevocationAuthorityPublicKeyBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageLinearCombinationLightBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(LinearCombinationLightBuildingBlock.class);
    if (block == null){
      block = new LinearCombinationLightBuildingBlock();
      map(block);

    }
    return block;
  }

  private GeneralBuildingBlock manageLinearCombinationModQBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(LinearCombinationModQBuildingBlock.class);
    if (block == null){
      block = new LinearCombinationModQBuildingBlock(basis.provideBigIntFactory(),
              bbf, basis.provideGroupFactory());
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageMechanismSpecificationBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(MechanismSpecificationBuildingBlock.class);
    if (block == null){
      block = new MechanismSpecificationBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageMessageBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(MessageBuildingBlock.class);
    if (block == null){
      block = new MessageBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageRevealAttributeBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(RevealAttributeBuildingBlock.class);
    if (block == null){
      block = new RevealAttributeBuildingBlock();
      map(block);

    }
    return block;

  }


  private GeneralBuildingBlock manageSystemParametersBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(SystemParametersBuildingBlock.class);
    if (block == null){
      block = new SystemParametersBuildingBlock();
      map(block);

    }
    return block;

  }


  private GeneralBuildingBlock manageVerifierParametersBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(VerifierParametersBuildingBlock.class);
    if (block == null){
      block = new VerifierParametersBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageFourSquaresRangeProofBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(FourSquaresRangeProofBuildingBlock.class);
    if (block == null){
      block = new FourSquaresRangeProofBuildingBlock(
              manageSafeRSAGroupInVerifierParam(),
              basis.provideBigIntFactory(), bbf, basis.provideGroupFactory()
      );
      map(block);

    }
    return block;

  }

  private SafeRSAGroupInVerifierParameters manageSafeRSAGroupInVerifierParam() {
    if (this.safeRSAGroupInVerifierParameters==null){
      this.safeRSAGroupInVerifierParameters = new SafeRSAGroupInVerifierParameters(
              keyManager, bbf, basis.provideGroupFactory()
      );
    }
    return this.safeRSAGroupInVerifierParameters;
  }

  private GeneralBuildingBlock manageInverseAttributeNotEqualBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(InverseAttributeNotEqualBuildingBlock.class);
    if (block == null){
      block = new InverseAttributeNotEqualBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageCgAttributeSetMembershipBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(CgAttributeSetMembershipBuildingBlock.class);
    if (block == null){
      block = new CgAttributeSetMembershipBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageAbc4TrustMessageBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(Abc4TrustMessageBuildingBlock.class);
    if (block == null){
      block = new Abc4TrustMessageBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock manageCredentialSpecificationBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(CredentialSpecificationBuildingBlock.class);
    if (block == null){
      block = new CredentialSpecificationBuildingBlock();
      map(block);

    }
    return block;

  }

  private GeneralBuildingBlock managePresentationTokenDescriptionBuildingBlock() {
    GeneralBuildingBlock block = buildingBlocksByClass.get(PresentationTokenDescriptionBuildingBlock.class);
    if (block == null){
      block = new PresentationTokenDescriptionBuildingBlock();
      map(block);

    }
    return block;

  }

  private void map(GeneralBuildingBlock block) {
    buildingBlocksByClass.put(block.getClass(), block);
    buildingBlocks.put(block.getBuildingBlockId(), block);

  }

  protected ExternalSecretsHelper getExternalSecretsHelper() {
    if (externalSecretsHelper==null){
      return manageExternalSecretsHelper();
    }
    return externalSecretsHelper;
  }

  protected ExternalSecretsManager getExternalSecretsManager() {
    if (externalSecretsManager==null){
      return manageExternalSecretsManager();
    }
    return externalSecretsManager;
  }

  protected SafeRSAGroupInVerifierParameters getSafeRSAGroupInVerifierParameters() {
    if (safeRSAGroupInVerifierParameters==null){
      manageSafeRSAGroupInVerifierParam();

    }
    return safeRSAGroupInVerifierParameters;
  }

  RandomGeneration getRandomGeneration(){return basis.provideRandomGeneration();}
  BigIntFactory getBigIntFactory(){return basis.provideBigIntFactory();}
  Timing getTiming(){return basis.provideTiming();}
  GroupFactory getGroupFactory(){return basis.provideGroupFactory();}
  KeyManager getKeyManager(){return this.keyManager;}
  CredentialManager getCredentialManagerUser(){return this.credentialManager;}
  TestVectorHelper getTestVectorHelper(){return basis.providesTestVectorHelper();}
  HashComputationForChallenge getHashComputationForChallenge(){return basis.provideHashComputationForChallenge();}
  Logger getLogger(){return basis.provideLogger();}

  @SuppressWarnings("unchecked")
  public synchronized <T> T get(final Class<T> clazz) throws ConfigurationException {
      return (T) getInstanceByClass(clazz);

  }

  public synchronized GeneralBuildingBlock get(final URI type) throws ConfigurationException {
    System.out.println("Annoying URI Call" + type);
    return getInstanceByClass(blockURNToClassMap.get(type));
  }
  
  public List<Class<? extends GeneralBuildingBlock>> getList() {
    return Collections.unmodifiableList(listOfBuildingBlocks);
  }




}
