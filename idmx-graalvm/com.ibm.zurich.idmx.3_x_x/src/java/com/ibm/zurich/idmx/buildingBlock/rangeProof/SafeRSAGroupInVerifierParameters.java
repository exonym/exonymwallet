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
package com.ibm.zurich.idmx.buildingBlock.rangeProof;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.IdemixVerifierParameters;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Parameter;
import eu.abc4trust.xml.UriParameter;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.VerifierParametersTemplate;

public class SafeRSAGroupInVerifierParameters {

  private static final String PARAMETER_NAME = "safeRsaGroupReference:ip:cl";
  private final KeyManager keyManager;
  private final BuildingBlockFactory bbFactory;
  private final GroupFactory gf;

  @Inject
  public SafeRSAGroupInVerifierParameters(final KeyManager keyManager, final BuildingBlockFactory bbFactory,
    final GroupFactory gf) {
    this.keyManager = keyManager;
    this.bbFactory = bbFactory;
    this.gf = gf;
  }

  /**
   * Adds implementation-specific parameters to the verifier parameter template.
   * 
   * @param spWrapper
   * @param vpt
   * @throws KeyManagerException
   */
  public void populateVerifierParameterTemplate(final SystemParametersWrapper spWrapper,
                                                final List<Parameter> vpt) {
    try {
      // Abort if we already added parameters
      for (final Parameter p : vpt) {
        if (p.getName().equals(PARAMETER_NAME)) {
          return;
        }
      }
      // Add all CL issuers
      boolean added = false;
      final ClSignatureBuildingBlock clbb =
          bbFactory.getBuildingBlockByClass(ClSignatureBuildingBlock.class);
      final List<URI> issuers = keyManager.listIssuerParameters();

      for (final URI issuerUri : issuers) {
        IssuerParameters ip = keyManager.getIssuerParameters(issuerUri);
        if (ip.getAlgorithmID().equals(clbb.getImplementationId())) {
          final UriParameter param = new ObjectFactory().createUriParameter();
          param.setName(PARAMETER_NAME);
          param.setValue(issuerUri);
          vpt.add(param);
          added = true;
        }
      }
      if (!added) {
        throw new RuntimeException(
            "Could not find any CL issuer parameters in key manager. At least one is needed to get a safe RSA group in the verifier parameters.");
      }

    } catch (KeyManagerException|ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds implementation-specific parameters to the verifier parameters given the template.
   * 
   * @param spWrapper
   * @param vpt
   * @param vp
   */
  public void populateVerifierParameters(SystemParametersWrapper spWrapper,
      VerifierParametersTemplate vpt, List<Parameter> vp) {
    // Abort if we already added parameters
    for (final Parameter p : vp) {
      if (p.getName().equals(PARAMETER_NAME)) {
        return;
      }
    }
    boolean added = false;
    // Copy over parameters from verifier template
    for (final Parameter p : vpt.getParameter()) {
      if (p.getName().equals(PARAMETER_NAME)) {
        vp.add(p);
        added = true;
      }
    }
    if (!added) {
      throw new RuntimeException("Could not find any CL issuer parameters in key manager. "
          + "At least one is needed to get a safe RSA group in the verifier parameters.");
    }

  }

  public class GroupDescription {
    public final URI issuerUri;
    public final HiddenOrderGroup group;
    public final HiddenOrderGroupElement S;
    public final HiddenOrderGroupElement Z;
    
    public GroupDescription(final HiddenOrderGroup group, final HiddenOrderGroupElement S, final HiddenOrderGroupElement Z, final URI issuerUri) {
      this.group = group;
      this.S = S;
      this.Z = Z;
      this.issuerUri = issuerUri;
    }
  }

  public GroupDescription getGroupDescription(VerifierParameters vp) {
    if (vp != null) {
      XmlUtils.fixNestedContent(vp.getCryptoParams());
//      IdemixVerifierParameters ivp =
//          (IdemixVerifierParameters) JAXBIntrospector
//              .getValue(vp.getCryptoParams().getContent().get(0));
      final IdemixVerifierParameters ivp = (IdemixVerifierParameters) vp.getCryptoParams().getContent().get(0);
      // Use the first issuer parameters that are found
      for (final Parameter p : ivp.getParameter()) {
        if (p.getName().equals(PARAMETER_NAME)) {
          final UriParameter up = (UriParameter) p;
          final GroupDescription ret = getGroupDescription(up.getValue());
          if(ret != null) {
            return ret;
          }
        }
      }
      throw new RuntimeException("Could not find any issuer parameters that are "
          + "compatible with the verifier parameters in key manager. "
          + "At least one is needed to get a safe RSA group in the verifier parameters.");
    } else {
      throw new RuntimeException("This method needs non-null verifier parameters.");
    }
  }
  
  public GroupDescription getGroupDescription(final URI issuerUri) {
    final IssuerParameters ip;
    try {
      ip = keyManager.getIssuerParameters(issuerUri);
      if (ip == null) {
        return null;
      }
      final IssuerParametersFacade ipf = new IssuerParametersFacade(ip);
      final ClPublicKeyWrapper clpkw = new ClPublicKeyWrapper(ipf.getPublicKey());
      final URI issuer = ip.getParametersUID();
      final HiddenOrderGroup group = gf.createSignedQuadraticResiduesGroup(clpkw.getModulus());;
      final HiddenOrderGroupElement S = group.valueOf(clpkw.getS());
      final HiddenOrderGroupElement Z = group.valueOf(clpkw.getZ());
      final GroupDescription ret = new GroupDescription(group, S, Z, issuer);
      return ret;
    } catch (KeyManagerException|ConfigurationException e) {
      return null;
    }
  }

  /**
   * Get group description from issuer URI (and make sure it appears in the verifier parameters)
   * @param vp
   * @param issuer
   * @return
   */
  public GroupDescription getGroupDescription(final VerifierParameters vp, final URI issuer) {
    if (vp != null) {
      XmlUtils.fixNestedContent(vp.getCryptoParams());

      final IdemixVerifierParameters ivp =
          (IdemixVerifierParameters) JAXBIntrospector
              .getValue(vp.getCryptoParams().getContent().get(0));
      // Use the first issuer parameters that are found
      for (final Parameter p : ivp.getParameter()) {
        if (p.getName().equals(PARAMETER_NAME)) {
          final UriParameter up = (UriParameter) p;
            if(issuer.equals(up.getValue())) {
              final GroupDescription ret = getGroupDescription(up.getValue());
            if(ret != null) {
              return ret;
            }
          }
        }
      }
    }
    throw new RuntimeException("Could not find any issuer parameters that are "
        + "compatible with the verifier parameters in key manager. "
        + "At least one is needed to get a safe RSA group in the verifier parameters.");
  }
}
