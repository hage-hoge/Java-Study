package reservation.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 128bit でバイト配列を暗号化／復号するユーティリティ。
 * デモ用に「AES/ECB/PKCS5Padding」を使用。
 */
public final class CryptoUtil {
    // ★ 実用時は外部ファイル・環境変数に逃がす
    private static final byte[] KEY = "0123456789abcdef".getBytes(); // 16byte

    private CryptoUtil() {}

    public static byte[] encrypt(byte[] plain) throws Exception {
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"));
        return c.doFinal(plain);
    }

    public static byte[] decrypt(byte[] cipher) throws Exception {
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"));
        return c.doFinal(cipher);
    }
}
