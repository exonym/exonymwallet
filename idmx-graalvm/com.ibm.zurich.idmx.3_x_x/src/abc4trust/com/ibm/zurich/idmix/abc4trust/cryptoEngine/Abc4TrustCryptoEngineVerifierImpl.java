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
package com.ibm.zurich.idmix.abc4trust.cryptoEngine;

import java.security.SecureRandom;

import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.xml.IdemixVerifierParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.VerifierParametersTemplate;

import javax.inject.Inject;

public class Abc4TrustCryptoEngineVerifierImpl implements eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier {

  public final CryptoEngineVerifier cev;
  
  @Inject
  public Abc4TrustCryptoEngineVerifierImpl(final CryptoEngineVerifier cev) {
    this.cev = cev;
  }

  @Override
  public boolean verifyToken(final PresentationToken presentationToken, final VerifierParameters vp) throws TokenVerificationException,
      CryptoEngineException {
    return cev.verifyToken(presentationToken, vp);
  }

  @Override
  public VerifierParameters createVerifierParameters(final SystemParameters sp) throws CryptoEngineException {
    final VerifierParametersTemplate vpt = cev.generateVerifierParameterConfigurationTemplate();
    final IdemixVerifierParameters ivp = cev.generateVerifierParameters(sp, vpt);
    
    final VerifierParameters vp = new ObjectFactory().createVerifierParameters();
    vp.setSystemParametersId(ivp.getSystemParametersId());
    vp.setVerifierParametersId(ivp.getVerifierParametersId());
    vp.setVersion(ivp.getVersion());
    vp.setCryptoParams(new ObjectFactory().createCryptoParams());
    vp.getCryptoParams().getContent().add(new ObjectFactory().createIdemixVerifierParameters(ivp));
    
    return vp;
  }
  
  //TODO(ksa) random constant? SHA-256?
  @Override
  public byte[] createNonce() {
    final int lengthInBits = 256;
    final byte[] ret = new byte[lengthInBits / 8];
    new SecureRandom().nextBytes(ret);
    return ret;
  }

}
