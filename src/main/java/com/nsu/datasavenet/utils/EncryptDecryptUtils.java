package com.nsu.datasavenet.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecryptUtils {

    private static final SecureRandom random = new SecureRandom();

    // TODO Вырезать и добавить шифрование во все use-case
    public static void main(String[] args) throws Exception {

        String password = "yourStrongPassword";
        File inputFile = new File("src/test/resources/d.txt");
        File outputFile = new File("src/test/resources/d.enc");

//        byte[] salt = generateSalt();
//        byte[] iv = generateIV();
//        SecretKeySpec secretKey = generatePBKDF2Key(password, salt, 65536, 256);

//        encryptFile(inputFile, outputFile, secretKey, iv);

//        encryptObjectWithPassword(inputFile,outputFile,"qwerty12345");
//
//        decryptObjectWithPassword(outputFile,new File("src/test/resources/decrypted.txt"), "qwerty12345");

        // сохраняем salt и iv для последующей расшифровки
//        System.out.println("Salt (base64): " + Base64.getEncoder().encodeToString(salt));
//        System.out.println("IV (base64): " + Base64.getEncoder().encodeToString(iv));
    }


    public static byte[] encryptObjectWithPassword(Object input, String password){
        Objects.requireNonNull(input);
        try {
            byte[] salt = generateSalt();
            byte[] iv = generateIV();
            SecretKeySpec key = generatePBKDF2Key(password, salt, 65536, 256);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (
                        CipherOutputStream cos = new CipherOutputStream(baos, cipher)) {

                    baos.write(salt);
                    baos.write(iv);
                    cos.write(serializeObject(input));
                }
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T decryptObjectWithPassword(byte[] input, String password, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
            byte[] salt = new byte[16];
            byte[] iv = new byte[16];

            bais.read(salt);
            bais.read(iv);

            SecretKeySpec key = generatePBKDF2Key(password, salt, 65536, 256);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            try (CipherInputStream cis = new CipherInputStream(bais, cipher)) {
                return deserializeObject(cis.readAllBytes(), clazz);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static SecretKeySpec generatePBKDF2Key(String password, byte[] salt, int iterations, int keyLength)
            throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
    }

    private static byte[] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bs);
        os.writeObject(object);
        os.close();
        return bs.toByteArray();
    }

    private static <T> T deserializeObject(byte[] bytes, Class<T> clazz)  {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            Object obj = ois.readObject();
            if (!clazz.isInstance(obj)) {
                throw new ClassCastException("Expected " + clazz.getName() + " but got " + obj.getClass().getName());
            }
            return clazz.cast(obj);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

}
