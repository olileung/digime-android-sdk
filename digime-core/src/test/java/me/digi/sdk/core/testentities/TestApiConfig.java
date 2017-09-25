/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.testentities;

import android.net.Uri;
import android.support.annotation.NonNull;

import me.digi.sdk.core.config.ApiConfig;
import okhttp3.HttpUrl;

public class TestApiConfig extends ApiConfig {
    private HttpUrl mockUrl;

    public TestApiConfig(@NonNull HttpUrl url) {
        this.mockUrl = url;
    }

    @Override
    public String getUrl() {
        return mockUrl.toString();
    }

    @Override
    public String getHost() {
        return Uri.parse(getUrl()).getHost();
    }

    @Override
    public String userAgentString(String appName, String versionCode) {
        return "TestAgent 1.0";
    }
}
