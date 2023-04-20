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
package com.ibm.zurich.idmx.proofEngine.builderVerifier;

import java.util.List;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;

class ZkBuilderVerifierVerify implements ZkVerifierStateVerify {

  private final StatePerModule state;
  private final StateForVerifier verifierState;

  public ZkBuilderVerifierVerify(final StatePerModule moduleState, final StateForVerifier state) {
    this.state = moduleState;
    this.verifierState = state;
  }

  @Override
  public BigInt getSValueAsInteger(final String name) throws ProofException {
    try {
      if (verifierState.isRegisteredAttribute(name)) {
        state.assertRegistered(name);
        return verifierState.getSValueOfAttribute(name);
      } else {
        return state.getSValueAsInteger(name);
      }
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public byte[] getSValueAsObject(final String name) throws ProofException {
    try {
      return state.getSValueAsByteArray(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public BigInt getDValueAsInteger(final String name) throws ProofException {
    try {
      return state.getDValueAsInteger(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public <GE extends GroupElement<?, GE, ?>> GE getDValueAsGroupElement(final String name,
		  final Group<?, GE, ?> group) throws ProofException {
    try {
      return state.getDValueAsGroupElement(name, group);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public byte[] getDValueAsObject(final String name) throws ProofException {
    try {
      return state.getDValueAsByteArray(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public boolean isRevealedAttribute(final String name) throws ProofException {
    try {
      state.assertRegistered(name);
      return verifierState.isRevealedAttribute(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public BigInt getValueOfRevealedAttribute(final String name) throws ProofException {
    try {
      state.assertRegistered(name);
      return verifierState.getValueOfAttribute(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public BigInt getChallenge() throws ProofException {
    return verifierState.getChallenge();
  }

  @Override
  public List<byte[]> getHashContributions() throws ProofException {
    return verifierState.getHashContributions();
  }

  @Override
  public int getPositionInHashContributionsList() throws ProofException {
    return verifierState.getIndexOfHashContribution(state.getModuleName());
  }

  @Override
  public void checkHashContributionOfDValue(final String name, final byte[] hash) throws ProofException {
    try {
      state.checkHashContributionOfDValue(name, hash);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void checkNValue(final String name, final byte[] hashContribution) throws ProofException {
    try {
      state.addNValue(name, hashContribution);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void checkNValue(final String name, final BigInt value) throws ProofException {
    try {
      state.addNValue(name, value);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void checkNValue(final String name, final GroupElement<?, ?, ?> value)
		  throws ProofException {
    try {
      state.addNValue(name, value);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void checkTValue(final String name, final GroupElement<?, ?, ?> tValue)
		  throws ProofException {
    try {
      state.addTValue(name, tValue);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void checkHashContributionOfBuildingBlock(final byte[] hashContribution) throws ProofException {
    try {
      state.setHashContribution(hashContribution);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void checkValueOfAttribute(final String attributeId, final BigInt value) throws ProofException {
    try {
      state.assertRegistered(attributeId);
      final BigInt oldValue = verifierState.getValueOfAttribute(attributeId);
      if (!oldValue.equals(value)) {
        throw new ProofException("Value of attribute incorrect " + attributeId);
      }
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }
  
  @Override
  public ResidueClass getResidueClass(String name) throws ProofException {
    try {
      state.assertRegistered(name);
      return verifierState.getResidueClass(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

}
