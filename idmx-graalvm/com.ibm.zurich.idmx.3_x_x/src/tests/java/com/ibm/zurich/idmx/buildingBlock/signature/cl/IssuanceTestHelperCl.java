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
package com.ibm.zurich.idmx.buildingBlock.signature.cl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.signature.IssuanceTestHelper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;

import eu.abc4trust.xml.KeyPair;

class IssuanceTestHelperCl extends IssuanceTestHelper {
  
  private static final String USERNAME = "user";

  public IssuanceTestHelperCl()
      throws SerializationException {
    super(ClSignatureBuildingBlock.class, "../sp_default.xml", "keyPair_cl.xml");
  }

  @Override
  protected Pair<CarryOverStateIssuer, CarryOverStateRecipient> constructCarryOverStateManually(List<Boolean> carryOver, boolean device, List<BigInt> attributes,
      EcryptSystemParametersWrapper sp, KeyPair keyPair) throws ConfigurationException {
        ClPublicKeyWrapper pk = new ClPublicKeyWrapper(keyPair.getPublicKey());
        List<BigInt> coAttributes = new ArrayList<BigInt>();
        HiddenOrderGroup group =
            groupFactory.createSignedQuadraticResiduesGroup(pk.getModulus());
        HiddenOrderGroupElement S = group.valueOfNoCheck(pk.getS());
        BigInt randomizer =
            group.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
        HiddenOrderGroupElement commitment = S.multOp(randomizer);
        for (int i = 0; i < carryOver.size(); ++i) {
          if (carryOver.get(i)) {
            BigInt value = attributes.get(i);
            attributes.set(i, null);
            coAttributes.add(value);
            HiddenOrderGroupElement base = group.valueOfNoCheck(pk.getBase(i));
            commitment = commitment.opMultOp(base, value);
          } else {
            coAttributes.add(null);
          }
        }
        if (device) {
          BigInteger credPk_bigInt = esManager.getCredentialPublicKey(USERNAME, DEVICE_URI, CRED_URI_ON_DEVICE);
          HiddenOrderGroupElement credPk = group.valueOfNoCheck(bigIntFactory.valueOf(credPk_bigInt));
          commitment = commitment.op(credPk);
        }
        CarryOverStateIssuer coiss1 = new CarryOverStateIssuer(commitment, carryOver);
        CarryOverStateRecipient corec1 =
            new CarryOverStateRecipient(commitment, randomizer, coAttributes);
        Pair<CarryOverStateIssuer, CarryOverStateRecipient> coState =
            new Pair<CarryOverStateIssuer, CarryOverStateRecipient>(coiss1, corec1);
        return coState;
      }
  
}
