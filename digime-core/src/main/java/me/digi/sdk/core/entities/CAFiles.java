/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.entities;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CAFiles {

    @SerializedName("fileList")
    public List<String> fileIds;
}