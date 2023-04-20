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

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.SystemParametersTemplate;

/**
 * 
 */
public abstract class SystemParameterGeneratorBuildingBlock extends GeneralBuildingBlock {

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "spGen";
  }

  @Override
  public final boolean contributesToSystemParameters() {
    return true;
  }

  /**
   * Adds local configuration parameters to the system parameters template.
   * 
   * @param template
   */
  public abstract void addBuildingBlockSystemParametersTemplate(final SystemParametersTemplate template);

  /**
   * Generates the system parameters relevant for a building block based on the configuration values
   * given in the template. Only signature building blocks implement this method - other building
   * blocks throw a ConfigurationException().
   * 
   * @param template
   * @param systemParameters
   * @throws ConfigurationException
   */
  public abstract void generateBuildingBlockSystemParameters(final SystemParametersTemplate template,
      SystemParameters systemParameters) throws ConfigurationException;

}
