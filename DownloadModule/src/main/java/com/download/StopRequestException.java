package com.download;

/**
 * Created by wangjiangpeng01 on 2017/8/21.
 */

public class StopRequestException extends RuntimeException {

    private int mStatus;

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public StopRequestException(int status, String detailMessage) {
        super(detailMessage);
        this.mStatus = status;
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public StopRequestException(int status, Throwable throwable) {
        super(throwable);
        this.mStatus = status;
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace,
     * the specified detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param throwable     the cause of this exception.
     */
    public StopRequestException(int status, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.mStatus = status;
    }

    public int getFinalStatus() {
        return mStatus;
    }

}
