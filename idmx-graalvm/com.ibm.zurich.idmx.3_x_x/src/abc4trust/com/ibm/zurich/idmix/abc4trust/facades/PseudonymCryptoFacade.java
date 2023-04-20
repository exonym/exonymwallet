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
package com.ibm.zurich.idmix.abc4trust.facades;

import java.net.URI;

import javax.xml.bind.JAXBElement;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;

import eu.abc4trust.xml.AbstractPseudonym;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.ScopeExclusivePseudonym;

public class PseudonymCryptoFacade {
  private final JAXBElement<CryptoParams> cp;

  public PseudonymCryptoFacade() {
    this(new ObjectFactory().createCryptoParams());
  }

  public PseudonymCryptoFacade(final CryptoParams cp) {
    this.cp = new ObjectFactory().createCryptoParams(cp);
  }

//  @SuppressWarnings("unchecked")
  public AbstractPseudonym getAbstractPseudonym() {
//	    return ((JAXBElement<AbstractPseudonym>) cp.getValue().getContent().get(0)).getValue();
	  XmlUtils.fixNestedContent(cp.getValue());
	    return (AbstractPseudonym) cp.getValue().getContent().get(0);
  }

  public void setAbstractPseudonym(final AbstractPseudonym ap) {
    if (cp.getValue().getContent().size() != 0) {
      throw new RuntimeException("Already contains an abstract pseudonym");
    }
    cp.getValue().getContent().add(new ObjectFactory().createAbstractPseudonym(ap));
  }

  @Deprecated
  public void setScopeExclusivePseudonym(final URI scope, final URI device, final byte[] value) {
    // TODO(enr): This method is needed for compatibility with Abc4trust
    final ScopeExclusivePseudonym sep = new ObjectFactory().createScopeExclusivePseudonym();
    sep.setDeviceUid(device);
    sep.setScope(scope);
    final ScopeExclusivePseudonymBuildingBlock bb =
        new ScopeExclusivePseudonymBuildingBlock(null, null, null, null, null, new BigIntFactoryImpl());
    final BigInt value_bi = bb.getPseudonymValueFromBytes(value);
    sep.setValue(value_bi.getValue());
    setAbstractPseudonym(sep);
  }
  
  public CryptoParams getCryptoParams() {
    return cp.getValue();
  }
}
