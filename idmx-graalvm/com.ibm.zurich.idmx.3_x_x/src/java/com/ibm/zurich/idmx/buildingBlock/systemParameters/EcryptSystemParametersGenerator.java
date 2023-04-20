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

package com.ibm.zurich.idmx.buildingBlock.systemParameters;

import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.SystemParametersTemplate;

/**
 * 
 */
public class EcryptSystemParametersGenerator extends SystemParameterGeneratorBuildingBlock {


  // System parameters element names
  public static final String RSA_MODULUS_LENGTH_NAME = "rsaModulusLength";

  @Override
  public final String getImplementationIdSuffix() {
    return "ecrypt2011";
  }

  @Override
  public void addBuildingBlockSystemParametersTemplate(final SystemParametersTemplate template) {
    final EcryptSystemParametersTemplateWrapper spt = new EcryptSystemParametersTemplateWrapper(template);

    spt.setSecurityLevel(Configuration.defaultSecurityLevel());
    spt.setHashFunction(Configuration.defaultHashFunction());

  }

  @Override
  public void generateBuildingBlockSystemParameters(final SystemParametersTemplate template,
                                                    final SystemParameters systemParameters) throws ConfigurationException {

    final EcryptSystemParametersWrapper sp = new EcryptSystemParametersWrapper(systemParameters);
    final EcryptSystemParametersTemplateWrapper spt = new EcryptSystemParametersTemplateWrapper(template);

    sp.setSecurityLevel(spt.getSecurityLevel());
    sp.setHashFunction(spt.getHashFunction());

    // calculate the bit length values based on the configuration values
    calculateGeneralSystemParameters(spt, sp);
  }

  /**
   * 
   * @return System parameters calculated according to the conditions as in Camenisch et al. (2010).
   *         http://domino.research.ibm.com/library/cyberdig
   *         .nsf/papers/EEB54FF3B91C1D648525759B004FBBB1 /$File/rz3730_revised.pdf
   * @throws ConfigurationException
   * @throws javax.naming.ConfigurationException
   */
  private void calculateGeneralSystemParameters(final EcryptSystemParametersTemplateWrapper spt,
                                                final EcryptSystemParametersWrapper sp) throws ConfigurationException {

    final int securityLevel = spt.getSecurityLevel();

    // Generate the bit length parameters
    final int rsaModulusBitlength = securityLevelEquivaltentRsaModulusBitlength(securityLevel);

    // Set the parameters in the system parameters
    sp.setParameter(RSA_MODULUS_LENGTH_NAME, rsaModulusBitlength);
  }

  /**
   * @return RSA modulus length for a given security level according to Ecrypt II recommendations
   *         (2011).
   */
  public static int securityLevelEquivaltentRsaModulusBitlength(final int securityLevel) {
    double min_n = 0.0;
    double max_n = 1000000.0;
    while (Math.abs(max_n - min_n) >= 1e-3) {
      final double try_n = (min_n + max_n) / 2;
      final double try_sec = rsaEquivalentSecurityLevel(try_n);
      if (try_sec > securityLevel) {
        max_n = try_n;
      } else {
        min_n = try_n;
      }
    }
    int round = 16;
    return (int) Math.round(min_n / round) * round;
  }

  /**
   * @return Security level of the given RSA modulus bit length <var>modulusBitLength</var>
   *         according to Ecrypt II recommendations (2011).
   *         http://www.ecrypt.eu.org/documents/D.SPA.20.pdf
   */
  //TODO(ksa) bit length in double?!
  //FIXME(ksa) Code sometimes does floor/round OR ceil
  public static double rsaEquivalentSecurityLevel(final double modulusBitLength) {
    return Math.pow(64. / 9., 1 / 3.) * Math.log(Math.exp(1)) / Math.log(2)
        * Math.pow(modulusBitLength * Math.log(2), 1 / 3.)
        * Math.pow(Math.log(modulusBitLength * Math.log(2)), 2 / 3.) - 14;
  }

}
