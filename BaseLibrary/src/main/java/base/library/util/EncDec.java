package base.library.util;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Des算法加密解密
 *
 * Created by wangjiangpeng01 on 2017/8/16.
 */

public class EncDec {

    private EncDec() {

    }

    public static String encrypt(String key, String input) throws Exception {
        return binToHex(desEncrypt(key, input.getBytes("utf8")));
    }

    public static String decrypt(String key, String input) {
        try {
            return new String(desDecrypt(key, hexToBin(input)), "utf8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static byte[] desEncrypt(String keyStr, byte[] plainText) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte rawKeyData[] = keyStr.getBytes("utf8");
        DESKeySpec dks = new DESKeySpec(rawKeyData);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, key, sr);
        byte data[] = plainText;
        byte encryptedData[] = cipher.doFinal(data);
        return encryptedData;
    }

    private static byte[] desDecrypt(String keyStr, byte[] plainText) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte rawKeyData[] = keyStr.getBytes("utf8");
        DESKeySpec dks = new DESKeySpec(rawKeyData);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, key, sr);
        byte data[] = plainText;
        byte encryptedData[] = cipher.doFinal(data);
        return encryptedData;
    }

    private static String binToHex(byte[] md) {
        StringBuffer sb = new StringBuffer("");
        int read = 0;
        for (int i = 0; i < md.length; i++) {
            read = md[i];
            if (read < 0)
                read += 256;
            if (read < 16)
                sb.append("0");
            sb.append(Integer.toHexString(read));
        }
        return sb.toString();
    }

    private static byte[] hexToBin(String hexStr) {
        String charString = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int index = 0; index < hexStr.length(); index++) {
            charString += hexStr.charAt(index) + "" + hexStr.charAt(++index);
            int charInt = Integer.parseInt(charString, 16);
            if (charInt - 256 < 0) {
                baos.write(charInt - 256);
            } else {
                baos.write(charInt);
            }
            charString = "";
        }
        return baos.toByteArray();
    }
}