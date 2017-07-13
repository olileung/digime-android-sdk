/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import com.google.gson.GsonBuilder;

import me.digi.sdk.core.config.ApiConfig;
import me.digi.sdk.core.internal.network.CallConfigAdapterFactory;
import me.digi.sdk.core.service.ConsentAccessSessionService;
import me.digi.sdk.core.service.ConsentAccessService;
import me.digi.sdk.core.provider.OkHttpProvider;

import me.digi.sdk.core.session.CASession;
import me.digi.sdk.core.session.CASessionDeserializer;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class DigiMeAPIClient {
    private final Retrofit clientRetrofit;
    private final ConcurrentHashMap<Class, Object> registeredServices;


    public DigiMeAPIClient() {
        this(OkHttpProvider.client(
                DigiMeClient.getInstance().getCertificatePinner()),
                new ApiConfig());
    }

    public DigiMeAPIClient(OkHttpClient client) {
        this(OkHttpProvider.client(
                client,
                DigiMeClient.getInstance().getCertificatePinner()),
                new ApiConfig());
    }

    public DigiMeAPIClient(CASession session) {
        this(OkHttpProvider.client(
                session,
                DigiMeClient.getInstance().getCertificatePinner()),
                new ApiConfig());
    }

    public DigiMeAPIClient(OkHttpClient client, CASession session) {
        this(OkHttpProvider.client(
                client,
                session,
                DigiMeClient.getInstance().getCertificatePinner()),
                new ApiConfig());
    }

    private DigiMeAPIClient(OkHttpClient client, ApiConfig apiConfig) {
        this.registeredServices = new ConcurrentHashMap<>();

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