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

package com.ibm.zurich.idmx.interfaces.util.group;

import com.ibm.zurich.idmx.interfaces.util.BigInt;

public interface GroupFactory {

  KnownOrderGroup createPrimeOrderGroup(final BigInt modulus, final BigInt subgroupOrder);

  HiddenOrderGroup createSignedQuadraticResiduesGroup(final BigInt modulus);

  /**
   * Creates a Paillier group with operation being multiplication modulo Z_{n^2}.
   */
  PaillierGroup createPaillierGroup(final BigInt n);

  /**
   * Creates a group with operation being multiplication modulo Z_{pq}.
   */
  KnownOrderGroup createResiduesModPQGroup(final BigInt p, final BigInt q);

  /**
   * Creates a group with operation being multiplication modulo Z_{n}.
   */
  HiddenOrderGroup createSRSAGroup(final BigInt modulus);

}
