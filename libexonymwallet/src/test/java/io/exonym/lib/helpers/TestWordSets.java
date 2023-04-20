package io.exonym.lib.helpers;

import io.exonym.lib.wallet.ExonymOwner;
import io.exonym.lib.wallet.RecoveryPhrase;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.standard.WhiteList;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class TestWordSets {

    Logger logger = Logger.getLogger(TestWordSets.class.getName());

    @Test
    public void testLengthAndCharacterOfAllWords() {
        for (char alpha='a'; alpha<='z'; alpha++){
            for (int length=3;length<13; length++){
                try {
                    String[] set = WordSets.openWordSet(length, alpha);
                    assert(set.length==5);
                    for (String word : set){
                        int l = word.length();
                        int c = word.toCharArray()[0];
                        assert (l==length);
                        assert (c==alpha);

                    }
                } catch (Exception e) {
                    if (!(alpha=='z' && length==12)){
                        System.out.println("Failed to find " + alpha + " " + length);
                    }
                }
            }
        }
    }

    @Test
    public void testUsingPasswordHash() {
        try {
            String password = "34wdvdbf8e8r883427573";// CryptoUtils.tempPassword();
            String hash = CryptoUtils.computeSha256HashAsHex(password);
            byte[] pwd = ExonymOwner.toUsablePassStoreInitByteArray(hash);
            PassStore passStore = new PassStore(pwd);

            PassStore plain = new PassStore(password, false);


            passStore.decipher(plain.encrypt("a".getBytes(StandardCharsets.UTF_8)));


        } catch (Exception e) {
            System.out.println(ExceptionUtils.getStackTrace(e));
            assert false;
        }
    }

    @Test
    public void createUsableBackupPhrase() {
        try {
            String t = CryptoUtils.tempPassword();
            byte[] h = ExonymOwner.toUnsignedByteArray(PassStore.initNew(t));
            RecoveryPhrase phrase = new RecoveryPhrase(h);
            String[] wv = phrase.getWordVector();
            String r = JaxbHelper.serializeToJson(wv, String[].class);
            System.out.println("PASSWORD=" +t+" RECOVERY=" + r);
            RecoveryPhrase recovery = new RecoveryPhrase(wv);
            PassStore original = new PassStore(t, false);
            PassStore recovered = new PassStore(recovery.getByteVector());
            String test = "test";
            byte[] enc = original.encrypt(test.getBytes(StandardCharsets.UTF_8));
            byte[] dec = recovered.decipher(enc);
            assert (new String(dec, StandardCharsets.UTF_8)).equals(test);

        } catch (Exception e) {
            logger.severe("Failure " + e);
            assert false;

        }
    }

    // TODO Move to whitelist tests in core
    @Test
    public void testHex() {
        assert (WhiteList.isHex(CryptoUtils.computeSha256HashAsHex("asda")));
        assert (WhiteList.isHex("0xaf00"));
        assert (WhiteList.isHex("0XAF00"));
        assert (WhiteList.isHex("AF00"));
        assert (!WhiteList.isHex("AFG0"));
    }

    @Test
    public void convertAllBytesToWords() {
        try {
            byte[] b = new byte[256];
            for (int i=-128; i<128; i++) {
                b[i+128] = (byte)i;

            }
            RecoveryPhrase in = new RecoveryPhrase(b);
            String[] words = in.getWordVector();
            RecoveryPhrase out = new RecoveryPhrase(words);
            byte[] recovered = out.getByteVector();

            int i = 0;
            for (String w : words){
                // System.out.println("in " + b[i] + " " +  w + " i=" + i + " out " + recovered[i]);
                assert (b[i]==recovered[i]);
                assert (w!=null);
                i++;

            }
        } catch (Exception e) {
            logger.severe("Failure " + e);
            assert false;

        }
    }

    public static void readFile() throws Exception {
        String root = "stack";
        String str = "";
        try (BufferedReader br = new BufferedReader(new FileReader("resource/words.txt"));){
            String line = null;

            int length = 0;
            char letter = ' ';
            String stackName = "";
            while ((line=br.readLine())!=null){
                if (line.length()>1){
                    if (line.length() != length || letter != line.toCharArray()[0]) {
                        stackName += "\";";
                        System.out.println(stackName);
                        letter = line.toCharArray()[0];
                        length = line.length();
                        stackName = "private static String stack_" + length + "_" + letter + " = \"" + line;


                    } else {
                        stackName = stackName + " " + line;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

}
