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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.keypair.issuer.IssuerPublicKeyWrapper;

import eu.abc4trust.xml.PublicKey;

public class BrandsPublicKeyWrapper extends IssuerPublicKeyWrapper {

  public BrandsPublicKeyWrapper(final PublicKey publicKey) {
    super(publicKey);
  }


  private static final String UPROVE_G0 = "uprove:g:0";

  /**
   * Getter for public generator g0.
   */
  public BigInt getG0() throws ConfigurationException {
    return (BigInt) getParameter(UPROVE_G0);
  }

  /**
   * Setter for public generator g0.
   */
  public void setG0(final BigInt g0) throws ConfigurationException {
    setParameter(UPROVE_G0, g0);
  }


  private static final String UPROVE_GI_PREFIX = "uprove:g:";

  /**
   * Getter for public generator for i-th attribute, where i is between 1 and the maximal number of
   * attributes.
   */
  public BigInt getGI(final int i) throws ConfigurationException {
    if (i < 1) {
      throw new RuntimeException("i must be larger than 0 in uprove public key.");
    }
    String label = UPROVE_GI_PREFIX + Integer.valueOf(i).toString();
    return (BigInt) getParameter(label);
  }

  /**
   * Setter for public generator for ith attribute, where i is between 1 and the maximal number of
   * attributes.
   */
  public void setGI(final BigInt gi, final int i) throws ConfigurationException {
    if (i < 1) {
      throw new RuntimeException("i must be larger than 0 in uprove public key.");
    }
    String label = UPROVE_GI_PREFIX + Integer.valueOf(i).toString();
    setParameter(label, gi);
  }


  private static final String UPROVE_GD = "uprove:g:d";

  /**
   * Getter for public generator for external attribute (e.g., on smartcard). This is an optional
   * field.
   */
  public BigInt getGD() throws ConfigurationException {
    return (BigInt) getParameter(UPROVE_GD);
  }

  /**
   * Setter for public generator for external attribute (e.g., on smartcard). This is an optional
   * field.
   */
  public void setGExt(final BigInt gExt) throws ConfigurationException {
    setParameter(UPROVE_GD, gExt);
  }


  private static final String UPROVE_GT = "uprove:g:t";

  /**
   * Getter for public generator gt, the base used for the credential specification ID.
   */
  public BigInt getGT() throws ConfigurationException {
    return (BigInt) getParameter(UPROVE_GT);
  }

  /**
   * Setter for public generator gt, the base used for the credential specification ID.
   */
  public void setGT(final BigInt gt) throws ConfigurationException {
    setParameter(UPROVE_GT, gt);
  }

  private static final String UPROVE_TOKENS = "uprove:tokens";

  /**
   * Getter for the number of tokens to generate during issuance.
   */
  public int getNumberOfTokens() throws ConfigurationException {
    return (Integer) getParameter(UPROVE_TOKENS);
  }

  /**
   * Setter for the number of tokens to generate during issuance.
   */
  public void setNumberOfUProveTokens(final int tokens) throws ConfigurationException {
    setParameter(UPROVE_TOKENS, tokens);
  }

  private static final String UPROVE_UIDP = "uprove:UIDp";

  /**
   * Getter for the U-Prove field "UIDp" (unique identifier of issuer parameters). This is an
   * optional field. If not set, a hash of the issuer parameters will be used instead.
   */
  public BigInt getUidP() throws ConfigurationException {
    return (BigInt) getParameter(UPROVE_UIDP);
  }

  /**
   * Is the optional U-Prove field "UIDp" (unique identifier of issuer parameters) present in the
   * public key?
   */
  public boolean hasUidP() throws ConfigurationException {
    return hasParameter(UPROVE_UIDP);
  }

  /**
   * Setter for the U-Prove field "UIDp" (unique identifier of issuer parameters). This is an
   * optional field.
   */
  public void setUidP(final BigInt uidp) throws ConfigurationException {
    setParameter(UPROVE_UIDP, uidp);
  }

  private static final String UPROVE_S = "uprove:S";

  /**
   * Getter for the U-Prove field "S" (application specific specification for issuer parameters and
   * U-Prove tokens issued among them). This is an optional field. If not set, a hash of the system
   * parameters will be used instead.
   */
  public BigInt getS() throws ConfigurationException {
    return (BigInt) getParameter(UPROVE_S);
  }

  /**
   * Is the optional U-Prove field "S" (application specific specification for issuer parameters and
   * U-Prove tokens issued among them) present in the public key?
   */
  public boolean hasS() throws ConfigurationException {
    return hasParameter(UPROVE_S);
  }

  /**
   * Setter for the U-Prove field "UIDp" (application specific specification for issuer parameters
   * and U-Prove tokens issued among them). This is an optional field.
   */
  public void setS(final BigInt s) throws ConfigurationException {
    setParameter(UPROVE_S, s);
  }
  
  private static final String UPROVE_EI_PREFIX = "uprove:e:";

  /**
   * Getter for the "hashed" flag e_i for the i-th attribute, where i is between 1 and the maximal
   * number of attributes. This is an optional parameter. If it is not set in the public key, the
   * value 0 is returned by this function.
   */
  public int getEI(final int i) throws ConfigurationException {
    if (i < 1) {
      throw new RuntimeException("i must be larger than 0 in uprove public key.");
    }
    final String label = UPROVE_EI_PREFIX + Integer.valueOf(i).toString();
    if(hasParameter(label)) {
      return (Integer) getParameter(label);
    } else {
      return 0;
    }
  }

  /**
   * Setter for public generator for ith attribute, where i is between 1 and the maximal number of
   * attributes. This is an optional parameter. If it is not set in the public key, the value 0
   * is used by default.
   */
  public void setEI(final int ei, final int i) throws ConfigurationException {
    if (i < 1) {
      throw new RuntimeException("i must be larger than 0 in uprove public key.");
    }
    final String label = UPROVE_EI_PREFIX + Integer.valueOf(i).toString();
    setParameter(label, ei);
  }

}
