/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;

public class HTTPError {

    @SerializedName("error")
    public final String error;

    @SerializedName("code")
    public final int code;

    @SerializedName("message")
    public final String message;

    public HTTPError(String error, int code, String message) {
        this.error = error;
        this.code = code;
        this.message = message;
    }
}