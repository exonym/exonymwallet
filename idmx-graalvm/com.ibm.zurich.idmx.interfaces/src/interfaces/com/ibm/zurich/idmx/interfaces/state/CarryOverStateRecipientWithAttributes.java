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

package com.ibm.zurich.idmx.interfaces.state;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import eu.abc4trust.xml.Attribute;

public class CarryOverStateRecipientWithAttributes implements Serializable {

  private static final long serialVersionUID = -6221696755807728011L;

  public CarryOverStateRecipient coState;
  public URI deviceUid;
  public URI credentialUid;
  public List<Attribute> attributes;

}
