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

package com.ibm.zurich.idmx.buildingBlock;

import java.net.URI;
import java.util.List;

import com.ibm.zurich.idmx.configuration.BuildingBlockBaseName;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.util.UriUtils;

import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.VerifierParametersTemplate;

public abstract class GeneralBuildingBlock {

  /**
   * Name of the current type of the building block. This name should start with
   * urn:idmix:3.j:block:.
   */
  public final URI getBuildingBlockId() {
    return UriUtils.concat(BuildingBlockBaseName.getBuildingBlockBaseId(),
        getBuildingBlockIdSuffix());
  }

  /**
   * Returns the suffix of the building block, which is used to build the building block id. For
   * example, the signature building block returns 'sig', which results in the building block id
   * 'urn:idmx:3.0.0:block:sig'.
   */
  protected abstract String getBuildingBlockIdSuffix();

  /**
   * Name of the specific implementation of the building block. This name should start with the
   * building block id and have a specific suffix for each implementation (e.g.,
   * urn:idmix:3.0.0:block:sig:cl for the CL implementation of the signature building block).
   * 
   * @throws ConfigurationException
   */
  public final URI getImplementationId() throws ConfigurationException {
    return UriUtils.concat(getBuildingBlockId(), getImplementationIdSuffix());
  }

  protected abstract String getImplementationIdSuffix() throws ConfigurationException;

  /**
   * Returns true if the building block contributes to the system parameters (and thereby also to
   * the system parameters template).
   */
  public boolean contributesToSystemParameters() {
    return false;
  }

  /**
   * Returns true if the building block contributes to the issuer key template.
   */
  public boolean contributesToIssuerKeyTemplate() {
    return false;
  }

  /**
   * Returns true if the building block contributes to the revocation public key template.
   */
  public boolean contributesToRevocationPublicKeyTemplate() {
    return false;
  }

  /**
   * Returns true if the building block contributes to the revocation public key template.
   */
  public boolean contributesToInspectorPublicKeyTemplate() {
    return false;
  }
  
  /**
   * Returns true if the building block contributes to the verifier parameter template.
   * If set to true, you must override populateVerifierParameterTemplate()
   * and populateVerifierParamaters()
   */
  public boolean contributesToVerifierParameterTemplate() {
    return false;
  }

  /**
   * Adds implementation-specific parameters to the verifier parameter template.
   * @param spWrapper
   * @param parameter
   */
  public void populateVerifierParameterTemplate(final SystemParametersWrapper spWrapper,
                                                final List<Parameter> parameter) {
  }

  /**
   * Adds implementation-specific parameters to the verifier parameters given the template.
   * @param spWrapper
   * @param verifierParametersTemplate
   * @param parameter
   */
  public void populateVerifierParameters(final SystemParametersWrapper spWrapper,
                                         final VerifierParametersTemplate verifierParametersTemplate, final List<Parameter> parameter) {
  }
}
