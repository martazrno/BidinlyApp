package via.pro3.datatierserver.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    private static SecretKeySpec getKey(String passwordHash) {
        byte[] keyBytes = passwordHash.substring(0, 16).getBytes();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String data, String passwordHash) throws Exception {
        SecretKeySpec keySpec = getKey(passwordHash);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decrypt(String encrypted, String passwordHash) throws Exception {
        SecretKeySpec keySpec = getKey(passwordHash);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
    }
}