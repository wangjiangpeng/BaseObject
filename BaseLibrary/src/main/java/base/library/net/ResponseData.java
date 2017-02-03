package base.library.net;

/**
 * 网络请求应答参数
 * <p>
 * Created by wangjiangpeng01 on 2017/2/3.
 */

public class ResponseData {

    /**
     * 应答码
     */
    private int code;

    /**
     * 内容
     */
    private byte[] data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

}
