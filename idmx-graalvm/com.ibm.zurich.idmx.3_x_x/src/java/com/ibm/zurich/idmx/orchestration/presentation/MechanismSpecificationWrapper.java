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
package com.ibm.zurich.idmx.orchestration.presentation;

import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.MechanismSpecification;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;


public class MechanismSpecificationWrapper extends ParametersHelper {

  // private MechanismSpecification ms;
  private final JAXBElement<MechanismSpecification> mechanismSpecification;

  public MechanismSpecificationWrapper() {
    this(new ObjectFactory().createMechanismSpecification());
    getMechanismSpecification().setProofSystem(Configuration.PROOF_SYSTEM);
  }

  public MechanismSpecificationWrapper(final MechanismSpecification mechanismSpecification) {
    this.mechanismSpecification =
        new ObjectFactory().createMechanismSpecification(mechanismSpecification);
    this.parameterListHelper = new ParameterListHelper(getMechanismSpecification().getParameter());
  }

  @SuppressWarnings("unchecked")
  private MechanismSpecificationWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = SystemParameters.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.mechanismSpecification = (JAXBElement<MechanismSpecification>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  // public MechanismSpecificationWrapper() {
  // ms = new ObjectFactory().createMechanismSpecification();
  // this.parameterListHelper = new ParameterListHelper(ms.getParameter());
  // }
  //
  // public MechanismSpecificationWrapper(MechanismSpecification ms) {
  // this.ms = ms;
  // this.parameterListHelper = new ParameterListHelper(ms.getParameter());
  // }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return parameterName;
  }


  public URI getSystemParameterUri() {
    return getMechanismSpecification().getSystemParametersId();
  }

  public void setSystemParameterId(URI systemParameterUri) {
    getMechanismSpecification().setSystemParametersId(systemParameterUri);
  }

  public String getProofSystem() {
    return getMechanismSpecification().getProofSystem();
  }

  public void setProofSystem(final String proofSystem) {
    getMechanismSpecification().setProofSystem(proofSystem);
  }

  public void setImplementationChoice(final String moduleId, final URI implementationUid) {
    setParameter(moduleId, implementationUid);
  }

  public URI getImplementationChoice(final String moduleId) throws ConfigurationException {
    return (URI) getParameter(moduleId);
  }


  /**
   * Computes a cryptographic hash of the system parameters
   * 
   * @throws ConfigurationException
   */
  public byte[] getHash(final String hashAlgorithm) throws ConfigurationException {
    try {
      final byte[] toHash = JaxbHelperClass.canonicalXml(mechanismSpecification);
      final MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
      md.update(toHash);
      return md.digest();
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new ConfigurationException(e);
    }
  }

  /**
   * @return
   */
  public MechanismSpecification getMechanismSpecification() {
    return (MechanismSpecification) JAXBIntrospector.getValue(mechanismSpecification);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(mechanismSpecification);
  }

  public static MechanismSpecificationWrapper deserialize(final String mechanismSpecification)
      throws SerializationException {
    return new MechanismSpecificationWrapper(JaxbHelperClass.deserialize(mechanismSpecification));
  }

  public static MechanismSpecificationWrapper deserialize(final InputStream inputStream)
      throws SerializationException {
    return new MechanismSpecificationWrapper(JaxbHelperClass.deserialize(inputStream));
  }
}
