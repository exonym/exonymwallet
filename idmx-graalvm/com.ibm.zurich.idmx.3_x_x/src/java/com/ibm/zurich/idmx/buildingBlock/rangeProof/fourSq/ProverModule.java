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
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.VerifierParameters;

public class ProverModule extends ZkModuleImpl implements ZkModuleProver {

  private final BigIntFactory bif;
  private final EcryptSystemParametersWrapper sp;
  private final BuildingBlockFactory bbFactory;

  private final GroupDescription gd;

  private final List<ZkModuleProver> children;
  
  /// Number of range sub-proofs with hidden attributes (i.e., actually performed range proofs)
  @VisibleForTest
  static final AtomicInteger hiddenRangeChecks = new AtomicInteger(0);
  /// Number of range proofs that were skipped because both operations when revealed
  @VisibleForTest
  static final AtomicInteger skippedBecauseRevealed = new AtomicInteger(0);
  /// Number of range proofs that were skipped because it was an unnecessary range check
  @VisibleForTest
  static final AtomicInteger skippedBecauseInRange = new AtomicInteger(0);
  

  public ProverModule(final FourSquaresRangeProofBuildingBlock parent, final String identifierOfModule,
                      final String lhsAttribute, final String rhsAttribute, final boolean strict, final BigIntFactory bif,
      final EcryptSystemParametersWrapper sp, final VerifierParameters vp, final BuildingBlockFactory bbFactory,
      final GroupFactory gf, final SafeRSAGroupInVerifierParameters rsaVp) throws ConfigurationException {

    super(parent, identifierOfModule);

    this.bif = bif;
    this.sp = sp;
    this.bbFactory = bbFactory;
    this.children = new ArrayList<ZkModuleProver>();

    // Get safe RSA group
    this.gd = rsaVp.getGroupDescription(vp);

    // Create child modules
    SystemParametersBuildingBlock spBB =
        bbFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    ZkModuleProver spProver =
        spBB.getZkModuleProver(identifierOfModule + ":sp", sp.getSystemParameters());
    children.add(spProver);

    VerifierParametersBuildingBlock vpBB =
        bbFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    ZkModuleProver vpProver =
        vpBB.getZkModuleProver(identifierOfModule + ":vp", sp.getSystemParameters(), vp);
    children.add(vpProver);

    // Proof that 0 <= lhs
    ZkModuleProver sub1 =
        new ProverSubModule(parent, identifierOfModule + ":ineq1",
            RangeProofBuildingBlock.ATT_NAME_ZERO, lhsAttribute, false, true);
    children.add(sub1);

    // Proof that lhs < rhs or lhs <= rhs
    ZkModuleProver sub2 =
        new ProverSubModule(parent, identifierOfModule + ":ineq2", lhsAttribute, rhsAttribute,
            strict, false);
    children.add(sub2);

    // Proof that rhs <= maxAttributeValue
    ZkModuleProver sub3 =
        new ProverSubModule(parent, identifierOfModule + ":ineq3", rhsAttribute,
            RangeProofBuildingBlock.ATT_NAME_MAX, false, true);
    children.add(sub3);
  }


  @Override
  public void initializeModule(ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    // Zero and maxAtt as constants
    zkBuilder.registerAttribute(RangeProofBuildingBlock.ATT_NAME_ZERO, false);
    zkBuilder.attributeIsRevealed(RangeProofBuildingBlock.ATT_NAME_ZERO);
    zkBuilder.setValueOfAttribute(RangeProofBuildingBlock.ATT_NAME_ZERO, bif.zero(), ResidueClass.INTEGER_IN_RANGE);
    zkBuilder.registerAttribute(RangeProofBuildingBlock.ATT_NAME_MAX, false);
    zkBuilder.attributeIsRevealed(RangeProofBuildingBlock.ATT_NAME_MAX);
    zkBuilder.setValueOfAttribute(RangeProofBuildingBlock.ATT_NAME_MAX,
        sp.getMaximumAttributeValue(0, bif), ResidueClass.INTEGER_IN_RANGE);

    for (final ZkModuleProver zkp : children) {
      zkp.initializeModule(zkBuilder);
    }
  }

  @Override
  public void collectAttributesForProof(ZkProofStateCollect zkBuilder)
      throws ConfigurationException {
    for (final ZkModuleProver zkp : children) {
      zkp.collectAttributesForProof(zkBuilder);
    }
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    // Write chosen issuer
    try {
      zkBuilder.addDValue("issuer", gd.issuerUri.toString().getBytes("UTF-8"));
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    zkBuilder.addNValue("group", gd.group.getGroupDescription());
    zkBuilder.addNValue("base1", gd.S);
    zkBuilder.addNValue("base2", gd.Z);

    for (final ZkModuleProver zkp : children) {
      zkp.firstRound(zkBuilder);
    }
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver zkp : children) {
      zkp.secondRound(zkBuilder);
    }
  }



  private class ProverSubModule extends ZkModuleImpl implements ZkModuleProver {

    private final String delta;
    private final List<ZkModuleProverCommitment<HiddenOrderGroupElement>> children;
    private final ZkModuleProver linearComb;

    private final String nameC[] = new String[4];
    private final String nameU[] = new String[4];
    private final String nameR[] = new String[4];
    private final String nameCDelta;
    private final String nameDelta;
    private final String nameRDelta;
    private final String nameCDelta2;
    private final String nameU2[] = new String[4];
    private final String nameRAlpha;
    
    private final String lhsAttribute;
    private final String rhsAttribute;
    private final boolean isRangeCheck;
    
    private boolean skipProof = false;

    public ProverSubModule(final GeneralBuildingBlock parent, final String identifierOfModule,
                           final String lhsAttribute, final String rhsAttribute, final boolean strict, final boolean isRangeCheck) throws ConfigurationException {
      super(parent, identifierOfModule);
      this.lhsAttribute = lhsAttribute;
      this.rhsAttribute = rhsAttribute;
      this.isRangeCheck = isRangeCheck;
      this.children = new ArrayList<ZkModuleProverCommitment<HiddenOrderGroupElement>>();
      final DamgardFujisakiRepresentationBuildingBlock dfbb =
          bbFactory.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);

      // For non-strict (proof that lhs <= rhs): delta = rhs - lhs
      // For strict (proof that lhs < rhs): delta = rhs - lhs - 1
      {
        BigInt constant = bif.zero();
        if (strict) {
          constant = bif.one().negate();
        }
        final Term term1 = new Term(lhsAttribute, bif.one().negate());
        final Term term2 = new Term(rhsAttribute, bif.one());
        this.delta = identifierOfModule + ":delta";
        final List<Term> terms = Arrays.asList(term1, term2);
        linearComb =
            bbFactory.getBuildingBlockByClass(LinearCombinationLightBuildingBlock.class)
                .getZkModuleProver(identifierOfModule + ":delta", this.delta, constant, terms);
      }

      HiddenOrderGroupElement Sinv = gd.S.invert();
      // C_j = Z^{u_j} * (S^-1)^{r_j} j = 0 .. 3
      for (int i = 0; i < 4; ++i) {
        final BaseForRepresentation baseZ = BaseForRepresentation.managedAttribute(gd.Z);
        final BaseForRepresentation baseS = BaseForRepresentation.randomAttribute(Sinv);
        final ZkModuleProverCommitment<HiddenOrderGroupElement> zkp =
            dfbb.getZkModuleProver(sp.getSystemParameters(), identifierOfModule + ":T" + i, null,
                Arrays.asList(baseZ, baseS), gd.group, null, null, null, null);
        children.add(zkp);
        nameU[i] = zkp.identifierOfAttribute(0);
        nameR[i] = zkp.identifierOfAttribute(1);
        nameC[i] = zkp.identifierOfCommitment();
      }
      // C_delta = Z^{delta} * S^{r_delta}
      {
        final BaseForRepresentation baseZ = BaseForRepresentation.managedAttribute(gd.Z);
        final BaseForRepresentation baseS = BaseForRepresentation.randomAttribute(gd.S);
        final ZkModuleProverCommitment<HiddenOrderGroupElement> zkp =
            dfbb.getZkModuleProver(sp.getSystemParameters(), identifierOfModule + ":Td", null,
                Arrays.asList(baseZ, baseS), gd.group, null, null, null, null);
        children.add(zkp);
        nameDelta = zkp.identifierOfAttribute(0);
        nameRDelta = zkp.identifierOfAttribute(1);
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

        final ZkModuleProverCommitment<HiddenOrderGroupElement> zkp =
            dfbb.getZkModuleProver(sp.getSystemParameters(), identifierOfModule + ":Td2", null,
                bases, gd.group, null, null, null, null);
        children.add(zkp);
        for (int i = 0; i < 4; ++i) {
          nameU2[i] = zkp.identifierOfAttribute(i);
        }
        nameRAlpha = zkp.identifierOfAttribute(4);
        nameCDelta2 = zkp.identifierOfCommitment();
      }
    }

    @Override
    public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
      linearComb.initializeModule(zkBuilder);
      for (final ZkModuleProver zkp : children) {
        zkp.initializeModule(zkBuilder);
      }
      // Inform the proof engine that we need to know the value of delta before we can
      // provide the value of u_0 ... u_3
      zkBuilder.requiresAttributeValue(delta);
      for (int i = 0; i < 4; ++i) {
        zkBuilder.providesAttribute(nameU[i]);
      }
      // Prove equalities between delta, u_1, u_2, u_3, u_4 in all equations
      zkBuilder.attributesAreEqual(delta, nameDelta);
      for (int i = 0; i < 4; ++i) {
        zkBuilder.attributesAreEqual(nameU[i], nameU2[i]);
      }
      // Compute the bit length of RAlpha
      // Recall that RAlpha = SUM_{j=0}^4{u_j*r_j} + r_delta
      // Here we ignore the potential contribution of r_delta since it is so small
      final int bitLengthU = sp.getAttributeLength() / 2;
      final int bitLengthR = gd.group.getRandomIterationcounterLength(sp.getStatisticalInd());
      final int log2_4 = 2; // log_2(4)
      final int RAlphaBitLength = bitLengthU + bitLengthR + log2_4 + 1;
      zkBuilder.registerAttribute(nameRAlpha, false, RAlphaBitLength);

      // We compute the value of RAlpha later
      zkBuilder.providesAttribute(nameRAlpha);
    }

    @Override
    public void collectAttributesForProof(final ZkProofStateCollect zkBuilder)
        throws ConfigurationException {
      linearComb.collectAttributesForProof(zkBuilder);


      final BigInt deltaValue = zkBuilder.getValueOfAttribute(delta);
      if (deltaValue.compareTo(bif.zero()) < 0) {
        throw new RuntimeException("Cannot do range proof: delta is negative: " + deltaValue);
      }
      
      if (zkBuilder.isRevealedAttribute(lhsAttribute) && zkBuilder.isRevealedAttribute(rhsAttribute)) {
        // If both operands are revealed, skip proof
        skippedBecauseRevealed.incrementAndGet();
        skipProof = true;
      } else if(isRangeCheck && !zkBuilder.getResidueClass(lhsAttribute).needsRangeCheck()
          && !zkBuilder.getResidueClass(rhsAttribute).needsRangeCheck()) {
        // If this is a range check and both attributes are integers, skip proof
        skippedBecauseInRange.incrementAndGet();
        skipProof = true;
      }
      
      if(skipProof) {
        for (int i = 0; i < 4; ++i) {
          zkBuilder.setValueOfAttribute(nameU[i], bif.zero(), null);
        }
        zkBuilder.setValueOfAttribute(nameRAlpha, bif.zero(), null);
      } else {
        hiddenRangeChecks.incrementAndGet();
        // Do the four square decomposition on delta:
        RabinShallitDecomposition decompRS = new RabinShallitDecomposition(bif);
        final BigInt decomp[] =
            decompRS.decomposeInteger(deltaValue,
                sp.getPrimeProbability());
        for (int i = 0; i < 4; ++i) {
          zkBuilder.setValueOfAttribute(nameU[i], decomp[i], null);
        }
  
        // Run collect phase for C0 ... C3 and CDelta
        for (int i = 0; i < 5; ++i) {
          children.get(i).collectAttributesForProof(zkBuilder);
        }
  
        // Compute RAlpha = r_delta + SUM_{j=0}^4{u_j*r_j}
        BigInt rAlpha = zkBuilder.getValueOfAttribute(nameRDelta);
        for (int i = 0; i < 4; ++i) {
          BigInt uj = zkBuilder.getValueOfAttribute(nameU[i]);
          BigInt rj = zkBuilder.getValueOfAttribute(nameR[i]);
          rAlpha = rAlpha.add(uj.multiply(rj));
        }
        zkBuilder.setValueOfAttribute(nameRAlpha, rAlpha, null);
  
        // Run collect for the last module
        children.get(5).collectAttributesForProof(zkBuilder);
      }
    }

    @Override
    public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
        ProofException {
      if(skipProof) {
        return;
      }
      
      linearComb.firstRound(zkBuilder);
      for (ZkModuleProver zkp : children) {
        zkp.firstRound(zkBuilder);
      }
    }

    @Override
    public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
      if(skipProof) {
        return;
      }
      
      linearComb.secondRound(zkBuilder);
      for (final ZkModuleProver zkp : children) {
        zkp.secondRound(zkBuilder);
      }
      final HiddenOrderGroupElement cDelta1 =
          (HiddenOrderGroupElement) zkBuilder.getDValueAsGroupElement(nameCDelta);
      final HiddenOrderGroupElement cDelta2 =
          (HiddenOrderGroupElement) zkBuilder.getDValueAsGroupElement(nameCDelta2);
      if (!cDelta1.equals(cDelta2)) {
        throw new RuntimeException("Problem in range proof; value C_delta is inconsistent");
      }
    }
  }
}
