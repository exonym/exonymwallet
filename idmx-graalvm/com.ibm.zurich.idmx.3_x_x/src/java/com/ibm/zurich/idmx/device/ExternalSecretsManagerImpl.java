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
package com.ibm.zurich.idmx.device;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofCommitment;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofResponse;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofSpecification;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

/**
 * Mock secrets manager that gives the illusion that an unlimited number of external devices are
 * connected to it.
 */
public class ExternalSecretsManagerImpl implements ExternalSecretsManager {

  private final BigIntFactory bigIntFactory;
  private final RandomGeneration randomGeneration;
  private final KeyManager keyManager;
  private final CredentialManager credentialManager;
  private final BuildingBlockFactory bbf;

  private final Map<URI, URI> issuerForNewCredentials;
  private final Map<URI, IssuerParam> fakeIssuerParam;

  @Inject
  public ExternalSecretsManagerImpl(final BigIntFactory bigIntFactory, final RandomGeneration randomGeneration,
                                    final KeyManager keyManager, final BuildingBlockFactory bbf, final CredentialManager cm) {
    this.bigIntFactory = bigIntFactory;
    this.randomGeneration = randomGeneration;
    this.keyManager = keyManager;
    this.credentialManager = cm;
    this.bbf = bbf;
    this.issuerForNewCredentials = new ConcurrentHashMap<URI, URI>();
    this.fakeIssuerParam = new ConcurrentHashMap<URI, ExternalSecretsManagerImpl.IssuerParam>();
  }

  private class CardParam {
    public final BigInt modulus;
    public final BigInt order;
    public final BigInt base;
    public final int hashSizeBytes;
    public final int statisticalZkBytes;
    public final int secretSizeBytes;
    public final BigInt secret;

    CardParam(final Secret s) {
      secret = bigIntFactory.valueOf(s.getSecretKey());
      modulus = bigIntFactory.valueOf(s.getSystemParameters().getPrimeModulus());
      order = bigIntFactory.valueOf(s.getSystemParameters().getSubgroupOrder());
      base = bigIntFactory.valueOf(s.getSystemParameters().getGenerator());
      secretSizeBytes = s.getSystemParameters().getDeviceSecretSizeBytes();
      statisticalZkBytes = s.getSystemParameters().getZkStatisticalHidingSizeBytes();
      hashSizeBytes = s.getSystemParameters().getZkChallengeSizeBytes();
    }
  }
  
  // VisibleForTest
  public void setFakeIssuerParam(final URI issuerUri, final BigInt modulus, final BigInt baseSecret, final @Nullable BigInt baseRandomizer) {
    fakeIssuerParam.put(issuerUri, new IssuerParam(modulus, baseSecret, baseRandomizer));
  }

  private CardParam getCardParam(final String username, final URI secretUri) {
    try {
      return new CardParam(credentialManager.getSecret(username, secretUri));
    } catch (final CredentialManagerException e) {
      throw new RuntimeException(e);
    }
  }

  public static Secret generateSecret(final SystemParameters sp, final BigInteger secretKey, final URI secretUid) {
    try {
      final EcryptSystemParametersWrapper spw = new EcryptSystemParametersWrapper(sp);

      final Secret s = new ObjectFactory().createSecret();
      s.setSecretKey(secretKey);
      s.setSecretDescription(new ObjectFactory().createSecretDescription());
      s.getSecretDescription().setDeviceBoundSecret(false);
      s.getSecretDescription().setSecretUID(secretUid);
      s.setSystemParameters(new ObjectFactory().createSmartcardSystemParameters());

      s.getSystemParameters().setDeviceSecretSizeBytes(256 / 8);
      s.getSystemParameters().setGenerator(spw.getDHGenerator1().getValue());
      s.getSystemParameters().setPrimeModulus(spw.getDHModulus().getValue());
      s.getSystemParameters().setSignatureNonceLengthBytes(128 / 8);
      s.getSystemParameters().setSubgroupOrder(spw.getDHSubgroupOrder().getValue());
      s.getSystemParameters().setZkChallengeSizeBytes(256 / 8);
      s.getSystemParameters().setZkNonceOpeningSizeBytes(256 / 8);
      s.getSystemParameters().setZkNonceSizeBytes(256 / 8);
      s.getSystemParameters().setZkStatisticalHidingSizeBytes(80 / 8);

      return s;
    } catch (final ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public static Secret generateSecret(final KeyManager km, final BigInteger secretKey, final URI secretUid) {
    try {
      return generateSecret(km.getSystemParameters(), secretKey, secretUid);
    } catch (KeyManagerException e) {
      throw new RuntimeException(e);
    }
  }

  private class IssuerParam {
    private final BigInt modulus;
    private final BigInt baseSecret;
    private final BigInt baseRandomizer;

    public IssuerParam(final BigInt modulus, final BigInt baseSecret, final BigInt baseRandomizer) {
      this.modulus = modulus;
      this.baseSecret = baseSecret;
      this.baseRandomizer = baseRandomizer;
    }
  }

  private IssuerParam getIssuerParam(final URI issuerUri) {
    try {
      if(fakeIssuerParam.containsKey(issuerUri)) {
        return fakeIssuerParam.get(issuerUri);
      }
      final IssuerParameters ip = keyManager.getIssuerParameters(issuerUri);
      final IssuerParametersFacade ipf = new IssuerParametersFacade(ip);
      final SignatureBuildingBlock sigBB = bbf.getSignatureBuildingBlockById(ipf.getBuildingBlockId());
      if (sigBB.getClass() == ClSignatureBuildingBlock.class) {
        ClPublicKeyWrapper pkw =
            new ClPublicKeyWrapper(new IssuerParametersFacade(ip).getPublicKey());

        return new IssuerParam(pkw.getModulus(), pkw.getRd(), pkw.getS());
      } else if (sigBB.getClass() == BrandsSignatureBuildingBlock.class) {
        final BrandsPublicKeyWrapper pkw =
            new BrandsPublicKeyWrapper(new IssuerParametersFacade(ip).getPublicKey());
        final SystemParameters sp = keyManager.getSystemParameters();
        final EcryptSystemParametersWrapper spw = new EcryptSystemParametersWrapper(sp);

        return new IssuerParam(spw.getDHModulus(), pkw.getGD(), null);
      } else {
        throw new RuntimeException("Unknown issuer parameters algorithm: "
            + ipf.getBuildingBlockId());
      }
    } catch (KeyManagerException|ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  private IssuerParam getIssuerParamForCredential(final String username, final URI credentialUri) {
    try {
      final URI issuerUri;
      final CredentialDescription cd = credentialManager.getCredentialDescription(username, credentialUri);
      if (cd == null) {
        issuerUri = issuerForNewCredentials.get(credentialUri);
      } else {
        issuerUri = cd.getIssuerParametersUID();
      }
      if (issuerUri == null) {
        return null;
      }
      return getIssuerParam(issuerUri);
    } catch (final CredentialManagerException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void allocateCredential(final String username, final URI deviceUid, final URI newCredentialUri, final URI issuerUri,
                                              final boolean overwrite) {
    issuerForNewCredentials.put(newCredentialUri, issuerUri);
  }


  @Override
  public synchronized void associateIssuer(final String username, final URI deviceUid, final URI newCredentialUri, final URI issuerUri) {
    issuerForNewCredentials.put(newCredentialUri, issuerUri);
  }

  @Override
  public synchronized boolean isDeviceLoaded(final String username, final URI deviceUid) {
    try {
      return credentialManager.getSecret(username, deviceUid) != null;
    } catch (final CredentialManagerException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized boolean doesCredentialExists(final String username, final URI deviceUid, final URI credentialUri) {
    try {
      final CredentialDescription cd = credentialManager.getCredentialDescription(username, credentialUri);
      if (cd == null) {
        return false;
      }
      return cd.getSecretReference().equals(deviceUid);
    } catch (final CredentialManagerException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized BigInteger getPublicKeyBase(final String username, final URI deviceUid) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      return null;
    } else {
      return cp.base.getValue();
    }
  }

  @Override
  public synchronized BigInteger getPseudonymModulus(final String username, final URI deviceUid) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      return null;
    } else {
      return cp.modulus.getValue();
    }
  }

  @Override
  public synchronized BigInteger getPseudonymSubgroupOrder(final String username, final URI deviceUid) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      return null;
    } else {
      return cp.order.getValue();
    }
  }

  @Override
  public synchronized BigInteger getBaseForDeviceSecret(final String username, final URI deviceUid, final URI credentialUri) {
    final IssuerParam ip = getIssuerParamForCredential(username, credentialUri);
    if (ip == null) {
      return null;
    } else {
      return ip.baseSecret.getValue();
    }
  }

  @Override
  public synchronized @Nullable
  BigInteger getBaseForCredentialSecret(final String username, final URI deviceUid, final URI credentialUri) {
    final IssuerParam ip = getIssuerParamForCredential(username, credentialUri);
    if (ip == null || ip.baseRandomizer == null) {
      return null;
    } else {
      return ip.baseRandomizer.getValue();
    }
  }

  @Override
  public synchronized BigInteger getModulus(final String username, final URI deviceUid, final URI credentialUri) {
    final IssuerParam ip = getIssuerParamForCredential(username, credentialUri);
    if (ip == null) {
      return null;
    } else {
      return ip.modulus.getValue();
    }
  }

  @Override
  public synchronized int getChallengeSizeBytes(final String username, final URI deviceUid) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      throw new RuntimeException("Card not loaded");
    } else {
      return cp.hashSizeBytes;
    }
  }

  @Override
  public synchronized int getRandomizerSizeBytes(final String username, final URI deviceUid) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      throw new RuntimeException("Card not loaded");
    } else {
      return cp.hashSizeBytes + cp.secretSizeBytes + cp.statisticalZkBytes;
    }
  }

  @Override
  public synchronized int getAttributeSizeBytes(final String username, final URI deviceUid) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      throw new RuntimeException("Card not loaded");
    } else {
      return cp.secretSizeBytes;
    }
  }

  @Override
  public synchronized BigInteger getDevicePublicKey(final String username, final URI deviceUid) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      return null;
    } else {
      return cp.base.modPow(cp.secret, cp.modulus).getValue();
    }
  }

  @Override
  public synchronized BigInteger getCredentialPublicKey(final String username, final URI deviceUid, final URI credentialUri) {
    final IssuerParam ip = getIssuerParamForCredential(username, credentialUri);
    final CardParam cp = getCardParam(username, deviceUid);
    if (ip == null || cp == null) {
      return null;
    }

    BigInt a = ip.baseSecret.modPow(cp.secret, ip.modulus);
    if (ip.baseRandomizer != null) {
      a =
          a.multiply(ip.baseRandomizer.modPow(secret(cp.secret, credentialUri), ip.modulus)).mod(
              ip.modulus);
    }

    return a.getValue();
  }

  @Override
  public synchronized BigInteger getScopeExclusivePseudonym(final String username, final URI deviceUid, final URI scope) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      return null;
    } else {
      final BigInt seBase =
          ExternalSecretsHelperImpl.getBaseForScopeExclusivePseudonym(bigIntFactory, scope,
              cp.modulus, cp.order);
      return seBase.modPow(cp.secret, cp.modulus).getValue();
    }
  }

  @Override
  public synchronized BigInteger getBaseForScopeExclusivePseudonym(final String username, final URI scope, final BigInteger modulus,
                                                                   final BigInteger subgroupOrder) {
    return ExternalSecretsHelperImpl.getBaseForScopeExclusivePseudonym(bigIntFactory, scope,
        bigIntFactory.valueOf(modulus), bigIntFactory.valueOf(subgroupOrder)).getValue();
  }

  @Override
  public synchronized BigInteger getBaseForScopeExclusivePseudonym(final String username, final URI deviceUid, final URI scope) {
    final CardParam cp = getCardParam(username, deviceUid);
    if (cp == null) {
      return null;
    } else {
      final BigInt seBase =
          ExternalSecretsHelperImpl.getBaseForScopeExclusivePseudonym(bigIntFactory, scope,
              cp.modulus, cp.order);
      return seBase.getValue();
    }
  }

  @Override
  public synchronized DeviceProofCommitment getPresentationCommitment(final DeviceProofSpecification spec) {
    return ((DeviceProofSpecificationImpl) spec).getCommitment();
  }

  @Override
  public synchronized DeviceProofResponse getPresentationResponse(final DeviceProofCommitment com,
                                                                  final BigInteger challenge) {
    return ((DeviceProofSpecificationImpl.DeviceProofCommitmentImpl) com).getResponse(challenge);
  }

  @Override
  public synchronized DeviceProofSpecification newProofSpec(final String username) {
    return new DeviceProofSpecificationImpl(username);
  }

  private BigInt secret(final BigInt secret, final URI toHash) {
    try {
      final String HASH_ALGORITHM = "SHA-512";
      final String ENCODING = "UTF-8";
      final byte[] secretBytes = secret.toByteArrayUnsigned();
      final byte[] encodedToHash = toHash.toString().getBytes(ENCODING);
      final byte[] secretLengthBytes = ByteBuffer.allocate(4).putInt(secretBytes.length).array();
      final byte[] toHashLengthBytes = ByteBuffer.allocate(4).putInt(encodedToHash.length).array();
      final MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
      md.update(secretLengthBytes);
      md.update(secretBytes);
      md.update(toHashLengthBytes);
      md.update(encodedToHash);
      final byte[] hashAsBytes = md.digest();
      return bigIntFactory.unsignedValueOf(hashAsBytes);
    } catch (UnsupportedEncodingException|NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 
   */
  private class DeviceProofSpecificationImpl implements DeviceProofSpecification {

    private final Set<CredentialUriOnDevice> credentialProofs;
    private final Set<CredentialUriOnDevice> sePseudonymProofs;
    private final Set<URI> publicKeyProofs;
    private final Set<URI> devices;
    private final String username;

    public DeviceProofSpecificationImpl(final String username) {
      credentialProofs = new HashSet<CredentialUriOnDevice>();
      sePseudonymProofs = new HashSet<CredentialUriOnDevice>();
      publicKeyProofs = new HashSet<URI>();
      devices = new HashSet<URI>();
      this.username = username;
    }

    @Override
    public void addCredentialProof(final URI deviceId, final URI credentialUri) {
      credentialProofs.add(new CredentialUriOnDevice(deviceId, credentialUri));
      devices.add(deviceId);
    }

    @Override
    public void addScopeExclusivePseudonymProof(final URI deviceId, final URI scope) {
      sePseudonymProofs.add(new CredentialUriOnDevice(deviceId, scope));
      devices.add(deviceId);
    }

    @Override
    public void addPublicKeyProof(final URI deviceId) {
      publicKeyProofs.add(deviceId);
      devices.add(deviceId);
    }

    public DeviceProofCommitment getCommitment() {
      return new DeviceProofCommitmentImpl();
    }

    /**
     * 
     */
    private class DeviceProofCommitmentImpl implements DeviceProofCommitment {

      private final Map<CredentialUriOnDevice, BigInt> rValuesForCredentials;
      private final Map<URI, BigInt> rValuesForDevice;

      public DeviceProofCommitmentImpl() {
        rValuesForCredentials = new HashMap<CredentialUriOnDevice, BigInt>();
        rValuesForDevice = new HashMap<URI, BigInt>();
        generateRValues();
      }

      private void generateRValues() {
        for (final URI device : devices) {
          final BigInt rValue = randomGeneration.generateRandomNumber(getRandomizerSizeBytes(username, device) * 8);
          rValuesForDevice.put(device, rValue);
        }
        for (final CredentialUriOnDevice credential : credentialProofs) {
          final IssuerParam ip = getIssuerParamForCredential(username, credential.getCredentialUri());
          if (ip == null) {
            throw new NullPointerException("Unknown issuer parameters of credential "
                + credential.getCredentialUri() + " on device " + credential.getDeviceUid() + ".");
          }
          if (ip.baseRandomizer != null) {
            final BigInt rValue =
                randomGeneration
                    .generateRandomNumber(getRandomizerSizeBytes(username, credential.getDeviceUid()) * 8);
            rValuesForCredentials.put(credential, rValue);
          }
        }
      }

      @Override
      public BigInteger getCommitmentForCredential(final URI deviceUid, final URI credentialUri) {
        final CredentialUriOnDevice credential = new CredentialUriOnDevice(deviceUid, credentialUri);
        final IssuerParam ip = getIssuerParamForCredential(username, credential.getCredentialUri());
        final BigInt xr = rValuesForDevice.get(deviceUid);
        BigInt a = ip.baseSecret.modPow(xr, ip.modulus);
        if (ip.baseRandomizer != null) {
          final BigInt xv = rValuesForCredentials.get(credential);
          a = a.multiply(ip.baseRandomizer.modPow(xv, ip.modulus)).mod(ip.modulus);
        }
        return a.getValue();
      }

      @Override
      public BigInteger getCommitmentForScopeExclusivePseudonym(final URI deviceUid, final URI scope) {
        final CardParam cp = getCardParam(username, deviceUid);
        final BigInt xr = rValuesForDevice.get(deviceUid);
        final BigInt seBase =
            ExternalSecretsHelperImpl.getBaseForScopeExclusivePseudonym(bigIntFactory, scope,
                cp.modulus, cp.order);
        return seBase.modPow(xr, cp.modulus).getValue();

      }

      @Override
      public BigInteger getCommitmentForPublicKey(final URI deviceUid) {
        final CardParam cp = getCardParam(username, deviceUid);
        final BigInt xr = rValuesForDevice.get(deviceUid);
        return cp.base.modPow(xr, cp.modulus).getValue();
      }

      public DeviceProofResponse getResponse(final BigInteger challenge) {
        return new DeviceProofResponseImpl(challenge);
      }

      /**
       * 
       */
      private class DeviceProofResponseImpl implements DeviceProofResponse {

        private final BigInt challenge;
        //TODO(ksa) BigInteger here?
        public DeviceProofResponseImpl(final BigInteger challenge) {
          this.challenge = bigIntFactory.valueOf(challenge);
        }

        @Override
        public BigInteger getResponseForCredentialSecretKey(final URI deviceUid, final URI credentialUri) {
          final CardParam cp = getCardParam(username, deviceUid);
          final BigInt x = secret(cp.secret, credentialUri);
          final BigInt r = rValuesForCredentials.get(new CredentialUriOnDevice(deviceUid, credentialUri));
          return r.subtract(challenge.multiply(x)).getValue();
        }

        @Override
        public BigInteger getResponseForDeviceSecretKey(final URI deviceUid) {
          final CardParam cp = getCardParam(username, deviceUid);
          final BigInt x = cp.secret;
          final BigInt r = rValuesForDevice.get(deviceUid);
          return r.subtract(challenge.multiply(x)).getValue();
        }
      }
    }
  }
}
