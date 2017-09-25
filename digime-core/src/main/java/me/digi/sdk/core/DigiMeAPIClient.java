/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import android.support.annotation.VisibleForTesting;

import com.google.gson.GsonBuilder;

import me.digi.sdk.core.config.ApiConfig;
import me.digi.sdk.core.config.DefaultApiConfig;
import me.digi.sdk.core.internal.network.CallConfigAdapterFactory;
import me.digi.sdk.core.service.ConsentAccessSessionService;
import me.digi.sdk.core.service.ConsentAccessService;
import me.digi.sdk.core.provider.OkHttpProvider;

import me.digi.sdk.core.session.CASession;
import me.digi.sdk.core.session.CASessionDeserializer;
import me.digi.sdk.crypto.CAKeyStore;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class DigiMeAPIClient {
    private Retrofit clientRetrofit;
    private final ConcurrentHashMap<Class, Object> registeredServices = new ConcurrentHashMap<>();


    public DigiMeAPIClient() {
        ApiConfig config = new DefaultApiConfig();
        createClient(OkHttpProvider.client(
                DigiMeClient.getInstance().getCertificatePinner(), config),
                config);
    }

    public DigiMeAPIClient(OkHttpClient client) {
        ApiConfig config = new DefaultApiConfig();
        createClient(OkHttpProvider.client(
                client,
                DigiMeClient.getInstance().getCertificatePinner(), config),
                config);
    }

    public DigiMeAPIClient(OkHttpClient client, ApiConfig config) {
        createClient(OkHttpProvider.client(
                client,
                DigiMeClient.getInstance().getCertificatePinner(), config),
                config);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public DigiMeAPIClient(boolean attachInterceptor, CAKeyStore keyStore, ApiConfig config) {
        createClient(OkHttpProvider.client(attachInterceptor,
                null, keyStore, config),
                config);
    }

    private void createClient(OkHttpClient client, ApiConfig apiConfig) {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(CASession.class, new CASessionDeserializer());
        this.clientRetrofit = new Retrofit.Builder()
                .addCallAdapterFactory(CallConfigAdapterFactory.create())
                .client(client)
                .baseUrl(apiConfig.getUrl())
                .addConverterFactory(GsonConverterFactory.create(gson.create()))
                .build();
    }

    private <T> T registerClass(Class<T> klas) {
        if (!registeredServices.contains(klas)) {
            registeredServices.putIfAbsent(klas, clientRetrofit.create(klas));
        }
        return (T)registeredServices.get(klas);
    }

    /**
     * Exposed available services
     */

    public ConsentAccessSessionService sessionService() {
        return registerClass(ConsentAccessSessionService.class);
    }

    public ConsentAccessService consentAccessService() {
        return registerClass(ConsentAccessService.class);
    }



}