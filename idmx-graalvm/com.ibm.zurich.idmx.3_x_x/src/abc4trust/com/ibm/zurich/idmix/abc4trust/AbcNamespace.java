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

package com.ibm.zurich.idmix.abc4trust;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class AbcNamespace extends NamespacePrefixMapper {

  @Override
  public String getPreferredPrefix(final String base, final String other, final boolean arg2) {
    if (base.startsWith("http://abc4trust.eu/wp2")) {
      return "abc";
    } else if (base.equals("http://www.w3.org/2001/XMLSchema")) {
      return "xs";
    } else if (base.equals("http://www.w3.org/2001/XMLSchema-instance")) {
      return "xsi";
    } else if (base.equals("http://zurich.ibm.com")) {
      return "idmx";
    } else {
      return other;
    }
  }

}
