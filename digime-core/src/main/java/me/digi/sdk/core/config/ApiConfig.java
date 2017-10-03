/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.config;

public interface ApiConfig {

    String getUrl();

    String getHost();

    String userAgentString(String appName, String versionCode);
}
