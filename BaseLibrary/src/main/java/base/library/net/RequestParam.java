package base.library.net;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.key;
import static android.R.attr.value;

/**
 * 网络请求参数
 * <p>
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
     * 头部
     */
    private HashMap<String, String> posts = new HashMap<>();
    /**
     * get数据
     */
    private String getData;
    /**
     * ssl是否双向验证
     */
    private boolean isSSLMutual;
    /**
     * 私钥资源
     */
    private int keyStoreId;
    /**
     * 信任证书资源
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
    /**
     * 连接超时时间（单位毫秒）
     */
    private long connectTimeout;
    /**
     * 读取超时时间（单位毫秒）
     */
    private long readTimeout;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addHeaders(Map<String, String> map) {
        headers.putAll(map);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public Map<String, String> getHeaders() {
        return (Map<String, String>) headers.clone();
    }

    public void addPost(String key, String value) {
        posts.put(key, value);
    }

    public void addPosts(Map<String, String> map) {
        posts.putAll(map);
    }

    public void removePost(String key) {
        posts.remove(key);
    }

    public Map<String, String> getPosts() {
        return (Map<String, String>) posts.clone();
    }

    public String getGetData() {
        return getData;
    }

    public void setGetData(String getData) {
        this.getData = getData;
    }

    public boolean isHttps() {
        if (domain != null && domain.startsWith("https")) {
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

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getUrl() {
        return getData == null ? domain : domain + getData;
    }

}
