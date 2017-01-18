package base.library.net;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求参数
 *
 * Created by wangjiangpeng01 on 2017/1/11.
 */

public class RequestParam {

    /**
     * 域名
     */
    private String domain;
    /**
     * 头部
     */
    private HashMap<String, String> headers = new HashMap<>();
    /**
     * get数据
     */
    private String getData;
    /**
     * post数据
     */
    private String postData;

    /**
     * ssl是否双向验证
     */
    private boolean isSSLMutual;

    /**
     * 私钥资源
     */
    private int keyStoreId;

    /**
     * 信任证书密码
     */
    private int trustStoreId;

    /**
     * 私钥密码
     */
    private String keyStorePass;

    /**
     * 信任证书密码
     */
    private String trustStorePass;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public Map<String, String> getHeaders() {
        return (Map<String, String>) headers.clone();
    }

    public String getGetData() {
        return getData;
    }

    public void setGetData(String getData) {
        this.getData = getData;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    public boolean isHttps() {
        if(domain != null && domain.startsWith("https")){
            return true;
        } else {
            return false;
        }
    }

    public boolean isSSLMutual() {
        return isSSLMutual;
    }

    public void setSSLMutual(boolean SSLMutual) {
        isSSLMutual = SSLMutual;
    }

    public int getKeyStoreId() {
        return keyStoreId;
    }

    public void setKeyStoreId(int keyStoreId) {
        this.keyStoreId = keyStoreId;
    }

    public int getTrustStoreId() {
        return trustStoreId;
    }

    public void setTrustStoreId(int trustStoreId) {
        this.trustStoreId = trustStoreId;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public void setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass;
    }

    public String getUrl() {
        return getData == null ? domain : domain + getData;
    }

}
