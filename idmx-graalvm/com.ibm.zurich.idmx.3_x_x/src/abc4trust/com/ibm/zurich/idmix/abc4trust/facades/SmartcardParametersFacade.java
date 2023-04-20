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
package com.ibm.zurich.idmix.abc4trust.facades;

import java.math.BigInteger;

import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.smartcard.SmartcardParameters;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SystemParameters;

public class SmartcardParametersFacade {
  private final IssuerParametersFacade ipw;
  private final EcryptSystemParametersWrapper spw;

  public SmartcardParametersFacade(final SystemParameters sp, final IssuerParameters ip) {
    this.ipw = new IssuerParametersFacade(ip);
    this.spw = new EcryptSystemParametersWrapper(sp);
  }

  public SmartcardParameters getSmartcardParameters() throws CryptoEngineException {
    try {
      String technology = ipw.getBuildingBlockId().toString();
      if (technology.endsWith("cl")) {
        final ClPublicKeyWrapper pkw = new ClPublicKeyWrapper(ipw.getPublicKey());
        final BigInteger R0 = pkw.getRd().getValue();
        final BigInteger S = pkw.getS().getValue();
        final BigInteger n = pkw.getModulus().getValue();
        return SmartcardParameters.forTwoBaseCl(n, R0, S);
      } else if (technology.endsWith("uprove")) {
        final BrandsPublicKeyWrapper pkw = new BrandsPublicKeyWrapper(ipw.getPublicKey());
        final BigInteger gD = pkw.getGD().getValue();
        final BigInteger p = spw.getDHModulus().getValue();
        final BigInteger q = spw.getDHSubgroupOrder().getValue();
        return SmartcardParameters.forOneBaseUProve(p, q, gD);
      } else {
        throw new CryptoEngineException("Unknown technology: " + technology);
      }
    } catch (final ConfigurationException ce) {
      throw new CryptoEngineException(ce);
    }
  }
}
