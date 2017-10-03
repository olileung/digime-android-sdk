/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.config;

import android.net.Uri;
import android.os.Build;

import java.text.Normalizer;

import me.digi.sdk.core.BuildConfig;

public class DefaultApiConfig implements ApiConfig{
    private static final String API_HOST_URL = "https://" + BuildConfig.BASE_HOST;
    private static volatile DefaultApiConfig singleton;

    private final String url;

    private DefaultApiConfig() {
        this.url = API_HOST_URL;
    }

    public static DefaultApiConfig config() {
        if (singleton == null) {
            synchronized (DefaultApiConfig.class) {
                singleton = new DefaultApiConfig();
            }
        }
        return singleton;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getHost() {
        return Uri.parse(getUrl()).getHost();
    }

    @Override
    public String userAgentString(String appName, String versionCode) {
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

    public Uri.Builder buildUrl(String... paths) {
        final Uri.Builder builder = Uri.parse(getUrl()).buildUpon();
        if (paths != null) {
            for (String p : paths) {
                builder.appendPath(p);
            }
        }
        return builder;
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
