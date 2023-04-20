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
package com.ibm.zurich.idmx.buildingBlock.helper.representation.modNSquare;

import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.VerifierModulePresentation;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
//import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
//import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
//import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupMultOpSequence;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;

import eu.abc4trust.xml.SystemParameters;

public class VerifierModule extends VerifierModulePresentation<PaillierGroup, PaillierGroupElement, PaillierGroupMultOpSequence> {

  private final PaillierGroup group;

  public VerifierModule(final ModNSquareRepresentationBuildingBlock parent, final String identifierOfModule,
                        final SystemParameters systemParameters, final List<BaseForRepresentation> bases, final PaillierGroup group,
      final @Nullable PaillierGroupElement commitment, final @Nullable String commitmentAsDValue,
      final BuildingBlockFactory bbFactory) {
    super(parent, identifierOfModule, systemParameters, bases, group, commitment,
        commitmentAsDValue, bbFactory);
    this.group = group;
  }

  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {

    for (int i = 0; i < bases.size(); i++) {
      if (bases.get(i).chooseExponentRandomly == false) {
        zkVerifier.registerAttribute(identifierOfAttribute(i), false);
      } else {
        zkVerifier
            .registerAttribute(identifierOfAttribute(i), false, group
                .getRandomIterationcounterLengthForSubgroupOfOrderPhiN(spWrapper.getStatisticalInd()));
      }
    }
  }
}
