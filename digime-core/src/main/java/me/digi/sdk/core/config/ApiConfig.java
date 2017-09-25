/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.config;

public abstract class ApiConfig {

    public abstract String getUrl();

    public abstract String getHost();

    public abstract String userAgentString(String appName, String versionCode);
}
