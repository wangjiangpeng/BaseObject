package base.library.net;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import base.library.BaseApplication;

/**
 * 网络请求
 * <p>
 * keytool命令如下：
 * -genkey     在用户主目录中创建一个默认文件".jks",还会产生一个server_jks_cennavi的别名，server_jks_cennavi中包含用户的公钥、私钥和证书
 * -alias      产生别名server_jks_testws
 * -keystore   指定密钥库的名称(产生的各类信息将不在.jks文件中
 * -keyalg     指定密钥的算法
 * -validity   指定创建的证书有效期多少天
 * -keysize    指定密钥长度
 * -storepass  指定密钥库的密码
 * -keypass    指定别名条目的密码
 * -dname      指定证书拥有者信息
 * <p>
 * <p>
 * 生成服务器密钥
 * keytool -genkey -alias "tomcat" -keyalg "RSA" -keystore "f:\server.keystore"
 * 生成客户端密钥（android只支持BKS格式）
 * keytool -genkey -alias "client" -keyalg "RSA" -keystore "f:\client.keystore" -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider
 * 客户端密钥导出为一个单独的CER文件
 * keytool -export -alias "client" -keystore "f:\client.keystore" -file "f:\client.cer" -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider
 * 服务器密钥导出为一个单独的CER文件
 * keytool -export -alias "tomcat" -keystore "f:\server.keystore" -file "f:\tserver.cer"
 * CER文件导入到服务器的密钥库，添加为一个信任证书
 * keytool -import -alias "client" -keystore "f:\servertrust.keystore" -file "f:\client.cer"
 * CER文件导入到客户端的密钥库，添加为一个信任证书
 * keytool -import -alias "tomcat" -keystore "f:\clienttrust.keystore" -file "f:\tserver.cer" -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider
 * <p>
 * Created by wangjiangpeng01 on 2017/1/10.
 */
public class HttpRequest {

    /**
     * 请求
     *
     * @param param 请求参数
     * @return
     * @throws IOException
     */
    public byte[] request(RequestParam param) throws IOException {
        OkHttpClient client = new OkHttpClient();
        // https
        if (param.isHttps()) {
            client.setSslSocketFactory(createSSLSocketFactory(param));
            client.setHostnameVerifier(new HostnameVerifierImpl());
        }
        // 请求数据填充
        Request.Builder builder = new Request.Builder();
        builder.url(param.getUrl());
        Map<String, String> headers = param.getHeaders();
        for (String key : headers.keySet()) {
            builder.addHeader(key, headers.get(key));
        }
        // 执行请求
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            byte[] result = response.body().bytes();
            response.body().close();
            return result;

        } else {
            throw new IOException("service error:" + response);
        }
    }

    private SSLSocketFactory createSSLSocketFactory(RequestParam param) {
        InputStream trustInputStream = null;
        InputStream inputStream = null;
        try {
            Application app = BaseApplication.getInstance();
            // 信任证书加载
            trustInputStream = app.getResources().openRawResource(param.getTrustStoreId());
            KeyStore trustKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            TrustManagerFactory trustManager = TrustManagerFactory.getInstance("X509");
            trustKeyStore.load(trustInputStream, param.getTrustStorePass().toCharArray());
            trustManager.init(trustKeyStore);

            // 双向验证时，客户端密钥加载
            KeyManager[] keyManagers = null;
            if(param.isSSLMutual()){
                inputStream = app.getResources().openRawResource(param.getKeyStoreId());
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                KeyManagerFactory keyManager = KeyManagerFactory.getInstance("X509");
                keyStore.load(inputStream, param.getKeyStorePass().toCharArray());
                keyManager.init(keyStore, param.getKeyStorePass().toCharArray());
                keyManagers = keyManager.getKeyManagers();
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManager.getTrustManagers(), null);

            return sslContext.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (trustInputStream != null) {
                try {
                    trustInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private static class HostnameVerifierImpl implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}
