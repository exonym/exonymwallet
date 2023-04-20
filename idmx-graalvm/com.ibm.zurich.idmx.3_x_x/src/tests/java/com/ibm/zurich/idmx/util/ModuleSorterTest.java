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
package com.ibm.zurich.idmx.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;

import com.ibm.zurich.idmx.exception.TopologicalSortFailedException;
import com.ibm.zurich.idmx.interfaces.util.ModuleSorter;

public class ModuleSorterTest {

  @Test
  public void testDirectDependency() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);
    s.registerDemand("n", 1);

    assertEquals(s.sortModules(), Arrays.asList("m", "n"));
  }

  @Test
  public void testMultiDirectDependency() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);
    s.registerSupply("m", 1);
    s.registerDemand("n", 1);
    s.registerDemand("n", 1);
    s.registerDemand("n", 1);

    assertEquals(s.sortModules(), Arrays.asList("m", "n"));
  }

  @Test
  public void testNaturalOrderingSimple() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("z", 1);
    s.registerSupply("y", 1);
    s.registerSupply("w", 1);
    s.registerSupply("x", 1);
    s.registerSupply("v", 1);

    assertEquals(s.sortModules(), Arrays.asList("v", "w", "x", "y", "z"));
  }

  @Test
  public void testIndirectDependency() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);

    s.registerDemand("n", 1);
    s.registerSupply("n", 2);

    s.registerDemand("o", 2);

    assertEquals(s.sortModules(), Arrays.asList("m", "n", "o"));
  }

  @Test
  public void testMultipleDirectDependencies() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);

    s.registerDemand("n", 1);
    s.registerSupply("n", 2);

    s.registerDemand("o", 1);
    s.registerSupply("o", 3);

    s.registerDemand("p", 2);
    s.registerDemand("p", 3);

    assertEquals(s.sortModules(), Arrays.asList("m", "n", "o", "p"));
  }

  @Test
  public void testDirectAndIndirectDependencies2() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);

    s.registerDemand("n", 1);
    s.registerSupply("n", 2);

    s.registerDemand("o", 1);
    s.registerSupply("o", 3);

    s.registerDemand("p", 1);
    s.registerDemand("p", 2);
    s.registerDemand("p", 3);

    assertEquals(s.sortModules(), Arrays.asList("m", "n", "o", "p"));
  }


  @Test(expected = TopologicalSortFailedException.class)
  public void testCycle() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);
    s.registerDemand("m", 2);

    s.registerSupply("n", 2);
    s.registerDemand("n", 1);

    s.sortModules();
  }

  @Test
  public void testSatisfiableCycle() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);
    s.registerSupply("m", 3);

    s.registerDemand("n", 1);
    s.registerSupply("n", 2);
    s.registerDemand("n", 3);

    s.registerDemand("o", 2);
    s.registerSupply("o", 3);

    assertEquals(s.sortModules(), Arrays.asList("m", "n", "o"));
  }

  @Test
  public void testSatisfiableCycle2() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("l", 5);

    s.registerSupply("m", 1);
    s.registerSupply("m", 3);

    s.registerSupply("n", 2);
    s.registerDemand("n", 1);
    s.registerDemand("n", 3);
    s.registerDemand("n", 5);

    s.registerSupply("o", 3);
    s.registerDemand("o", 2);
    s.registerDemand("o", 5);

    assertEquals(s.sortModules(), Arrays.asList("l", "m", "n", "o"));
  }

  @Test
  public void testSatisfiableCycle2Reordered() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();

    s.registerSupply("m", 3);
    s.registerSupply("n", 2);
    s.registerDemand("n", 1);
    s.registerDemand("o", 2);
    s.registerDemand("n", 5);
    s.registerDemand("o", 5);
    s.registerDemand("n", 3);
    s.registerSupply("l", 5);
    s.registerSupply("o", 3);
    s.registerSupply("m", 1);

    assertEquals(s.sortModules(), Arrays.asList("l", "m", "n", "o"));
  }

  @Test
  public void testSelfCycleSimple() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);
    s.registerDemand("m", 1);

    assertEquals(s.sortModules(), Arrays.asList("m"));
  }

  @Test
  public void testSatisfiableCycleAndSelfCycles() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerDemand("l", 5);
    s.registerSupply("l", 5);

    s.registerSupply("m", 1);
    s.registerDemand("m", 1);
    s.registerDemand("m", 3);
    s.registerSupply("m", 3);

    s.registerDemand("n", 2);
    s.registerSupply("n", 2);
    s.registerDemand("n", 1);
    s.registerDemand("n", 3);
    s.registerDemand("n", 5);

    s.registerSupply("o", 3);
    s.registerDemand("o", 3);
    s.registerDemand("o", 2);
    s.registerDemand("o", 5);

    assertEquals(Arrays.asList("l", "m", "n", "o"), s.sortModules());
  }

  @Test(expected = TopologicalSortFailedException.class)
  public void testNotSatisfiable() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerSupply("m", 1);
    s.registerSupply("m", 3);

    s.registerDemand("n", 1);
    s.registerSupply("n", 2);

    s.registerDemand("o", 2);
    s.registerSupply("o", 3);
    s.registerDemand("o", 999);

    assertNull(s.sortModules());
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterDemandNotNull1() {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();

    s.registerDemand(null, 1);
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterDemandNotNull2() {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();

    s.registerDemand("m", null);
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterSupplyNotNull1() {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();

    s.registerSupply(null, 1);
  }

  @Test(expected = NullPointerException.class)
  public void testRegisterSupplyNotNull2() {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();

    s.registerSupply("m", null);
  }

  @Test
  public void testNaturalOrdering() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerDemand("p", 1);
    s.registerDemand("p", 2);
    s.registerDemand("p", 3);

    s.registerDemand("c", 2);
    s.registerDemand("b", 2);

    s.registerSupply("m", 1);

    s.registerDemand("n", 1);
    s.registerSupply("n", 2);

    s.registerDemand("o", 1);
    s.registerSupply("o", 3);

    s.registerDemand("a", 1);

    assertEquals(s.sortModules(), Arrays.asList("m", "a", "n", "b", "c", "o", "p"));
  }

  @Test
  public void testRegisterModuleSimple() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerModule("m");

    assertEquals(s.sortModules(), Arrays.asList("m"));
  }

  @Test
  public void testRegisterModule() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();

    s.registerModule("a");

    s.registerSupply("m", 3);
    s.registerSupply("n", 2);
    s.registerDemand("n", 1);
    s.registerDemand("o", 2);
    s.registerDemand("n", 5);

    s.registerModule("b");

    s.registerDemand("o", 5);
    s.registerDemand("n", 3);
    s.registerSupply("l", 5);
    s.registerSupply("o", 3);
    s.registerSupply("m", 1);

    s.registerModule("c");

    assertEquals(s.sortModules(), Arrays.asList("a", "b", "c", "l", "m", "n", "o"));
  }

  @Test
  public void testRegisteredModulesMultipleTimes() throws TopologicalSortFailedException {
    ModuleSorter<String, Integer> s = new ModuleSorterImpl<String, Integer>();
    s.registerModule("l");
    s.registerSupply("l", 5);

    s.registerSupply("m", 1);
    s.registerSupply("m", 3);
    s.registerModule("m");

    s.registerSupply("n", 2);
    s.registerDemand("n", 1);
    s.registerModule("n");
    s.registerDemand("n", 3);
    s.registerDemand("n", 5);

    s.registerModule("o");
    s.registerSupply("o", 3);
    s.registerModule("o");
    s.registerDemand("o", 2);
    s.registerModule("o");
    s.registerDemand("o", 5);
    s.registerModule("o");

    assertEquals(s.sortModules(), Arrays.asList("l", "m", "n", "o"));
  }
}
