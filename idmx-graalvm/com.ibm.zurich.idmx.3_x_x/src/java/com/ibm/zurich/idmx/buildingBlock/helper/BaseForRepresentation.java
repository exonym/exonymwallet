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

package com.ibm.zurich.idmx.buildingBlock.helper;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;

public class BaseForRepresentation {
  final @Nullable
  public GroupElement<?, ?, ?> base;
  public final @Nullable String identifierOfBaseAsDValue;
  public final boolean chooseExponentRandomly;
  public final boolean hasExternalSecret;
  public final boolean hasCredentialSecretKey;
  public final @Nullable BigInt externalRandomizerOffset;
  public final int maxOffsetLengthBits;

  private BaseForRepresentation(final @Nullable GroupElement<?, ?, ?> base, final @Nullable String identifierOfDValue,
                                final boolean chooseExponentRandomly, final boolean hasExternalSecret, final boolean isCredentialSecretKey,
      final @Nullable BigInt externalRandomizerOffset) {
    this.base = base;
    this.identifierOfBaseAsDValue = identifierOfDValue;
    this.chooseExponentRandomly = chooseExponentRandomly;
    this.hasExternalSecret = hasExternalSecret;
    this.hasCredentialSecretKey = isCredentialSecretKey;
    this.externalRandomizerOffset = externalRandomizerOffset;
    this.maxOffsetLengthBits = 0;//maxOffsetLengthBits;
  }

  public static BaseForRepresentation managedAttribute(final GroupElement<?, ?, ?> base) {
    return new BaseForRepresentation(base, null, false, false, false, null);
  }

  public static BaseForRepresentation randomAttribute(final GroupElement<?, ?, ?> base) {
    return new BaseForRepresentation(base, null, true, false, false,  null);
  }

  public static BaseForRepresentation deviceSecret(final GroupElement<?, ?, ?> base) {
    return new BaseForRepresentation(base, null, false, true,  false, null);
  }

  public static BaseForRepresentation deviceRandomizer(final GroupElement<?, ?, ?> base, final @Nullable BigInt offset) {
    return new BaseForRepresentation(base, null, false, false, true, offset);
  }

  public static BaseForRepresentation managedAttribute(String baseAsDValue) {
    return new BaseForRepresentation(null, baseAsDValue, false, false, false, null);
  }

  public static BaseForRepresentation randomAttribute(String baseAsDValue) {
    return new BaseForRepresentation(null, baseAsDValue, true, false, false, null);
  }

  public static BaseForRepresentation deviceSecret(String baseAsDValue) {
    return new BaseForRepresentation(null, baseAsDValue, false, true, false, null);
  }

  public static BaseForRepresentation deviceRandomizer(final String baseAsDValue, final @Nullable BigInt offset) {
    return new BaseForRepresentation(null, baseAsDValue, false, false, true, offset);
  }
  
  public GroupElement<?, ?, ?> getBase(final ZkProofStateFirstRound zkp, final Group<?, ?, ?> g)
      throws ProofException {
    if(base == null) {
      return zkp.getDValueAsGroupElement(identifierOfBaseAsDValue);
    } else {
      return base;
    }
  }

  public GroupElement<?, ?, ?> getBase(final ZkVerifierStateVerify zkv, final Group<?,?,?> g) throws ProofException {
    if(base == null) {
      return  zkv.getDValueAsGroupElement(identifierOfBaseAsDValue, g);
    } else {
      return base;
    }
  }


}
