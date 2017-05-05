package base.library.net;

import android.app.Application;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import base.library.BaseApplication;
import base.library.net.download.DownloadListener;
import base.library.net.download.DownloadParam;
import base.library.net.download.IDownloadManager;

/**
 * 网络请求
 * <p>
 * keytool命令如下：
 * -genkey     在用户主目录中创建一个默认文件".jks",还会产生一个别名，别名中包含用户的公钥、私钥和证书
 * -alias      产生别名
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
 * keytool -export -alias "tomcat" -keystore "f:\server.keystore" -file "f:\server.cer"
 * CER文件导入到服务器的密钥库，添加为一个信任证书
 * keytool -import -alias "client" -keystore "f:\servertrust.keystore" -file "f:\client.cer"
 * CER文件导入到客户端的密钥库，添加为一个信任证书
 * keytool -import -alias "tomcat" -keystore "f:\clienttrust.keystore" -file "f:\server.cer" -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider
 * <p>
 * Created by wangjiangpeng01 on 2017/1/10.
 */
public class HttpClient {

    private static final long DEFAULT_CONNECT_TIMEOUT = 30000;

    private static final long DEFAULT_READ_TIMEOUT = 30000;

    /**
     * 请求
     *
     * @param param 请求参数
     * @return
     * @throws IOException
     */
    public ResponseData request(RequestParam param) {
        OkHttpClient client = createOkHttps(param);
        Request.Builder rBuilder = createRequestBuilder(param);
        // 执行请求
        ResponseData responseData = new ResponseData();
        Request request = rBuilder.build();
        try {
            Response response = client.newCall(request).execute();
            responseData.setCode(response.code());
            if (response.isSuccessful()) {
                responseData.setData(response.body().bytes());
                response.body().close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            // 抛异常code参数设置为404
            responseData.setCode(404);
        }

        return responseData;
    }

    /**
     * 下载
     */
    public ResponseData download(DownloadParam param, DownloadListener listener) {
        OkHttpClient client = createOkHttps(param);
        Request.Builder rBuilder = createRequestBuilder(param);

        // 头部续传参数
        File file = new File(param.getDownloadPath());
        if (param.getRange() != 0 && file.exists()) {
            rBuilder.addHeader("RANGE", "bytes=" + param.getRange() + "-");
        }
        // 下载
        ResponseData responseData = new ResponseData();
        Request request = rBuilder.build();
        InputStream is = null;
        RandomAccessFile raf = null;
        try {
            Response response = client.newCall(request).execute();
            is = response.body().byteStream();
            long total = response.body().contentLength();
            if (file.exists()) {
                raf = new RandomAccessFile(param.getDownloadPath(), "rw");
                raf.seek(param.getRange());

            } else {
                file.createNewFile();
                raf = new RandomAccessFile(param.getDownloadPath(), "rw");
                if(total > 0){
                    raf.setLength(total);
                }
            }

            int len = 0;
            long sum = 0;
            byte[] buf = new byte[1024];
            while (((len = is.read(buf)) != -1) && param.getState() == IDownloadManager.STATE_DOWNLOADING) {
                raf.write(buf, 0, len);
                sum += len;
                if (listener != null) {
//                    listener.onDownloaded(total, sum);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return responseData;
    }

    private OkHttpClient createOkHttps(RequestParam param) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(param.getConnectTimeout() > 0 ? param.getConnectTimeout() : DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
        client.setReadTimeout(param.getReadTimeout() > 0 ? param.getReadTimeout() : DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS);
        // https
        if (param.isHttps()) {
            client.setSslSocketFactory(createSSLSocketFactory(param));
            // 域名验证视需求而定
            client.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }
        return client;
    }

    private Request.Builder createRequestBuilder(RequestParam param) {
        // 地址
        Request.Builder rBuilder = new Request.Builder();
        rBuilder.url(param.getUrl());
        // 头部
        Map<String, String> headers = param.getHeaders();
        for (String key : headers.keySet()) {
            rBuilder.addHeader(key, headers.get(key));
        }
        // post
        Map<String, String> posts = param.getPosts();
        if (posts.size() > 0) {
            FormEncodingBuilder feBuilder = new FormEncodingBuilder();
            for (String key : posts.keySet()) {
                feBuilder.add(key, posts.get(key));
            }
            rBuilder.post(feBuilder.build());
        }
        return rBuilder;
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
            TrustManager[] trustManagers = trustManager.getTrustManagers();

            // 双向验证时，客户端密钥加载
            KeyManager[] keyManagers = null;
            if (param.isSSLMutual()) {
                inputStream = app.getResources().openRawResource(param.getKeyStoreId());
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                KeyManagerFactory keyManager = KeyManagerFactory.getInstance("X509");
                keyStore.load(inputStream, param.getKeyStorePass().toCharArray());
                keyManager.init(keyStore, param.getKeyStorePass().toCharArray());
                keyManagers = keyManager.getKeyManagers();
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, new SecureRandom());

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

}
