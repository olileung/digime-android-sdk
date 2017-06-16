/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataGetEncryptedResponse {

    @SerializedName("fileContent")
    public String fileContent;

    @SerializedName("fileList")
    public List<String> fileList;
}
