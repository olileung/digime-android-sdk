/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.internal.network;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.config.NetworkConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static me.digi.sdk.core.SDKCallback.TIMEOUT_ERROR;

public class ProxiedCallback<T> implements Callback<T> {
    private final BackOffTimer backOffTimer;
    private final Call<T> proxiedCall;
    private final Callback<T> registeredCallback;
    private final ScheduledExecutorService callbackExecutor;
    private final NetworkConfig networkConfig;
    private final int triesAlready;

    ProxiedCallback(Call<T> call, Callback<T> delegate, ScheduledExecutorService executor, NetworkConfig config) {
        this(call, delegate, executor, config, 0);
    }

    private ProxiedCallback(Call<T> call, Callback<T> delegate, ScheduledExecutorService executor, NetworkConfig config, int retries) {
        this.proxiedCall = call;
        this.registeredCallback = delegate;
        this.callbackExecutor = executor;
        this.networkConfig = config;
        this.triesAlready = retries;
        this.backOffTimer = config.shouldPerformExponentialBackoff() ? new BackOffTimer((int)config.getMinDelay()) : null;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (!response.isSuccessful()) {
            long nextDelay = backOffTimer != null ? backOffTimer.calculateNextBackOffMillis() : 100;
            if (nextDelay == BackOffTimer.STOP) {
                registeredCallback.onResponse(call, response);
            } else if (triesAlready < networkConfig.getMaxRetries() && isRetryRequired(networkConfig, response.code())) {
                scheduleCall(nextDelay);
            } else {
                registeredCallback.onResponse(call, response);
            }
        } else {
            registeredCallback.onResponse(call, response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        long nextDelay = backOffTimer != null ? backOffTimer.calculateNextBackOffMillis() : 100;
        if (nextDelay == BackOffTimer.STOP) {
            registeredCallback.onFailure(call, new SDKException("Connection timeout", t, TIMEOUT_ERROR));
        } else if (triesAlready < networkConfig.getMaxRetries()) {
            scheduleCall(nextDelay);
        } else {
            registeredCallback.onFailure(call, new SDKException("Connection timeout", t, TIMEOUT_ERROR));
        }
    }

    private boolean isRetryRequired(NetworkConfig config, int statusCode) {
        if (config.getAlwaysOnCodes() != null && configContainsCode(config, statusCode)) {
            return true;
        }
        if (statusCode >= 500 || statusCode < 600) {
            return true;
        }
        return false;
    }

    private boolean configContainsCode(NetworkConfig config, int statusCode) {
        if (config.getAlwaysOnCodes() == null || config.getAlwaysOnCodes().length == 0)  return false;
        for (int code: config.getAlwaysOnCodes()) {
            if (code == statusCode) return true;
        }
        return false;
    }

    private void scheduleCall(long delay) {
        callbackExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                final Call<T> call = proxiedCall.clone();
                call.enqueue(new ProxiedCallback<T>(call, registeredCallback, callbackExecutor, networkConfig, triesAlready + 1));
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
