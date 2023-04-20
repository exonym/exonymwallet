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

package com.ibm.zurich.idmx.buildingBlock.systemParameters;

import static com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersTemplateWrapper.ATTRIBUTE_LENGTH_NAME;
import static com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersTemplateWrapper.SECURITY_LEVEL_NAME;
import static com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersTemplateWrapper.STATISTICAL_IND_NAME;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.xml.SystemParameters;

public class EcryptSystemParametersWrapper extends SystemParametersWrapper {

  public EcryptSystemParametersWrapper() {}

  public EcryptSystemParametersWrapper(final SystemParameters systemParameters) {
    super(systemParameters);
  }

  // System parameters element names
  public static final String DH_MODULUS_LENGTH_NAME = "dhModulusLength";

  public void setDHModulusLength(final int modulusLength) {
    setParameter(DH_MODULUS_LENGTH_NAME, modulusLength);
  }

  public int getDHModulusLength() throws ConfigurationException {
    return (Integer) getParameter(DH_MODULUS_LENGTH_NAME);
  }


  public static final String DH_SUBGROUP_LENGTH_NAME = "dhSubgroupLength";

  public void setDHSubgroupLength(final int subgroupLength) {
    setParameter(DH_SUBGROUP_LENGTH_NAME, subgroupLength);
  }

  public int getDHSubgroupLength() throws ConfigurationException {
    return (Integer) getParameter(DH_SUBGROUP_LENGTH_NAME);
  }


  public static final String PRIME_PROBABILITY_NAME = "primeProb";

  public void setPrimeProbability(final int prob) {
    setParameter(PRIME_PROBABILITY_NAME, prob);
  }

  public int getPrimeProbability() throws ConfigurationException {
    return (Integer) getParameter(PRIME_PROBABILITY_NAME);
  }


  public static final String DH_MODULUS_NAME = "dhModulus";

  public void setDHModulus(final BigInt modulus) {
    setParameter(DH_MODULUS_NAME, modulus);
  }

  public BigInt getDHModulus() throws ConfigurationException {
    return (BigInt) getParameter(DH_MODULUS_NAME);
  }


  public static final String DH_SUBGROUP_ORDER_NAME = "dhSubgroupOrder";

  public void setDHSubgroupOrder(final BigInt subgroupOrder) {
    setParameter(DH_SUBGROUP_ORDER_NAME, subgroupOrder);
  }

  public BigInt getDHSubgroupOrder() throws ConfigurationException {
    return (BigInt) getParameter(DH_SUBGROUP_ORDER_NAME);
  }


  public static final String DH_GENERATOR_1_NAME = "dhGen-1";

  public void setDHGenerator1(final BigInt g) {
    setParameter(DH_GENERATOR_1_NAME, g);
  }

  public BigInt getDHGenerator1() throws ConfigurationException {
    return (BigInt) getParameter(DH_GENERATOR_1_NAME);
  }


  public static final String DH_GENERATOR_2_NAME = "dhGen-2";

  public void setDHGenerator2(final BigInt g) {
    setParameter(DH_GENERATOR_2_NAME, g);
  }

  public BigInt getDHGenerator2() throws ConfigurationException {
    return (BigInt) getParameter(DH_GENERATOR_2_NAME);
  }


  public void setStatisticalZeroKnowledge(final int statZk) {
    setParameter(STATISTICAL_IND_NAME, statZk);
  }

  public int getStatisticalInd() throws ConfigurationException {
    return (Integer) getParameter(STATISTICAL_IND_NAME);
  }
  
  /**
   * Returns the size of RValues in bits corresponding to attributes of the given size (in bits).
   * @param attributeSizeBits The size of the attribute in bits. If you pass a value smaller than
   * the size of the subgroup order, the size of the subgroup order will be taken to do the
   * computation.
   * @return
   * @throws ConfigurationException
   */
  public int getSizeOfRValue(final int attributeSizeBits) throws ConfigurationException {
    final int localAttributeSizeBits;
    if(attributeSizeBits < getDHSubgroupLength()) {
      localAttributeSizeBits = getDHSubgroupLength();
    } else {
      localAttributeSizeBits = attributeSizeBits;
    }
    // All values are rounded up so that they are multiples of 8
    final int attributeSizeBytes = (localAttributeSizeBits + 7) / 8;
    final int statisticalSizeBytes = (getStatisticalInd() + 7) / 8;
    final int hashSizeBytes = (getHashLength() + 7) / 8;
    
    final int rSizeBytes = attributeSizeBytes + statisticalSizeBytes + hashSizeBytes;
    return rSizeBytes * 8;
  }
  
  /**
   * Returns the maximum attribute value assuming that the attribute has the given number
   * of bits. The minimum attribute value is always 0.
   * @param attributeSizeBits
   * @param bigIntFactory The size of the attribute in bits. If you pass a value smaller than
   * the size of the subgroup order, this function will return the subgroup order minus 1.
   * @return
   * @throws ConfigurationException
   */
  public BigInt getMaximumAttributeValue(final int attributeSizeBits, final BigIntFactory bigIntFactory)
      throws ConfigurationException {
    final BigInt maxValue;
    if (attributeSizeBits < getDHSubgroupLength()) {
      maxValue = getDHSubgroupOrder();
    } else {
      maxValue = bigIntFactory.two().pow(attributeSizeBits);
    }
    return maxValue.subtract(bigIntFactory.one());
  }


  public void setAttributeLength(final int length) {
    setParameter(ATTRIBUTE_LENGTH_NAME, length);
  }

  public int getAttributeLength() throws ConfigurationException {
    return (Integer) getParameter(ATTRIBUTE_LENGTH_NAME);
  }


  public void setSecurityLevel(final int level) {
    setParameter(SECURITY_LEVEL_NAME, level);
  }

  public int getSecurityLevel() throws ConfigurationException {
    return (Integer) getParameter(SECURITY_LEVEL_NAME);
  }

   /**
   * Returns the hash length in bits.
   * @return
   * @throws ConfigurationException
   */
  public int getHashLength() throws ConfigurationException {
    final MessageDigest md;
    try {
      md = MessageDigest.getInstance(getHashFunction());
      return 8 * md.getDigestLength();
    } catch (final NoSuchAlgorithmException e) {
      throw new ConfigurationException(e);
    }
  }

  /**
   * Returns one bit length relevant to the choice of the prime number <tt>e</tt> of a CL signature.
   * 
   * @throws ConfigurationException
   */
  public int getL_e() throws ConfigurationException {
    return getAttributeLength() + getHashLength() + getStatisticalInd() + 5;
  }

  /**
   * Returns one bit length relevant to the choice of the prime number <tt>e</tt> of a CL signature.
   * 
   * @throws ConfigurationException
   */
  public int getLPrime_e() throws ConfigurationException {
    return getAttributeLength() + 1;
  }

  /**
   * Returns the bit length of the randomisation exponent of a CL signature.
   * 
   * @throws ConfigurationException
   */
  public int getL_v() throws ConfigurationException {
    return getDHModulusLength() + 2 * getStatisticalInd() + getHashLength() + 3;
  }

}
