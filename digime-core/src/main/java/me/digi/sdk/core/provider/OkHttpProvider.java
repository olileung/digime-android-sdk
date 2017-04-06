/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.provider;

import me.digi.sdk.core.Session;
import me.digi.sdk.core.CAContract;
import me.digi.sdk.core.config.ApiConfig;

import java.io.IOException;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpProvider {

    public static OkHttpClient client(SSLSocketFactory sslSocketFactory) {
        return providerBuilder(sslSocketFactory).build();
    }

    public static OkHttpClient client(Session session,
                                               CAContract contract,
                                               SSLSocketFactory sslSocketFactory) {
        return providerBuilder(session, contract, sslSocketFactory).build();
    }

    public static OkHttpClient client(OkHttpClient client,
                                               SSLSocketFactory sslSocketFactory) {
        if (client == null) {
            throw new IllegalArgumentException("Must provide a valid http client.");
        }

        return client.newBuilder().sslSocketFactory(sslSocketFactory).build();
    }

    public static OkHttpClient client(
            OkHttpClient client,
            Session session,
            CAContract contract,
            SSLSocketFactory sslSocketFactory) {
        if (session == null) {
            throw new IllegalArgumentException("Must provide a valid session.");
        }

        if (client == null) {
            throw new IllegalArgumentException("Must provide a valid http client.");
        }

        return client.newBuilder().sslSocketFactory(sslSocketFactory)
                .build();
    }

    public static OkHttpClient.Builder providerBuilder(SSLSocketFactory sslSocketFactory) {
        return new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory);
    }

    public static OkHttpClient.Builder providerBuilder(
            Session session, CAContract contract,
            SSLSocketFactory sslSocketFactory) {
        if (session == null) {
            throw new IllegalArgumentException("Must provide a valid session.");
        }

        return new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory);
    }

    //TODO move the UA params to the top of hierarchy
    private static OkHttpClient.Builder attachUserAgent(OkHttpClient.Builder builder) {
        return builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                final Request request = chain.request().newBuilder()
                        .header("User-Agent", ApiConfig.sdkUA("DigiMeSDK","1.0"))
                        .build();
                return chain.proceed(request);
            }
        });
    }

//TODO plug interceptors below
}