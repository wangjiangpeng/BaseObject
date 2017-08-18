package base.library.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 加密工具类
 *
 * Created by wangjiangpeng01 on 2017/8/16.
 */

public class SecurityUtil {

    private static String base64_random = "httpstd";

    /**
     * 将字符串编码为md5格式
     *
     * @param value
     * @return
     */
    public static String md5Encode(String value) {
        String tmp = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(value.getBytes("utf8"));
            byte[] md = md5.digest();
            tmp = binToHex(md);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public static String binToHex(byte[] md) {
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

    /**
     * base64编码
     *
     * @param value
     *            字符串
     * @return
     */
    public static String encodeBase64(String value) {
        return base64_random + Base64.encode(value);
    }

    /**
     * base64解码
     *
     * @param value
     *            字符串
     * @param random
     *            混淆码
     * @return
     */
    public static String decodeBase64(String value) {
        if (value == null || value.length() <= base64_random.length()) {
            return "";

        } else {
            int count = value.length();
            int last = base64_random.length();
            return Base64.decode(value.substring(last, count), "utf-8");
        }
    }

    /**
     * 计算给定路径的文件的md5值
     *
     * @param path
     *            APK文件路径
     * @return
     */
    public static String getMessageMd5(String path) {
        return getMessageDigest(path, "md5");
    }

    /**
     * 计算给定路径的文件的SHA-1值
     *
     * @param path
     *            APK文件路径
     * @return
     */
    public static String getMessageSha1(String path) {
        return getMessageDigest(path, "sha-1");
    }

    /**
     * 计算给定路径的文件的SHA-1值
     *
     * @param path
     *            APK文件路径
     * @param algorithm
     *            算法
     * @return
     */
    public static String getMessageDigest(String path, String algorithm) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        // 该对象通过使用 update() 方法处理数据
        BufferedInputStream in = null;
        MessageDigest messagedigest = null;
        try {
            messagedigest = MessageDigest.getInstance(algorithm);
            in = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[8192];
            int len = 0;

            while ((len = in.read(buffer)) != -1) {
                messagedigest.update(buffer, 0, len);
            }

            // 对于给定数量的更新数据，digest 方法只能被调用一次。在调用 digest 之后，MessageDigest
            // 对象被重新设置成其初始状态。
            return binToHex(messagedigest.digest());

        } catch (Throwable e) {
            e.printStackTrace();

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获得签名信息的md5标示
     *
     * @param apkPath
     *            apk完整路径
     * @return
     */
    public static String getApkSignInfo(String apkPath) {
        try {
            Signature[] mSignatures = initApkSignInfo(apkPath);
            if (mSignatures != null && mSignatures.length > 0) {
                StringBuilder builder = new StringBuilder();
                builder.append(mSignatures.length);
                for (int index = 0; index < mSignatures.length; index++) {
                    builder.append(mSignatures[index].toCharsString());
                }

                return md5Encode(builder.toString());
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    // 初始化sign信息，一个apk包是可以有多个签名的。
    private static Signature[] initApkSignInfo(String apkPath) throws Exception {
        Certificate[] certs = loadCertificates(apkPath);
        if (certs != null && certs.length > 0) {
            int count = certs.length;
            Signature[] mSignatures = new Signature[certs.length];
            for (int i = 0; i < count; i++) {
                byte[] tmpBytes = certs[i].getEncoded();
                mSignatures[i] = new Signature(tmpBytes);
            }

            return mSignatures;

        } else {
            return null;
        }
    }

    // 处于性能考虑值检查AndroidManifest.xml 的签名信息
    private static Certificate[] loadCertificates(String apkPath) throws Exception {
        InputStream is = null;

        try {
            JarFile jarFile = new JarFile(apkPath);
            byte[] readBuffer = new byte[8192];
            JarEntry jarEntry = jarFile.getJarEntry("AndroidManifest.xml");
            is = new BufferedInputStream(jarFile.getInputStream(jarEntry));

            // 必须读完这个文件的才能获取这个文件的签名信息。
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
            }

            return jarEntry.getCertificates();

        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }

    }

    // 签名信息封装类
    private static class Signature {

        // 签名原始的二进制信息
        private final byte[] mSignature;

        public Signature(byte[] signBytes) {
            this.mSignature = signBytes;
        }

        // 将字节高低位转化为对应的字符
        public String toCharsString() {
            int length = mSignature.length;
            char[] text = new char[length * 2];
            for (int j = 0; j < length; j++) {
                byte v = mSignature[j];
                int d = v >> 4 & 0xF;
                text[(j * 2)] = (char) (d >= 10 ? 97 + d - 10 : 48 + d);

                d = v & 0xF;
                text[(j * 2 + 1)] = (char) (d >= 10 ? 97 + d - 10 : 48 + d);
            }

            return new String(text);
        }
    }
}
