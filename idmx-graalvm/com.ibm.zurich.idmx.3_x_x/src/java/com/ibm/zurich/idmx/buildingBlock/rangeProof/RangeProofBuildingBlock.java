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

package com.ibm.zurich.idmx.buildingBlock.rangeProof;

import java.net.URI;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.util.UriUtils;

import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.VerifierParametersTemplate;

public abstract class RangeProofBuildingBlock extends GeneralBuildingBlock {


  public static final String ATT_NAME_ZERO = "rangeProof:zero";
  public static final String ATT_NAME_MAX = "rangeProof:max";

  private final SafeRSAGroupInVerifierParameters rsaVp;

  public RangeProofBuildingBlock(final SafeRSAGroupInVerifierParameters rsaVp) {
    this.rsaVp = rsaVp;
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "rangeProof";
  }

  protected String getModuleIdentifier(final String lhsAttribute, final String rhsAttribute, final int seq) {
    return UriUtils.concat(UriUtils.concat(URI.create(getBuildingBlockIdSuffix()), URI.create(""+seq)),
        UriUtils.concat(lhsAttribute, rhsAttribute)).toString();
  }

  /**
   * @param strict Set to true for less-than, set to false for less-equal
   * @throws ConfigurationException 
   */
  public abstract ZkModuleProver getZkModuleProver(final SystemParameters systemParameters,
                                                   final VerifierParameters verifierParameters, final String lhsAttribute, final String rhsAttribute,
                                                   final boolean strict, final int predicateNumber) throws ConfigurationException;

  /**
   * @param strict Set to true for less-than, set to false for less-equal
   * @throws ConfigurationException 
   */
  public abstract ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                                       final VerifierParameters verifierParameters, final String lhsAttribute, final String rhsAttribute,
                                                       final boolean strict, final int predicateNumber) throws ConfigurationException;

  /**
   * Perform a range check for the given attribute (i.e., check that the attribute is between
   * 0 and 2^{maxAttribute}-1).
   * @throws ConfigurationException 
   */
  public abstract ZkModuleProver getZkModuleProverRangeCheck(final SystemParameters systemParameters,
                                                             final VerifierParameters verifierParameters, final String attributeName) throws ConfigurationException;

  /**
   * Verify a range check for the given attribute (i.e., check that the attribute is between
   * 0 and 2^{maxAttribute}-1).
   * @throws ConfigurationException 
   */
  public abstract ZkModuleVerifier getZkModuleVerifierRangeCheck(final SystemParameters systemParameters,
                                                                 final VerifierParameters verifierParameters, final String attributeName) throws ConfigurationException;
  
  public boolean contributesToVerifierParameterTemplate() {
    return true;
  }

  /**
   * Adds implementation-specific parameters to the verifier parameter template.
   * 
   * @param spWrapper
   * @param parameter
   */
  @Override
public void populateVerifierParameterTemplate(final SystemParametersWrapper spWrapper,
                                              final List<Parameter> parameter) {
    rsaVp.populateVerifierParameterTemplate(spWrapper, parameter);
  }

  /**
   * Adds implementation-specific parameters to the verifier parameters given the template.
   * 
   * @param spWrapper
   * @param verifierParametersTemplate
   * @param parameter
   */
  @Override
public void populateVerifierParameters(final SystemParametersWrapper spWrapper,
                                       final VerifierParametersTemplate verifierParametersTemplate, final List<Parameter> parameter) {
    rsaVp.populateVerifierParameters(spWrapper, verifierParametersTemplate, parameter);
  }

}
