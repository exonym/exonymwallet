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
package com.ibm.zurich.idmx.orchestration.issuance;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.interfaces.orchestration.issuance.PhaseRecipient;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateRecipient;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.VerifierParameters;

public class StateRecipientImpl implements StateRecipient {

  private static final long serialVersionUID = 8043992107803081169L;
  
  private final PhaseRecipient phase;
  private final int stepOfPhase;
  private final CredentialTemplate ct;
  private final IssuanceTokenDescription itd;
  private final VerifierParameters vp;
  private final List<URI> credentials;
  private final List<URI> pseudonyms;
  private final List<Attribute> attributes;
  private final Serializable phaseDependantObject;


  public StateRecipientImpl(final PhaseRecipient phase, final int stepOfPhase, final CredentialTemplate ct,
                            final IssuanceTokenDescription itd, final VerifierParameters vp, final List<URI> credentials,
      final List<URI> pseudonyms, final List<Attribute> attributes, final Serializable phaseDependantObject) {
    this.phase = phase;
    this.stepOfPhase = stepOfPhase;
    this.ct = ct;
    this.vp = vp;
    this.credentials = new ArrayList<URI>(credentials);
    this.pseudonyms = new ArrayList<URI>(pseudonyms);
    this.attributes = new ArrayList<Attribute>(attributes);
    this.phaseDependantObject = phaseDependantObject;
    this.itd = itd;
  }

  public StateRecipientImpl(final StateRecipient lastState, final PhaseRecipient phase, final int stepOfPhase,
                            final Serializable phaseDependantObject) {
    this(phase, stepOfPhase, lastState.getCredentialTemplate(), lastState
        .getIssuanceTokenDescription(), lastState.getVerifierParameters(), lastState
        .getCredentialUrisForPresentation(), lastState.getPseudonymUrisForPresentation(), lastState
        .getSelfClaimedAttributes(), phaseDependantObject);
  }

  @Override
  public int getStepOfNextExpectedPhase() {
    return stepOfPhase;
  }

  @Override
  public CredentialTemplate getCredentialTemplate() {
    return ct;
  }

  @Override
  public VerifierParameters getVerifierParameters() {
    return vp;
  }

  @Override
  public PhaseRecipient getNextExpectedPhase() {
    return phase;
  }

  @Override
  public List<URI> getCredentialUrisForPresentation() {
    return Collections.unmodifiableList(credentials);
  }

  @Override
  public List<URI> getPseudonymUrisForPresentation() {
    return Collections.unmodifiableList(pseudonyms);
  }

  @Override
  public List<Attribute> getSelfClaimedAttributes() {
    return Collections.unmodifiableList(attributes);
  }

  @Override
  public Object getPhaseDependantObject() {
    return phaseDependantObject;
  }

  @Override
  public IssuanceTokenDescription getIssuanceTokenDescription() {
    return itd;
  }

}
