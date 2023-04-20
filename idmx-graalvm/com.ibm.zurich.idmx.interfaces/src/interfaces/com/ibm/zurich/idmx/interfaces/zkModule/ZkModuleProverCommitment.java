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

package com.ibm.zurich.idmx.interfaces.zkModule;

import java.util.List;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;

public interface ZkModuleProverCommitment<GE extends GroupElement<?,GE,?>> extends ZkModuleProver {
  /**
   * Recovers value of the commitment C. This method may be called only after this sub-module has
   * finished the firstRound() in the proof.
   */
  public GE recoverCommitment();

  /**
   * Recovers value of the randomizers. This method may be called at any time.
   */
  public List<BigInt> recoverRandomizers();
}
