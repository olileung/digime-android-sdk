/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.provider;

import me.digi.sdk.core.CASession;
import me.digi.sdk.core.CAContract;
import me.digi.sdk.core.DigiMeVersion;
import me.digi.sdk.core.config.ApiConfig;

import java.io.IOException;
import java.util.Collections;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class OkHttpProvider {

    private static final String SDK_USER_AGENT = "DigiMeSDK";

    public static OkHttpClient client(SSLSocketFactory sslSocketFactory) {
        return attachUserAgent(providerBuilder(sslSocketFactory)).build();
    }

    public static OkHttpClient client(CASession session,
                                               CAContract contract,
                                               SSLSocketFactory sslSocketFactory) {
        return attachUserAgent(providerBuilder(session, contract, sslSocketFactory)).build();
    }

    public static OkHttpClient client(OkHttpClient client,
                                               SSLSocketFactory sslSocketFactory) {
        if (client == null) {
            throw new IllegalArgumentException("Must provide a valid http client.");
        }

        return attachUserAgent(client.newBuilder()).connectionSpecs(Collections.singletonList(defaultConnectionSpec())).sslSocketFactory(sslSocketFactory).build();
    }

    public static OkHttpClient client(
            OkHttpClient client,
            CASession session,
            CAContract contract,
            SSLSocketFactory sslSocketFactory) {
        if (session == null) {
            throw new IllegalArgumentException("Must provide a valid session.");
        }

        if (client == null) {
            throw new IllegalArgumentException("Must provide a valid http client.");
        }

        return attachUserAgent(client.newBuilder()).connectionSpecs(Collections.singletonList(defaultConnectionSpec())).sslSocketFactory(sslSocketFactory)
                .build();
    }

    public static OkHttpClient.Builder providerBuilder(SSLSocketFactory sslSocketFactory) {
        return new OkHttpClient.Builder().connectionSpecs(Collections.singletonList(defaultConnectionSpec())).sslSocketFactory(sslSocketFactory);
    }

    public static OkHttpClient.Builder providerBuilder(
            CASession session, CAContract contract,
            SSLSocketFactory sslSocketFactory) {
        if (session == null) {
            throw new IllegalArgumentException("Must provide a valid session.");
        }

        return new OkHttpClient.Builder().connectionSpecs(Collections.singletonList(defaultConnectionSpec())).sslSocketFactory(sslSocketFactory);
    }

    private static ConnectionSpec defaultConnectionSpec() {
        ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();
        return connectionSpec;
    }

    private static OkHttpClient.Builder attachUserAgent(OkHttpClient.Builder builder) {
        return builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                final Request request = chain.request().newBuilder()
                        .header("User-Agent", ApiConfig.sdkUA(SDK_USER_AGENT, DigiMeVersion.VERSION))
                        .build();
                return chain.proceed(request);
            }
        });
    }

//TODO plug interceptors below
}