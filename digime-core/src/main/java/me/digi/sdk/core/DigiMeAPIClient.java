/*
 * Copyright © 2017 digi.me. All rights reserved.
 */


package me.digi.sdk.core;

import android.text.TextUtils;

import me.digi.sdk.core.config.ApiConfig;
import me.digi.sdk.core.service.CASessionService;
import me.digi.sdk.core.service.ConsentAccessService;
import me.digi.sdk.core.provider.OkHttpProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DigiMeAPIClient {
    private final Retrofit clientRetrofit;
    private final ConcurrentHashMap<Class, Object> registeredServices;

    public DigiMeAPIClient() {
        this(OkHttpProvider.client(
                /*sslfactory*/ null),
                new ApiConfig());
    }

    public DigiMeAPIClient(OkHttpClient client) {
        this(OkHttpProvider.client(
                client,
                /*sslfactory*/ null),
                new ApiConfig());
    }

    public DigiMeAPIClient(Session session) {
        this(OkHttpProvider.client(
                session,
                /*CAContract*/ null,
                /*sslfactory*/ null),
                new ApiConfig());
    }

    public DigiMeAPIClient(OkHttpClient client, Session session) {
        this(OkHttpProvider.client(
                client,
                session,
                /*CAContract*/ null,
                /*sslfactory*/ null),
                new ApiConfig());
    }

    DigiMeAPIClient(OkHttpClient client, ApiConfig apiConfig) {
        this.registeredServices = new ConcurrentHashMap<>();
        this.clientRetrofit = new Retrofit.Builder()
                .baseUrl(apiConfig.getUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    protected <T> T registerClass(Class<T> klas) {
        if (!registeredServices.contains(klas)) {
            registeredServices.putIfAbsent(klas, clientRetrofit.create(klas));
        }
        return (T)registeredServices.get(klas);
    }

    /*

        Public helper methods

     */

    public void getFiles(final SDKCallback<List<String>> callback) {

    }

    public void dataForFile(final String fileId, final SDKCallback<List<String>> callback) {

    }

    /*

        Exposed available services

     */

    public CASessionService sessionService() {
        return registerClass(CASessionService.class);
    }
    public ConsentAccessService consentAccessService() { return registerClass(ConsentAccessService.class); }



}