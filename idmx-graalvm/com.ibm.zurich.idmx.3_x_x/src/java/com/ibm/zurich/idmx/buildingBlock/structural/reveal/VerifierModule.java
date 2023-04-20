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

package com.ibm.zurich.idmx.buildingBlock.structural.reveal;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

public class VerifierModule extends ZkModuleImpl implements ZkModuleVerifier {

  private final String attributeId;
  @Nullable
  private final BigInt value;

  public VerifierModule(final RevealAttributeBuildingBlock parent, final String identifierOfModule,
                        final String attributeId, final @Nullable BigInt value) {

    super(parent, identifierOfModule);

    this.attributeId = attributeId;
    this.value = value;
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException {
    zkVerifier.registerAttribute(attributeId, false);
    zkVerifier.attributeIsRevealed(attributeId);
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    if (value != null) {
      final BigInt actualValue = zkVerifier.getValueOfRevealedAttribute(attributeId);
      if (!actualValue.equals(value)) {
        throw new ProofException("Revealed value does not match expected value. Expected: " + value
            + " ; Actual: " + actualValue);
      }
    }
    // Suppress the hash contribution we would get from revealing the attribute
    zkVerifier.checkHashContributionOfBuildingBlock(null);
    return true;
  }

}
