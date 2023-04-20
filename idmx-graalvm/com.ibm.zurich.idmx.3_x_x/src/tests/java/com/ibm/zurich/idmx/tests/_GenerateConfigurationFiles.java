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

package com.ibm.zurich.idmx.tests;

//import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
//import java.util.Calendar;
import java.util.List;

import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.zurich.idmx.dagger.CryptoTestModule;
//import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersTemplateWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
//import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer;
//import com.ibm.zurich.idmx.parameters.system.SystemParametersTemplateWrapper;
//import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.keyManager.KeyManagerException;
//import eu.abc4trust.xml.SystemParameters;
//import eu.abc4trust.xml.SystemParametersTemplate;

/**
 * 
 */
public class _GenerateConfigurationFiles {

  // System parameter template values for testing
//  private static final int SECURITY_LEVEL_FOR_TEST = 50;
//  private static final String HASH_FUNCTION_FOR_TEST = "SHA-256";
//  private static final int STATISTICAL_IND_FOR_TEST = 80;
//  private static final int ATTRIBUTE_LENGTH_FOR_TEST = 256;


  private static URI getBaseLocatinURI() {
    URI userDir = new File(System.getProperty("user.dir")).toURI();
    URI resourcesDir = userDir.resolve("src/tests/resources/");
    return resourcesDir.resolve("com/ibm/zurich/idmx/");
  }

  private static String getBaseLocation() {
    URI baseLocation = getBaseLocatinURI();
    return baseLocation.toString();
  }

//  private static String resolveBaseLocation(String path) {
//    if (path.startsWith("/")) {
//      path = path.substring(1);
//    }
//    if (!path.endsWith("/")) {
//      path = path + "/";
//    }
//    URI baseLocation =
//        new File(System.getProperty("user.dir")).toURI().resolve("src/tests/resources/")
//            .resolve("/com/ibm/zurich/idmx/");
//    return baseLocation.toString();
//  }


  static AbcComponent injector;

  // private static final String SYSTEM_PARAMETERS_TEMPLATE_BASE_FILENAME = BASE_LOCATION_PARAMETERS
  // .resolve("spTemplate_").toString();
  // private static final String SYSTEM_PARAMETERS_BASE_FILENAME = BASE_LOCATION_PARAMETERS.resolve(
  // "sp_").toString();
  // public static final String DEFAULT_SYSTEM_PARAMETERS_FILENAME =
  // BASE_LOCATION_PARAMETERS.resolve(
  // SYSTEM_PARAMETERS_BASE_FILENAME + "default.xml").toString();

  @BeforeClass
  public static void init() {
    injector = TestInitialisation.INJECTOR;
  }


  @Ignore("This test serves to generate/update all files that are needed to run the test cases. It is ignored if the parameter files do not need to be changed.")
  @Test
  public void generateAndUpdateAllParameterFiles() throws SerializationException,
      ConfigurationException, KeyManagerException, IOException {


    // Delete old files
    List<String> dirnames = getSystemParametersDirectories();
    deletePreviousConfigurationFiles(dirnames);

    // Create a template used for testing
    //SystemParametersTemplate spTemplate = createSystemParamtersTemplateForTests();

    // Generate system parameters based on the template
    //SystemParameters sp = generateSystemParametersForTests(spTemplate, dirnames, "sp_test.xml");



  }

  // @Test
  // public void verifyKeySetup() throws SerializationException, ConfigurationException,
  // KeyManagerException, IOException {
  //
  // // failing configuration (NOT always)
  // BigInteger pPrime =
  // new BigInteger(
  // "6595256003250326184853045693845624435311847560353668464172770618094864280392530870023394257327564081645413784311072035911146716020130969195846734366547051");
  // BigInteger qPrime =
  // new BigInteger(
  // "4722728624677188687423054231017294264894611665264474338971250027559778233610461039790641934633324802921175246543419937843352809647532973484112361075994729");
  // BigInteger p =
  // new BigInteger(
  // "13190512006500652369706091387691248870623695120707336928345541236189728560785061740046788514655128163290827568622144071822293432040261938391693468733094103");
  // BigInteger q =
  // new BigInteger(
  // "9445457249354377374846108462034588529789223330528948677942500055119556467220922079581283869266649605842350493086839875686705619295065946968224722151989459");
  // BigInteger modulus =
  // new BigInteger(
  // "124590417254497541066409204840657139232670317122835052990217152123396382565236220495483050476978838644299831472262029055559861692525004847301013422367115199969187929881452794152473269423557908079064570758884529536506505423900651103747972169519817424900951481748550647648439661147804116476988483590754111060277");
  // BigInteger order =
  // new BigInteger(
  // "31147604313624385266602301210164284808167579280708763247554288030849095641309055123870762619244709661074957868065507263889965423131251211825253355591778794333304668506605762400068354924430126916536529880649730812116303528653905774441038135361858375780795587142622234666123038037188195287275780918140806494179");
  // BigInteger accumulatedPrime =
  // new BigInteger(
  // "67843424801900923569594470032871351916159953226497490818196464515622368898887");
  // BigInteger originalAccumulator =
  // new BigInteger(
  // "30996947531309236117557781528031783705674969407557786492492848323341759837563617657421232306090408925069475545943574498913806789653007412649220201099243306824273874221217416507493036322936953320574190740906648700265602219039240760771334228849064955218981962466098608345615103349332284848354804052914234769049");
  //
  // // // successful configuration
  // // BigInteger pPrime =
  // // new BigInteger(
  // //
  // "5463922653448278329339702564365234009548646181694789041096548726068705939516007121482985189872800348961127791271082692977180428488685123978147785833611251");
  // // BigInteger qPrime =
  // // new BigInteger(
  // //
  // "4566998271378729635993476532182282859712315522853403848280834138554291466403278007568325226667045405049488041919000884801536624384516974505891249081030951");
  // // BigInteger p =
  // // new BigInteger(
  // //
  // "10927845306896556658679405128730468019097292363389578082193097452137411879032014242965970379745600697922255582542165385954360856977370247956295571667222503");
  // // BigInteger q =
  // // new BigInteger(
  // //
  // "9133996542757459271986953064364565719424631045706807696561668277108582932806556015136650453334090810098976083838001769603073248769033949011782498162061903");
  // // BigInteger modulus =
  // // new BigInteger(
  // //
  // "99814901252981475022632668224078152479401162172380923246714210938047077512905608576962480134863863471746243803464615956581022100798754806077893251162141328112914013746671987875131847218579367619872528905266693152394377594683072886035962224943600199907402461127648782022962535624386323711373982015455560603209");
  // // BigInteger order =
  // // new BigInteger(
  // //
  // "24953725313245368755658167056019538119850290543095230811678552734511769378226402144240620033715965867936560950866153989145255525199688701519473312790535327012768041023164014302193413530886407274487279952220228599407162087172065261866426030580691780053973609973995600463951744547570144326794253484346432829701");
  // // BigInteger accumulatedPrime =
  // // new BigInteger(
  // // "114100313335964780034588740348076613207091763155990731301412342977372240007367");
  // // BigInteger originalAccumulator =
  // // new BigInteger(
  // //
  // "41270159819155349026485966744662666335167194428592095577294175159686599733318521687745653634952778702644494075487202958819715339063897347403670708798399197060615050831571908894881875464115484160244682345166735940993667121644139452370162191141336648172396045789340084128933411697742668355739375219068150393529");
  //
  //
  // // verify relations of elements (re-calculate)
  // if (!p.equals(pPrime.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE))) {
  // System.err.println();
  // System.err.println(" -> p = 2p'+ 1 is computed incorrectly!!!");
  // }
  // if (!q.equals(qPrime.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE))) {
  // System.err.println();
  // System.err.println(" -> q = 2q'+ 1 is computed incorrectly!!!");
  // }
  // if (!modulus.equals(p.multiply(q))) {
  // System.err.println();
  // System.err.println(" -> modulus = pq is computed incorrectly!!!");
  // }
  // if (!order.equals(pPrime.multiply(qPrime))) {
  // System.err.println();
  // System.err.println(" -> order = p'q' is computed incorrectly!!!");
  // }
  //
  //
  // // verify assumptions (prime numbers)
  // if (!p.isProbablePrime(80)) {
  // System.err.println();
  // System.err.println(" -> p is not a prime!!!");
  // }
  // if (!pPrime.isProbablePrime(80)) {
  // System.err.println();
  // System.err.println(" -> p' is not a prime!!!");
  // }
  // if (!q.isProbablePrime(80)) {
  // System.err.println();
  // System.err.println(" -> q is not a prime!!!");
  // }
  // if (!pPrime.isProbablePrime(80)) {
  // System.err.println();
  // System.err.println(" -> q' is not a prime!!!");
  // }
  //
  // if (!originalAccumulator.mod(modulus).equals(originalAccumulator)) {
  // System.err.println();
  // System.err.println(" -> accumulator had not been reduced!!!");
  // }
  //
  // boolean fail = false;
  // int maxIterations = 1000;
  // int j = 0;
  // int k = 0;
  // for (int i = 0; i < maxIterations; i++) {
  //
  // accumulatedPrime = BigInteger.probablePrime(80, new Random());
  //
  // BigInteger inv = accumulatedPrime.modInverse(order);
  // BigInteger acc = originalAccumulator.modPow(inv, modulus);
  //
  // BigInteger calculatedPreviousAccumulator = acc.modPow(accumulatedPrime, modulus);
  // if (!inv.multiply(accumulatedPrime).mod(order).equals(BigInteger.ONE)) {
  // k++;
  // }
  //
  // if (!calculatedPreviousAccumulator.equals(originalAccumulator)) {
  //
  // // System.err.println();
  // // System.err.println(" -> re-calculation of accumulator fails - round (" + i + ")!!!");
  // fail = true;
  // j++;
  // }
  // }
  //
  // System.err.println(" Failure percentage using generator    : " + j * 100 / maxIterations +
  // "%");
  // System.err.println(" Failure percentage for neutral element: " + k * 100 / maxIterations +
  // "%");
  //
  // if (fail) {
  // Assert.fail();
  // }
  // }

  /**
   * Deletes all
   */
  private void deletePreviousConfigurationFiles(List<String> dirnames) {
    for (String dirname : dirnames) {
      TestUtils.deleteFilesInFolder(new File(dirname), "_test");
    }
  }

  private List<String> getBrandsIssuerParametersDirectories() {
    List<String> dirnames = new ArrayList<String>();

    return dirnames;
  }

  private List<String> getClIssuerParametersDirectories() {
    List<String> dirnames = new ArrayList<String>();

    return dirnames;
  }

  private List<String> getSystemParametersDirectories() {
    List<String> dirnames = new ArrayList<String>();
    dirnames.addAll(getClIssuerParametersDirectories());
    dirnames.addAll(getBrandsIssuerParametersDirectories());
    dirnames.add(getBaseLocation());
    return dirnames;
  }

//  private EcryptSystemParametersTemplateWrapper initSystemParametersTemplate()
//      throws ConfigurationException {
//    CryptoEngineIssuer cryptoEngineIssuer = injector.getInstance(CryptoEngineIssuer.class);
//    SystemParametersTemplate template = cryptoEngineIssuer.createSystemParametersTemplate();
//    EcryptSystemParametersTemplateWrapper spt = new EcryptSystemParametersTemplateWrapper(template);
//
//    // Set/change the default values in the system parameters template
//    spt.setSecurityLevel(SECURITY_LEVEL_FOR_TEST);
//    spt.setHashFunction(HASH_FUNCTION_FOR_TEST);
//    spt.setStatisticalInd(STATISTICAL_IND_FOR_TEST);
//    spt.setAttributeLength(ATTRIBUTE_LENGTH_FOR_TEST);
//    return spt;
//  }

  /**
   * Creates a system parameter template with values that may be used for testing.
   */
//  private SystemParametersTemplate createSystemParamtersTemplateForTests()
//      throws ConfigurationException, IOException, SerializationException {
//    // Create a template
//    SystemParametersTemplateWrapper spTemplateWrapper = initSystemParametersTemplate();
//
//    assertTrue(spTemplateWrapper != null);
//    assertTrue(spTemplateWrapper.getSystemParametersTemplate() != null);
//
//    String dirname = getBaseLocation();
//    Calendar calendar = Calendar.getInstance();
//    String spTemplateFilename =
//        "sp_tempate_" + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-"
//            + calendar.get(Calendar.DAY_OF_MONTH) + ".xml";
//    TestUtils.saveToFile(spTemplateWrapper.serialize(), dirname + spTemplateFilename);
//    TestUtils.print(dirname + spTemplateFilename);
//
//    return spTemplateWrapper.getSystemParametersTemplate();
//  }

  /**
   * Generates system parameters based on the given template.
   */
//  private SystemParameters generateSystemParametersForTests(SystemParametersTemplate spTemplate,
//      List<String> dirnames, String filename) throws SerializationException,
//      ConfigurationException, IOException {
//
//    CryptoEngineIssuer cryptoEngineIssuer = injector.getInstance(CryptoEngineIssuer.class);
//    SystemParameters systemParameters = cryptoEngineIssuer.setupSystemParameters(spTemplate);
//    SystemParametersWrapper systemParametersFacade = new SystemParametersWrapper(systemParameters);
//
//    assertTrue(systemParametersFacade != null);
//    assertTrue(systemParametersFacade.getSystemParameters() != null);
//
//    // save it to all indicated locations
//    for (String dirname : dirnames) {
//      TestUtils.saveToFile(systemParametersFacade.serialize(), dirname + filename);
//    }
//
//    return systemParameters;
//  }
}
