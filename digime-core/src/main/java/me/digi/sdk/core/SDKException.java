/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

public class SDKException extends RuntimeException {

    public SDKException(String detail) {
        super(detail);
    }

    public SDKException(String detail, Throwable throwable) {
        super(detail, throwable);
    }
}
