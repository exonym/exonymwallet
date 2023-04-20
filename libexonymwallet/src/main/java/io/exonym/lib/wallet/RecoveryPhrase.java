package io.exonym.lib.wallet;

import io.exonym.lib.helpers.WordSets;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;

public class RecoveryPhrase {

    private final String[] wordVector;
    private final byte[] byteVector;
    public static final int LOFA = 26;

    public RecoveryPhrase(byte[] bytes) throws Exception {
        if (bytes==null){
            throw new NullPointerException();
        }
        this.wordVector = new String[bytes.length];
        this.byteVector = bytes;
        wordAssemble(bytes);

    }

    private void wordAssemble(byte[] bytes) throws Exception {
        int i = 0;
        for (byte b : bytes){
            int u8 = b + 128;
            int length = 3 + u8 / LOFA;
            int start = u8 % LOFA;
            char letter = letterOfPosition(start);
            wordVector[i] = WordSets.findAppropriateWord(length, letter);
            i++;

        }
    }

    public RecoveryPhrase(String[] wordVector) throws Exception {
        if (wordVector==null){
            throw new NullPointerException();
        }
        this.wordVector = wordVector;
        this.byteVector = new byte[wordVector.length];
        byteAssemble(wordVector);

    }

    private void byteAssemble(String[] wordVector) throws Exception {
        int i = 0;
        for (String word : wordVector){
            if (word==null || word.length() < 3){
                throw new UxException(ErrorMessages.INCORRECT_PARAMETERS,
                        "A word was null or too short at position: " + i + " " + word);

            }
            int length = word.length();
            char alpha = word.charAt(0);
            int position = positionOfLetter(alpha);
            int reverse = (length - 3) * LOFA + position - 128;
            byteVector[i] = (byte)reverse;
            i++;

        }
    }

    private char letterOfPosition(int start) throws Exception {
        if (start >= LOFA || start < 0){
            throw new Exception(ErrorMessages.LENGTH_OF_MESSAGE_ERROR);
        }
        return (char)((byte)start + (byte)'a');

    }

    private int positionOfLetter(char letter) throws Exception {
        if (letter < 'a' || letter > 'z'){
            throw new Exception(ErrorMessages.OUT_OF_RANGE);
        }
        return (int)((byte)letter) - ((int)(byte)'a');

    }

    public String[] getWordVector() {
        return wordVector;
    }

    public byte[] getByteVector() {
        return byteVector;
    }
}
