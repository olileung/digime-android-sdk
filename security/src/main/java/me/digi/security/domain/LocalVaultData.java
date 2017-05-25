/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.security.domain;

import com.google.gson.annotations.SerializedName;

/**
 * LocalVaultData which contains user specific data
 */

public class LocalVaultData implements VaultData {
    @SerializedName("deviceUUID")
    public String deviceUUID;

    @SerializedName("libraryID")
    public String libraryId;

    @SerializedName("libraryPath")
    public String libraryPath;

    @SerializedName("pCloudReference")
    public String pCloudReference;

    @SerializedName("pCloudToken")
    public String pCloudToken;

    @SerializedName("pCloudType")
    public String pCloudType;

    public LocalVaultData(String deviceUUID, String libraryId, String libraryPath) {
        this.deviceUUID = deviceUUID;
        this.libraryId = libraryId;
        this.libraryPath = libraryPath;
    }
}
