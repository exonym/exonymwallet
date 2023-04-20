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

package com.ibm.zurich.idmx.configuration;

import java.net.URI;

import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.util.UriUtils;

/**
 * 
 */
public class BuildingBlockBaseName implements Constants {

  /**
   * @return
   */
  public static URI getBuildingBlockBaseId() {
    return UriUtils.concat(BASE_URI, IMPLEMENTATION_VERSION, "block");
  }

}
