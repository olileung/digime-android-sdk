/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CAEncryptedFileResponse {
    @SerializedName("fileContent")
    public String fileContent;

    @SerializedName("fileList")
    public List<String> fileIds;
}
