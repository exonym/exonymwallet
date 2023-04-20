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
package com.ibm.zurich.idmx.buildingBlock.signature.cl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.signature.IssuanceTestHelper;
import com.ibm.zurich.idmx.buildingBlock.signature.IssuanceTestHelper.CarryOver;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;

public class ClIssuanceTest {
  private IssuanceTestHelper ith;
 

  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    ith = new IssuanceTestHelperCl();
  }

  @Test
  public void issuanceFromScratchTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    ith.issuance(carryOver, false, 101, CarryOver.NONE);
  }
  
  @Test
  public void issuanceWithManualCarryOverNoneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    ith.issuance(carryOver, false, 101, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithManualCarryOverOneTest() throws SerializationException, ConfigurationException,
      IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, false, false);
    ith.issuance(carryOver, false, 102, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithManualCarryOverAllTest() throws SerializationException, ConfigurationException,
      IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, true, true);
    ith.issuance(carryOver, false, 103, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithSmartcardManualCarryOverNoneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    ith.issuance(carryOver, true, 201, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithSmartcardManualCarryOverOneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, true);
    ith.issuance(carryOver, true, 202, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithSmartcardManualCarryOverAllTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, true, true);
    ith.issuance(carryOver, true, 203, CarryOver.MANUAL);
  }
  //-
  @Test
  public void issuanceWithCarryOverNoneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    ith.issuance(carryOver, false, 301, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithCarryOverOneTest() throws SerializationException, ConfigurationException,
      IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, false, false);
    ith.issuance(carryOver, false, 302, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithCarryOverAllTest() throws SerializationException, ConfigurationException,
      IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, true, true);
    ith.issuance(carryOver, false, 303, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithSmartcardCarryOverNoneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    ith.issuance(carryOver, true, 401, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithSmartcardCarryOverOneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, true);
    ith.issuance(carryOver, true, 402, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithSmartcardCarryOverAllTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, true, true);
    ith.issuance(carryOver, true, 403, CarryOver.CARRY_OVER);
  }

}