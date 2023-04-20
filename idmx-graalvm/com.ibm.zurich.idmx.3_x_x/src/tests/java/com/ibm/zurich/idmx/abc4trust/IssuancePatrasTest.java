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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;

public class IssuancePatrasTest {

  private PatrasHelper helper;

  private static final int COURSE_UID = 111;
  @SuppressWarnings("unused")
  private static final String SHA256 = "urn:abc4trust:1.0:encoding:string:sha-256";
  private static final int NAME = 222;
  private static final int LASTNAME = 333;
  private static final int UNIVERSITYNAME = 444;
  private static final int DEPARTMENTNAME = 666;
  private static final int MATRICULATIONNUMBER = 555;
  @SuppressWarnings("unused")
  private static final int ATTENDANCE_UID = 777;
  @SuppressWarnings("unused")
  private static final int LECTURE_UID = 888;

  @Before
  public void setUp() throws Exception {

    helper = new PatrasHelper();
    helper.setupIssuance(true);
    helper.loadDefaultResources();

  }

  @Test
  public void testIssueUniCredCl() throws Exception {

    helper.loadIssuerResources("parameters/ip-cl.xml", "parameters/sk-cl.xml");

    IssuancePolicy policy =
        TestUtils.getResource("patras/issuance/issuancePolicyPatrasUniversity.xml",
            IssuancePolicy.class, this, true);
    IssuanceTokenDescription itd =
        TestUtils.getResource("patras/issuance/issuanceTokenDescriptionPatrasUniversity.xml",
            IssuanceTokenDescription.class, this, true);

    List<Attribute> uniAttrs =
        helper.populateIssuerAttributes(this.populateUniveristyAttributes(),
            URI.create("urn:patras:credspec:credUniv"));

    helper.runDeviceIssuance(policy, itd, null, uniAttrs, "University");
  }

  @Test
  public void testIssueUniCredBrands() throws Exception {

    helper.loadIssuerResources("parameters/ip-brands.xml", "parameters/sk-brands.xml");
    helper.loadRevocationResources("parameters/ra_abce.xml");

    helper
        .loadCredentialSpecification("credSpecs/credentialSpecificationPatrasUniversity.xml");


    IssuancePolicy policy =
        TestUtils.getResource("patras/issuance/issuancePolicyPatrasUniversity_brands.xml",
            IssuancePolicy.class, this, true);
    IssuanceTokenDescription itd =
        TestUtils.getResource(
            "patras/issuance/issuanceTokenDescriptionPatrasUniversity_brands.xml",
            IssuanceTokenDescription.class, this, true);

    List<Attribute> uniAttrs =
        helper.populateIssuerAttributes(this.populateUniveristyAttributes(),
            URI.create("urn:patras:credspec:credUniv"));

    helper.runDeviceIssuance(policy, itd, null, uniAttrs, "UniversityBrands");
  }

  @Test
  public void testIssueUniCredCl_revocable() throws Exception {

    helper.loadIssuerResources("parameters/ip-cl.xml", "parameters/sk-cl.xml");
    helper.loadRevocationResources("parameters/ra_abce.xml");

    IssuancePolicy policy =
        TestUtils.getResource("patras/issuance/issuancePolicyPatrasUniversity_revocable.xml",
            IssuancePolicy.class, this, true);
    IssuanceTokenDescription itd =
        TestUtils.getResource(
            "patras/issuance/issuanceTokenDescriptionPatrasUniversity_revocable.xml",
            IssuanceTokenDescription.class, this, true);

    List<Attribute> uniAttrs =
        helper.populateIssuerAttributes(populateUniveristyAttributes_revocable(),
            URI.create("urn:patras:credspec:credUniv:revocable"));

    helper.runDeviceIssuance(policy, itd, null, uniAttrs, "University_revocable");
  }

  @Test
  public void testIssueCourseCred() throws Exception {

    helper.loadIssuerResources("parameters/ip-cl.xml", "parameters/sk-cl.xml");
    helper.loadIssuerResources("parameters/ip-brands.xml", "parameters/sk-brands.xml");

    IssuancePolicy policy =
        TestUtils.getResource("patras/issuance/issuancePolicyPatrasCourse.xml",
            IssuancePolicy.class, this, true);
    IssuanceTokenDescription itd =
        TestUtils.getResource("patras/issuance/issuanceTokenDescriptionPatrasCourse.xml",
            IssuanceTokenDescription.class, this, true);
    List<Attribute> courseAttrs =
        helper.populateIssuerAttributes(this.populateCourseAttributes(),
            URI.create("urn:patras:credspec:credCourse"));
    
    helper.runDeviceIssuance(policy, itd, null, courseAttrs, "Course");
  }
  
  @Test
  public void testIssueCourseCred_revocation() throws Exception {

    helper.loadIssuerResources("parameters/ip-cl.xml", "parameters/sk-cl.xml");
    helper.loadIssuerResources("parameters/ip-brands.xml", "parameters/sk-brands.xml");
    
    helper.loadRevocationResources("parameters/ra_abce.xml");

    IssuancePolicy policy =
        TestUtils.getResource("patras/issuance/issuancePolicyPatrasCourse_revocation.xml",
            IssuancePolicy.class, this, true);
    IssuanceTokenDescription itd =
        TestUtils.getResource("patras/issuance/issuanceTokenDescriptionPatrasCourse_revocation.xml",
            IssuanceTokenDescription.class, this, true);
    List<Attribute> courseAttrs =
        helper.populateIssuerAttributes(this.populateCourseAttributes(),
            URI.create("urn:patras:credspec:credCourse"));
    
    Credential credUni =
        TestUtils.getResource("patras/credentials/credentialUniversity_revocable.xml", Credential.class,
            this, true);

    helper.runDeviceIssuance(policy, itd, credUni, courseAttrs, "Course");
  }

  @Test
  public void testIssueTombolaCred() throws Exception {

    helper.loadIssuerResources("parameters/ip-cl.xml", "parameters/sk-cl.xml");
    helper.loadIssuerResources("parameters/ip-brands.xml", "parameters/sk-brands.xml");

    IssuancePolicy policy =
        TestUtils.getResource("patras/issuance/issuancePolicyPatrasTombola.xml",
            IssuancePolicy.class, this, true);
    IssuanceTokenDescription itd =
        TestUtils.getResource("patras/issuance/issuanceTokenDescriptionPatrasTombola.xml",
            IssuanceTokenDescription.class, this, true);

    List<Attribute> tombolaAttrs = Collections.emptyList();

    Credential credUni =
        TestUtils.getResource("patras/credentials/credentialUniversity.xml", Credential.class,
            this, true);

    helper.runDeviceIssuance(policy, itd, credUni, tombolaAttrs, "Tombola");
  }



  private Map<String, Object> populateCourseAttributes() {
    Map<String, Object> att = new HashMap<String, Object>();
    att.put("urn:patras:credspec:credCourse:courseid", COURSE_UID);
    return att;
  }

  private Map<String, Object> populateUniveristyAttributes() {
    Map<String, Object> att = new HashMap<String, Object>();
    att.put("urn:patras:credspec:credUniv:firstname", NAME);
    att.put("urn:patras:credspec:credUniv:lastname", LASTNAME);
    att.put("urn:patras:credspec:credUniv:university", UNIVERSITYNAME);
    att.put("urn:patras:credspec:credUniv:department", DEPARTMENTNAME);
    att.put("urn:patras:credspec:credUniv:matriculationnr", MATRICULATIONNUMBER);
    return att;
  }

  private Map<String, Object> populateUniveristyAttributes_revocable() {
    Map<String, Object> att = new HashMap<String, Object>();
    att.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle", null);
    att.put("urn:patras:credspec:credUniv:firstname", NAME);
    att.put("urn:patras:credspec:credUniv:lastname", LASTNAME);
    att.put("urn:patras:credspec:credUniv:university", UNIVERSITYNAME);
    att.put("urn:patras:credspec:credUniv:department", DEPARTMENTNAME);
    att.put("urn:patras:credspec:credUniv:matriculationnr", MATRICULATIONNUMBER);
    return att;
  }

}
