/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

public class SDKException extends RuntimeException {
    protected final int code;
    static final int DEFAULT_EXCEPTION_CODE = -1;

    public SDKException(String detail) {
        super(detail);
        code = DEFAULT_EXCEPTION_CODE;
    }

    public SDKException(String detail, int code) {
        super(detail);
        this.code = code;
    }

    public SDKException(String detail, Throwable throwable) {
        super(detail, throwable);
        code = DEFAULT_EXCEPTION_CODE;
    }

    public SDKException(String detail, Throwable throwable, int code) {
        super(detail, throwable);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
