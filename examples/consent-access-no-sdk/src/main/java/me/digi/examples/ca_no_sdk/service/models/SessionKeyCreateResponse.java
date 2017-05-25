/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service.models;

import com.google.gson.annotations.SerializedName;

public class SessionKeyCreateResponse {
    @SerializedName("sessionKey")
    public String sessionKey;

    @SerializedName("expiry")
    public long expiry;
}
