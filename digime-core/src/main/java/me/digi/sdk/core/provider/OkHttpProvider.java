/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.provider;

import android.support.annotation.NonNull;

import me.digi.sdk.core.BuildConfig;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.internal.CAContentCryptoInterceptor;
import me.digi.sdk.core.session.CASession;
import me.digi.sdk.core.DigiMeSDKVersion;
import me.digi.sdk.core.config.ApiConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpProvider {

    private static final String SDK_USER_AGENT = "DigiMeSDK";

    public static OkHttpClient client(CertificatePinner certPinner) {
        return attachInterceptors(providerBuilder(certPinner))
                .build();
    }

    public static OkHttpClient client(CASession session,
                                               CertificatePinner certPinner) {
        return attachInterceptors(providerBuilder(session, certPinner))
                .build();
    }

    public static OkHttpClient client(OkHttpClient client,
                                      CertificatePinner certPinner) {
        if (client == null) {
            throw new IllegalArgumentException("Must provide a valid http client.");
        }

        return attachInterceptors(client.newBuilder()).connectionSpecs(defaultConnectionSpec())
                .certificatePinner(certPinner)
                .build();
    }

    public static OkHttpClient client(
            OkHttpClient client,
            CASession session,
            CertificatePinner certPinner) {
        if (session == null) {
            throw new IllegalArgumentException("Must provide a valid session.");
        }

        if (client == null) {
            throw new IllegalArgumentException("Must provide a valid http client.");
        }

        return attachInterceptors(client.newBuilder())
                .connectionSpecs(defaultConnectionSpec())
                .certificatePinner(certPinner)
                .build();
    }

    private static OkHttpClient.Builder providerBuilder(CertificatePinner certPinner) {
        return new OkHttpClient.Builder()
                .connectionSpecs(defaultConnectionSpec())
                .certificatePinner(certPinner);
    }

    private static OkHttpClient.Builder providerBuilder(
            CASession session,
            CertificatePinner certPinner) {
        if (session == null) {
            throw new IllegalArgumentException("Must provide a valid session.");
        }

        return new OkHttpClient.Builder()
                .connectionSpecs(defaultConnectionSpec())
                .certificatePinner(certPinner);
    }

    private static List<ConnectionSpec> defaultConnectionSpec() {
        return Collections.singletonList(new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build());
    }

    private static OkHttpClient.Builder attachInterceptors(OkHttpClient.Builder builder) {

        if (BuildConfig.LOG_REQUESTS) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            builder.addInterceptor(logging);
        }
        builder.addInterceptor(new CAContentCryptoInterceptor(DigiMeClient.getDefaultKeyLoader().getStore()));
        return setDefaultTimeout(builder).addInterceptor(new Interceptor() {
                          @Override
                          public Response intercept(@NonNull Chain chain) throws IOException {
                              final Request request = chain.request().newBuilder()
                                      .header("User-Agent", ApiConfig.sdkUA(SDK_USER_AGENT, DigiMeSDKVersion.VERSION))
                                      .build();
                              return chain.proceed(request);
                          }
                      });
    }

    private static OkHttpClient.Builder setDefaultTimeout(OkHttpClient.Builder builder) {
        return builder.connectTimeout(DigiMeClient.globalConnectTimeout, TimeUnit.SECONDS)
                .readTimeout(DigiMeClient.globalReadWriteTimeout, TimeUnit.SECONDS)
                .writeTimeout(DigiMeClient.globalReadWriteTimeout, TimeUnit.SECONDS);
    }
}