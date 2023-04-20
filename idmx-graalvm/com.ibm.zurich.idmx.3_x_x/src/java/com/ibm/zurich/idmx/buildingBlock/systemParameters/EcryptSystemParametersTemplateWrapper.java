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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.parameters.system.SystemParametersTemplateWrapper;

import eu.abc4trust.xml.SystemParametersTemplate;

public class EcryptSystemParametersTemplateWrapper extends SystemParametersTemplateWrapper {

  public EcryptSystemParametersTemplateWrapper() {}

  public EcryptSystemParametersTemplateWrapper(SystemParametersTemplate systemParametersTemplate) {
    super(systemParametersTemplate);
  }

  // TODO(enr): This doesn't belong to the template
  public static final String STATISTICAL_IND_NAME = "statisticalZk";

  public void setStatisticalInd(final int statZk) {
    setParameter(STATISTICAL_IND_NAME, statZk);
  }

  public int getStatisticalZeroKnowledge() throws ConfigurationException {
    return (Integer) getParameter(STATISTICAL_IND_NAME);
  }

  public static final String ATTRIBUTE_LENGTH_NAME = "attributeLength";

  public void setAttributeLength(final int length) {
    setParameter(ATTRIBUTE_LENGTH_NAME, length);
  }

  public int getAttributeLength() throws ConfigurationException {
    return (Integer) getParameter(ATTRIBUTE_LENGTH_NAME);
  }

  public static final String SECURITY_LEVEL_NAME = "securityLevel";

  public void setSecurityLevel(final int level) {
    setParameter(SECURITY_LEVEL_NAME, level);
  }

  public int getSecurityLevel() throws ConfigurationException {
    return (Integer) getParameter(SECURITY_LEVEL_NAME);
  }

}
