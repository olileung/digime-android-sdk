/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.config;

import android.net.Uri;
import android.os.Build;

import me.digi.sdk.core.BuildConfig;
import java.text.Normalizer;

public class ApiConfig {
    private static final String API_HOST_URL = "https://" + BuildConfig.BASE_HOST;

    private final String url;

    public ApiConfig() {
        this(API_HOST_URL);
    }

    private ApiConfig(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return Uri.parse(getUrl()).getHost();
    }

    public Uri.Builder buildUrl(String... paths) {
        final Uri.Builder builder = Uri.parse(getUrl()).buildUpon();
        if (paths != null) {
            for (String p : paths) {
                builder.appendPath(p);
            }
        }
        return builder;
    }

    public static String sdkUA(String appName, String versionCode) {
        String ua = appName +
                '/' + versionCode +
                ' ' +
                Build.MODEL + '/' + Build.VERSION.RELEASE +
                " (" +
                Build.MANUFACTURER + ';' +
                Build.MODEL + ';' +
                Build.BRAND + ';' +
                Build.PRODUCT +
                ')';
        return fromUtf(Normalizer.normalize(ua, Normalizer.Form.NFD));
    }

    private static String fromUtf(String str) {
        final StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c > '\u001f' && c < '\u007f') {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
