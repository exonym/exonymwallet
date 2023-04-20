/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.wallet;

import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.xml.CredentialSpecification;
import io.exonym.lib.standard.CryptoUtils;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import javax.crypto.Cipher;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class OwnerAPITest {


    private final static Logger logger = Logger.getLogger(OwnerAPITest.class.getName());
    @Test
    public void test() {
        try {
            URI uid = URI.create("uid:exonym:credential:c");
            String cred = GraalVMProbeMain.newCredentialSpec(uid.toString());
            logger.fine(cred);
            CredentialSpecification c = (CredentialSpecification) JaxbHelperClass.deserialize(cred).getValue();
            URI compare = c.getSpecificationUID();
            assertEquals(uid, compare);

        } catch (SerializationException e) {
            assert false;

        }
    }

    @Test
    public void readUrl() {
        String xml = GraalVMProbeMain.readUrl("https://spectra.plus/lambda.xml");
        logger.fine(xml);


    }

    @Test
    public void openSystemParams() {

    }

    @Test
    public void encrypt() {
        try {
            String salt16_B64 = "MQnANpovcMvrYUishzZe9Q==";
            byte[] salt = Base64.decodeBase64(salt16_B64);
            Cipher enc = CryptoUtils.generatePasswordCipher(Cipher.ENCRYPT_MODE, "password", salt);
            byte[] testEnc = "test".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = enc.doFinal(testEnc);
            logger.fine(new String(encrypted));
            Cipher dec = CryptoUtils.generatePasswordCipher(Cipher.DECRYPT_MODE, "passwor", salt);

            try {
                dec.doFinal(encrypted);
                assert false;

            } catch (Exception e) {
                logger.fine("Expected " + e.getMessage());
                assert true;

            }
            dec = CryptoUtils.generatePasswordCipher(Cipher.DECRYPT_MODE, "password", salt);
            byte[] result = dec.doFinal(encrypted);
            String r = new String(result);
            Assert.assertEquals("test",r);

        } catch (Exception e) {
            logger.throwing("OwnerAPITest.class", "encrypt()", e);

        }
    }
}