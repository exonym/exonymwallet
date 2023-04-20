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
package com.ibm.zurich.idmx.abc4trust;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;

public class PatrasRangeProofTest {

  private PatrasHelper helper;

  @Before
  public void setUp() throws Exception {
    helper = new PatrasHelper();
    helper.setupPresentation(true, true);
    helper.loadDefaultResources();
  }

  @Test
  public void testLoginCourseEvaluation() throws Exception {

    PresentationToken pt =
        TestUtils.getResource("patras/presentation/presentationTokenPatrasRangeProof.xml",
            PresentationToken.class, this, true);
    PresentationTokenDescription ptd = pt.getPresentationTokenDescription();

    Credential credCourse =
        TestUtils.getResource("patras/credentials/credentialCourse.xml", Credential.class, this,
            true);

    Credential credUni =
        TestUtils.getResource("patras/credentials/credentialUniversity.xml", Credential.class,
            this, true);

    helper.loadCredential(credCourse);
    helper.loadCredential(credUni);
    URI credUriCourse = credCourse.getCredentialDescription().getCredentialUID();
    URI credUriUni = credUni.getCredentialDescription().getCredentialUID();

    List<URI> creds = new LinkedList<URI>();
    creds.add(credUriCourse);
    creds.add(credUriUni);

    helper.runPresentationProtocol(ptd, creds);
  }

}
