package com.nsu.datasavenet.security;

import com.nsu.datasavenet.utils.EncryptDecryptUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptDecryptUtilsTest {

    private final String password = "testPassword123";


    @Test
    void testBinaryFileEncryptionDecryption() {
        byte[] data = new byte[]{0x00, 0x01, 0x7F, (byte) 0xFF, 0x55};

        byte[] encryptedData = EncryptDecryptUtils.encryptObjectWithPassword(data, password);
        byte[] decryptedData = EncryptDecryptUtils.decryptObjectWithPassword(encryptedData, password, byte[].class);

        assertArrayEquals(data, decryptedData);
    }

    @Test
    void testDifferentPasswordsFail() {

        String secret = "тест секрет";

        byte[] data = EncryptDecryptUtils.encryptObjectWithPassword(secret,"correct");

        assertThrows(Exception.class, () -> EncryptDecryptUtils.decryptObjectWithPassword(data,"wrong", String.class));
    }

    @Test
    void testNullPasswordThrows() {
        assertThrows(RuntimeException.class, () -> EncryptDecryptUtils.encryptObjectWithPassword("Secret",null));
    }

    @Test
    void testEmptyPasswordIsValidButDiscouraged() {
        String message = "Секрет";

        byte[] data = EncryptDecryptUtils.encryptObjectWithPassword(message, "");
        String result = EncryptDecryptUtils.decryptObjectWithPassword(data, "", String.class);

        assertEquals(message, result);
    }

    @Test
    void testEncryptionProducesDifferentOutputsForSamePassword() {
        byte[] fist = EncryptDecryptUtils.encryptObjectWithPassword("Same", password);
        byte[] second = EncryptDecryptUtils.encryptObjectWithPassword("Same", password);

        assertFalse(java.util.Arrays.equals(fist, second));
    }

    @Test
    void testNLPWhenEncryptNullThrows() {

        assertThrows(NullPointerException.class, () -> EncryptDecryptUtils.encryptObjectWithPassword(null, password));
    }
}
