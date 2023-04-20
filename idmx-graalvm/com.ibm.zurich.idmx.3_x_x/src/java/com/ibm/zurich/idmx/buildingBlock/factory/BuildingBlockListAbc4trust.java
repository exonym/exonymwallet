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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.abc4TrustMessage.Abc4TrustMessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.credentialSpecification.CredentialSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.presentationTokenDescription.PresentationTokenDescriptionBuildingBlock;

/**
 * The list of Abc4trust specific building blocks
 */
public class BuildingBlockListAbc4trust implements BuildingBlockList {

  @SuppressWarnings("unchecked")
  private static final Class<? extends GeneralBuildingBlock>[] listOfBuildingBlocks = new Class[] {
      // Structural
      Abc4TrustMessageBuildingBlock.class,
      CredentialSpecificationBuildingBlock.class,
      PresentationTokenDescriptionBuildingBlock.class};

  @Override
  public List<Class<? extends GeneralBuildingBlock>> getListOfBuildingBlocks() {
    final List<Class<? extends GeneralBuildingBlock>> ret =
        new ArrayList<Class<? extends GeneralBuildingBlock>>();
    ret.addAll(new BuildingBlockListIdmx().getListOfBuildingBlocks());
    ret.addAll(Arrays.asList(listOfBuildingBlocks));
    return Collections.unmodifiableList(ret);
  }

}
