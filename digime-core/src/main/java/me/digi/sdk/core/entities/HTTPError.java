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

    public HTTPError(String error, int code) {
        this.error = error;
        this.code = code;
    }
}