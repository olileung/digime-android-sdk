/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service.models;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataGetResponse {

    @SerializedName("fileContent")
    public List<JsonElement> fileContent;

    @SerializedName("fileList")
    public List<String> fileList;
}
