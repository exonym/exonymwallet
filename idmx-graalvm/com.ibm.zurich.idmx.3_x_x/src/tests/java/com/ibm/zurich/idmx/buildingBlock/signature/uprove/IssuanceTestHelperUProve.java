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
package com.ibm.zurich.idmx.buildingBlock.signature.uprove;

import com.ibm.zurich.idmx.buildingBlock.signature.IssuanceTestHelper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import eu.abc4trust.xml.KeyPair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

class IssuanceTestHelperUProve extends IssuanceTestHelper {
  
  private static final String USERNAME = "user";

  public IssuanceTestHelperUProve() throws SerializationException {
    super(BrandsSignatureBuildingBlock.class, "../sp_default.xml", "keyPair_brands.xml");
  }

  @Override
  protected Pair<CarryOverStateIssuer, CarryOverStateRecipient> constructCarryOverStateManually(
      List<Boolean> carryOver, boolean device, List<BigInt> attributes,
      EcryptSystemParametersWrapper sp, KeyPair keyPair) throws ConfigurationException {
    BrandsPublicKeyWrapper pk = new BrandsKeyPairWrapper(keyPair).getUProvePublicKeyWrapper();
    List<BigInt> coAttributes = new ArrayList<BigInt>();
    KnownOrderGroup group =
        groupFactory.createPrimeOrderGroup(sp.getDHModulus(), sp.getDHSubgroupOrder());
    KnownOrderGroupElement g = group.valueOfNoCheck(sp.getDHGenerator1());
    BigInt randomizer =
        group.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
    KnownOrderGroupElement commitment = g.multOp(randomizer);
    for (int i = 0; i < carryOver.size(); ++i) {
      if (carryOver.get(i)) {
        BigInt value = attributes.get(i);
        attributes.set(i, null);
        coAttributes.add(value);
        KnownOrderGroupElement base = group.valueOfNoCheck(pk.getGI(i + 1));
        commitment = commitment.opMultOp(base, value);
      } else {
        coAttributes.add(null);
      }
    }
    if (device) {
      BigInteger credPk_bigInt = esManager.getCredentialPublicKey(USERNAME, DEVICE_URI, CRED_URI_ON_DEVICE);
      KnownOrderGroupElement credPk = group.valueOfNoCheck(bigIntFactory.valueOf(credPk_bigInt));
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
