package me.digi.security.domain;

import com.google.gson.annotations.SerializedName;

/**
 * CloudVaultData which contains user specific data
 *
 * EXAMPLE
 *
 * {
 *    "createddate": 1489402053837,
 *    "datemode": "A",
 *    "jfsversion": "18.0.0",
 *    "mapfilepath": "18/map_0.json",
 *    "msks": [
 *      "daeedf4d6acdd2ac5280c85657d00605666a3fab516ff813d58c5c0e1820dcb8"
 *    ],
 *    "updateddate": 1489402625233,
 *    "uuid": "12345"
 * }
 *
 */

public class CloudVaultData implements VaultData {
    @SerializedName("createddate")
    public long createdDate;

    @SerializedName("datemode")
    public String dateMode;

    @SerializedName("jfsversion")
    public String jfsVersion;

    @SerializedName("mapfilepath")
    public String mapFilePath;

    @SerializedName("msks")
    public String[] msks;

    @SerializedName("updateddate")
    public long updatedDate;

    @SerializedName("uuid")
    public String uuid;
}
