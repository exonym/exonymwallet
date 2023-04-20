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

package com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ibm.zurich.idmx.annotations.VisibleForTest;
import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.RangeProofBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.SafeRSAGroupInVerifierParameters;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.SafeRSAGroupInVerifierParameters.GroupDescription;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombination.LinearCombinationLightBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.VerifierParameters;

public class VerifierModule extends ZkModuleImpl implements ZkModuleVerifier {


  private final String lhsAttribute;
  private final String rhsAttribute;
  /**
   * Set to true if this is a less-than Set to false if this is less-equal
   */
  private final boolean strict;
  private final BigIntFactory bif;
  private final EcryptSystemParametersWrapper sp;
  private final BuildingBlockFactory bbFactory;
  private final SafeRSAGroupInVerifierParameters rsaVp;
  private final VerifierParameters vp;
  private final FourSquaresRangeProofBuildingBlock parent;
  private GroupDescription gd;

  private final List<ZkModuleVerifier> children;
  
  /// Number of range sub-proofs with hidden attributes (i.e., actually performed range proofs)
  @VisibleForTest
  static final AtomicInteger hiddenRangeChecks = new AtomicInteger(0);
  /// Number of range proofs that were skipped because both operations when revealed
  @VisibleForTest
  static final AtomicInteger skippedBecauseRevealed = new AtomicInteger(0);
  /// Number of range proofs that were skipped because it was an unnecessary range check
  @VisibleForTest
  static final AtomicInteger skippedBecauseInRange = new AtomicInteger(0);

  public VerifierModule(final FourSquaresRangeProofBuildingBlock parent, final String identifierOfModule,
                        final String lhsAttribute, final String rhsAttribute, final boolean strict, final BigIntFactory bif,
      final EcryptSystemParametersWrapper sp, final VerifierParameters vp, final BuildingBlockFactory bbFactory,
      final GroupFactory gf, final SafeRSAGroupInVerifierParameters rsaVp) throws ConfigurationException {

    super(parent, identifierOfModule);

    this.parent = parent;
    this.lhsAttribute = lhsAttribute;
    this.rhsAttribute = rhsAttribute;
    this.strict = strict;
    this.bif = bif;
    this.sp = sp;
    this.bbFactory = bbFactory;
    this.rsaVp = rsaVp;
    this.vp = vp;
    this.children = new ArrayList<ZkModuleVerifier>();
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    try {
      final URI issuer = new URI(new String(zkVerifier.getDValueAsObject("issuer"), "UTF-8"));
      gd = rsaVp.getGroupDescription(vp, issuer);
    } catch (UnsupportedEncodingException|URISyntaxException e) {
      throw new RuntimeException(e);
    }

    // Create child modules
    final SystemParametersBuildingBlock spBB =
        bbFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    final ZkModuleVerifier spProver =
        spBB.getZkModuleVerifier(identifierOfModule + ":sp", sp.getSystemParameters());
    children.add(spProver);

    final VerifierParametersBuildingBlock vpBB =
        bbFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    final ZkModuleVerifier vpProver =
        vpBB.getZkModuleVerifier(identifierOfModule + ":vp", sp.getSystemParameters(), vp);
    children.add(vpProver);

    // Proof that 0 <= lhs
    final ZkModuleVerifier sub1 =
        new VerifierSubModule(parent, identifierOfModule + ":ineq1",
            RangeProofBuildingBlock.ATT_NAME_ZERO, lhsAttribute, false, true);
    children.add(sub1);

    // Proof that lhs < rhs or lhs <= rhs
    final ZkModuleVerifier sub2 =
        new VerifierSubModule(parent, identifierOfModule + ":ineq2", lhsAttribute, rhsAttribute,
            strict, false);
    children.add(sub2);

    // Proof that rhs <= maxAttributeValue
    final ZkModuleVerifier sub3 =
        new VerifierSubModule(parent, identifierOfModule + ":ineq3", rhsAttribute,
            RangeProofBuildingBlock.ATT_NAME_MAX, false, true);
    children.add(sub3);

    // Zero and maxAtt as constants
    zkVerifier.registerAttribute(RangeProofBuildingBlock.ATT_NAME_ZERO, false);
    zkVerifier.attributeIsRevealed(RangeProofBuildingBlock.ATT_NAME_ZERO);
    zkVerifier.setResidueClass(RangeProofBuildingBlock.ATT_NAME_ZERO, ResidueClass.INTEGER_IN_RANGE);
    zkVerifier.registerAttribute(RangeProofBuildingBlock.ATT_NAME_MAX, false);
    zkVerifier.attributeIsRevealed(RangeProofBuildingBlock.ATT_NAME_MAX);
    zkVerifier.setResidueClass(RangeProofBuildingBlock.ATT_NAME_MAX, ResidueClass.INTEGER_IN_RANGE);

    for (final ZkModuleVerifier zkv : children) {
      zkv.collectAttributesForVerify(zkVerifier);
    }
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    boolean ok = true;
    for (ZkModuleVerifier zkv : children) {
      ok = ok && zkv.verify(zkVerifier);
    }

    // Check zero and max att constants
    zkVerifier.checkValueOfAttribute(RangeProofBuildingBlock.ATT_NAME_ZERO, bif.zero());
    zkVerifier.checkValueOfAttribute(RangeProofBuildingBlock.ATT_NAME_MAX,
        sp.getMaximumAttributeValue(0, bif));

    zkVerifier.checkNValue("group", gd.group.getGroupDescription());
    zkVerifier.checkNValue("base1", gd.S);
    zkVerifier.checkNValue("base2", gd.Z);
    return ok;
  }


  private class VerifierSubModule extends ZkModuleImpl implements ZkModuleVerifier {

    private final String lhsAttribute;
    private final String rhsAttribute;
    
    private final String delta;
    private final List<ZkModuleVerifier> children;
    private final ZkModuleVerifier linearComb;
    private final boolean strict;

    private final String nameC[] = new String[4];
    private final String nameU[] = new String[4];
    private final String nameR[] = new String[4];
    private final String nameCDelta;
    private final String nameDelta;
    private final String nameCDelta2;
    private final String nameU2[] = new String[4];
    private final String nameRAlpha;
    private final boolean isRangeCheck;
    

    public VerifierSubModule(final GeneralBuildingBlock parent, final String identifierOfModule,
                             final String lhsAttribute, final String rhsAttribute, final boolean strict, final boolean isRangeCheck) throws ConfigurationException, ProofException {
      super(parent, identifierOfModule);
      this.lhsAttribute = lhsAttribute;
      this.rhsAttribute = rhsAttribute;
      this.strict = strict;
      this.isRangeCheck = isRangeCheck;
      this.children = new ArrayList<ZkModuleVerifier>();
      final DamgardFujisakiRepresentationBuildingBlock dfbb =
          bbFactory.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);

      // For non-strict (proof that lhs <= rhs): delta = rhs - lhs
      // For strict (proof that lhs < rhs): delta = rhs - lhs - 1
      {
        BigInt constant = bif.zero();
        if (strict) {
          constant = bif.one().negate();
        }
        Term term1 = new Term(lhsAttribute, bif.one().negate());
        Term term2 = new Term(rhsAttribute, bif.one());
        this.delta = identifierOfModule + ":delta";
        final List<Term> terms = Arrays.asList(term1, term2);
        linearComb =
            bbFactory.getBuildingBlockByClass(LinearCombinationLightBuildingBlock.class)
                .getZkModuleVerifier(identifierOfModule + ":delta", this.delta, constant, terms);
      }

      final HiddenOrderGroupElement Sinv = gd.S.invert();
      // C_j = Z^{u_j} * (S^-1)^{r_j} j = 0 .. 3
      for (int i = 0; i < 4; ++i) {
        final BaseForRepresentation baseZ = BaseForRepresentation.managedAttribute(gd.Z);
        final BaseForRepresentation baseS = BaseForRepresentation.randomAttribute(Sinv);
        final ZkModuleVerifier zkp =
            dfbb.getZkModuleVerifier(sp.getSystemParameters(), identifierOfModule + ":T" + i,
                Arrays.asList(baseZ, baseS), null, null, gd.group);
        // .getZkModuleProver(sp.getSystemParameters(), identifierOfModule + ":T" + i, null,
        // Arrays.asList(baseZ, baseS), gd.group, null, null, null, null);
        children.add(zkp);
        nameU[i] = zkp.identifierOfAttribute(0);
        nameR[i] = zkp.identifierOfAttribute(1);
        nameC[i] = zkp.identifierOfCommitment();
      }
      // C_delta = Z^{delta} * S^{r_delta}
      {
        final BaseForRepresentation baseZ = BaseForRepresentation.managedAttribute(gd.Z);
        final BaseForRepresentation baseS = BaseForRepresentation.randomAttribute(gd.S);
        final ZkModuleVerifier zkp =
            dfbb.getZkModuleVerifier(sp.getSystemParameters(), identifierOfModule + ":Td",
                Arrays.asList(baseZ, baseS), null, null, gd.group);
        children.add(zkp);
        nameDelta = zkp.identifierOfAttribute(0);
        nameCDelta = zkp.identifierOfCommitment();
      }
      // C_delta = C_0^{u_0} * C_1^{u_1} * C_2^{u_2} * C_3^{u_3} * S^alpha
      // where alpha = r_SUM{u_j*r_j} + r_delta
      {
        final List<BaseForRepresentation> bases = new ArrayList<BaseForRepresentation>();
        for (int i = 0; i < 4; ++i) {
          bases.add(BaseForRepresentation.managedAttribute(nameC[i]));
        }
        bases.add(BaseForRepresentation.managedAttribute(gd.S));

        final ZkModuleVerifier zkp =
            dfbb.getZkModuleVerifier(sp.getSystemParameters(), identifierOfModule + ":Td2", 
                bases, null, null, gd.group);
        children.add(zkp);
        for (int i = 0; i < 4; ++i) {
          nameU2[i] = zkp.identifierOfAttribute(i);
        }
        nameRAlpha = zkp.identifierOfAttribute(4);
        nameCDelta2 = zkp.identifierOfCommitment();
      }
    }

    @Override
    public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier)
        throws ProofException, ConfigurationException {
      linearComb.collectAttributesForVerify(zkVerifier);
      for(final ZkModuleVerifier zkv: children) {
        zkv.collectAttributesForVerify(zkVerifier);
      }
      
      // Prove equalities between delta, u_1, u_2, u_3, u_4 in all equations
      zkVerifier.attributesAreEqual(delta, nameDelta);
      for (int i = 0; i < 4; ++i) {
        zkVerifier.attributesAreEqual(nameU[i], nameU2[i]);
      }
      // Compute the bit length of RAlpha
      // Recall that RAlpha = SUM_{j=0}^4{u_j*r_j} + r_delta
      // Here we ignore the potential contribution of r_delta since it is so small
      final int bitLengthU = sp.getAttributeLength() / 2;
      final int bitLengthR = gd.group.getRandomIterationcounterLength(sp.getStatisticalInd());
      final int log2_4 = 2; // log_2(4)
      final int RAlphaBitLength = bitLengthU + bitLengthR + log2_4 + 1;
      zkVerifier.registerAttribute(nameRAlpha, false, RAlphaBitLength);
    }

    @Override
    public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
        ProofException {
      if (zkVerifier.isRevealedAttribute(lhsAttribute) && zkVerifier.isRevealedAttribute(rhsAttribute)) {
        // If both operands are revealed, short-circuit the verification
        skippedBecauseRevealed.incrementAndGet();
        final BigInt lhsValue = zkVerifier.getValueOfRevealedAttribute(lhsAttribute);
        final BigInt rhsValue = zkVerifier.getValueOfRevealedAttribute(rhsAttribute);
        if(strict) {
          return lhsValue.compareTo(rhsValue) < 0;
        } else {
          return lhsValue.compareTo(rhsValue) <= 0;
        }
      } else if(isRangeCheck && !zkVerifier.getResidueClass(lhsAttribute).needsRangeCheck()
          && !zkVerifier.getResidueClass(rhsAttribute).needsRangeCheck()) {
        // If this is a range check, and both attributes are integers, skip check
        skippedBecauseInRange.incrementAndGet();
        return true;
      } else {
        hiddenRangeChecks.incrementAndGet();
      }
      
      final HiddenOrderGroupElement cDelta1 =
          zkVerifier.getDValueAsGroupElement(nameCDelta, gd.group);
      final HiddenOrderGroupElement cDelta2 =
          zkVerifier.getDValueAsGroupElement(nameCDelta2, gd.group);
      if (!cDelta1.equals(cDelta2)) {
        throw new ProofException("Problem in range proof; value C_delta is inconsistent");
      }
      
      boolean ok = true;
      ok = ok && linearComb.verify(zkVerifier);
      for(ZkModuleVerifier zkv: children) {
        ok = ok && zkv.verify(zkVerifier);
      }
      return ok;
    }
  }

}
