/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
public class HTTPError {

    @SerializedName("error")
    public String error;

    @SerializedName("code")
    public int code;

    @SerializedName("message")
    public String message;

    public HTTPError(String error, int code, String message) {
        this.error = error;
        this.code = code;
        this.message = message;
    }

    public HTTPError(HTTPErrorV2 errorV2, int httpCode) {
        this.error = errorV2.cause.code;
        this.message = errorV2.cause.message;
        this.code = httpCode;
    }

    public class HTTPErrorV2 {

        public class InternalHTTPError {
            @SerializedName("code")
            public String code;

            @SerializedName("message")
            public String message;

            @SerializedName("reference")
            public String reference;
        }

        @SerializedName("error")
        public InternalHTTPError cause;
    }
}