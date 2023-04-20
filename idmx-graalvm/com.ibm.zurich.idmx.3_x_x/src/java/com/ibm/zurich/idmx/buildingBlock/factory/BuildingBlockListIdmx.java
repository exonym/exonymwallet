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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.modNSquare.ModNSquareRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.CsInspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.notEqual.inv.InverseAttributeNotEqualBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq.FourSquaresRangeProofBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.setMembership.cg.CgAttributeSetMembershipBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.attributeSource.AttributeSourceBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.constant.ConstantBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.inspectorKey.InspectorPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombination.LinearCombinationLightBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombinationModQ.LinearCombinationModQBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.mechanismSpecification.MechanismSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.message.MessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;

/**
 * The list of Crypto-Engine-specific building blocks.
 */
public class BuildingBlockListIdmx implements BuildingBlockList {

  @SuppressWarnings("unchecked")
  private static final Class<? extends GeneralBuildingBlock>[] listOfBuildingBlocks = new Class[] {
      // System parameter generator
      EcryptSystemParametersGenerator.class, //

      // Signature
      ClSignatureBuildingBlock.class, //
      BrandsSignatureBuildingBlock.class, //

      // Pseudonyms
      ScopeExclusivePseudonymBuildingBlock.class, //
      StandardPseudonymBuildingBlock.class, //
      
      // Helper
      PedersenRepresentationBuildingBlock.class, //
      DamgardFujisakiRepresentationBuildingBlock.class, //
      ModNSquareRepresentationBuildingBlock.class, //

      // Revocation
      ClRevocationBuildingBlock.class, //

      // Verifiable Encryption
      CsInspectorBuildingBlock.class, //

      // Structural
      ConstantBuildingBlock.class, //
      AttributeSourceBuildingBlock.class, //
      AttributeEqualityBuildingBlock.class, //
      IssuerPublicKeyBuildingBlock.class, //
      InspectorPublicKeyBuildingBlock.class, //
      RevocationAuthorityPublicKeyBuildingBlock.class, //
      LinearCombinationLightBuildingBlock.class, //
      LinearCombinationModQBuildingBlock.class, //
      MechanismSpecificationBuildingBlock.class, //
      MessageBuildingBlock.class, //
      RevealAttributeBuildingBlock.class, //
      SystemParametersBuildingBlock.class, //
      VerifierParametersBuildingBlock.class, //

      // Predicate
      FourSquaresRangeProofBuildingBlock.class, //
      InverseAttributeNotEqualBuildingBlock.class, //
      CgAttributeSetMembershipBuildingBlock.class, //

  };

  @Override
  public List<Class<? extends GeneralBuildingBlock>> getListOfBuildingBlocks() {
    return Collections.unmodifiableList(Arrays.asList(listOfBuildingBlocks));
  }

}
