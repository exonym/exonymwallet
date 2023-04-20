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
package com.ibm.zurich.idmx.orchestration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.zurich.idmx.orchestration.issuance.IssuanceOrchestrationTest;
import com.ibm.zurich.idmx.orchestration.presentation.PresentationOrchestrationTest_Brands;
import com.ibm.zurich.idmx.orchestration.presentation.PresentationOrchestrationTest_Cl;

@RunWith(Suite.class)
@SuiteClasses({IssuanceOrchestrationTest.class, PresentationOrchestrationTest_Cl.class,
    PresentationOrchestrationTest_Brands.class})
public class _OrchestrationTestSuite {

}
