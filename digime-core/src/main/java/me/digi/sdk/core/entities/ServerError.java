/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.entities;

public abstract class ServerError {
    private int responseCode;

    public abstract String errorCode();

    public abstract String message();

    public abstract String reference();

    public int code() {
        return responseCode;
    }

    public void setCode(int code) {
        responseCode = code;
    }
}
