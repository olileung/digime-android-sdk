/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.internal.network;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.config.NetworkConfig;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CallConfigAdapterFactory extends CallAdapter.Factory {
    private final ScheduledExecutorService callbackExecutor = null;

    public CallConfigAdapterFactory() {
//        callbackExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public static CallConfigAdapterFactory create() {
        return new CallConfigAdapterFactory();
    }

    @Override
    public CallAdapter<?, ?> get(final Type returnType, Annotation[] annotations, Retrofit retrofit) {
        boolean hasConfig = false;
        boolean shouldRetry = false;
        boolean hasRetryCodes = false;
        NetworkConfig config = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof CallConfig) {
                hasConfig = true;
                CallConfig ant = ((CallConfig) annotation);
                boolean expBackoff = DigiMeClient.retryWithExponentialBackoff ? ant.doExponentialBackoff() : DigiMeClient.retryWithExponentialBackoff;
                int retryCount = DigiMeClient.maxRetryCount == 0 ? ant.retryCount() : DigiMeClient.maxRetryCount;
                shouldRetry = ant.shouldRetry();

                config = new NetworkConfig(retryCount, DigiMeClient.minRetryPeriod, ant.retryOnResponseCode(), expBackoff);
                hasRetryCodes = ant.retryOnResponseCode().length > 0;
            }
        }

        final boolean shouldRetryCall = ((hasConfig && DigiMeClient.retryOnFail) && shouldRetry) || hasRetryCodes;
        final NetworkConfig callConfigWrapper = config;
        final CallAdapter<Object, Call<?>> delegate = (CallAdapter<Object, Call<?>>)retrofit.nextCallAdapter(this, returnType, annotations);
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return delegate.responseType();
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return (Call<Object>) delegate.adapt(shouldRetryCall ? new ConfigurableCall<>(call, callbackExecutor, callConfigWrapper) : call);
            }
        };
    }

    private static final class ConfigurableCall<T> implements Call<T> {
        private final Call<T> proxiedCall;
        private final ScheduledExecutorService callbackExecutor;
        private final NetworkConfig networkConfig;

        ConfigurableCall(Call<T> delegate, ScheduledExecutorService executor, NetworkConfig config) {
            proxiedCall = delegate;
            callbackExecutor = executor;
            networkConfig = config;
        }

        @Override
        public Response<T> execute() throws IOException {
            return proxiedCall.execute();
        }

        @Override
        public void enqueue(Callback<T> callback) {
            proxiedCall.enqueue(new ProxiedCallback<T>(proxiedCall, callback, callbackExecutor, networkConfig));
        }

        @Override
        public void cancel() {
            proxiedCall.cancel();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        @Override public Call<T> clone() {
            return new ConfigurableCall<>(proxiedCall.clone(), callbackExecutor, networkConfig);
        }

        @Override public boolean isExecuted() {
            return proxiedCall.isExecuted();
        }

        @Override public boolean isCanceled() {
            return proxiedCall.isCanceled();
        }

        @Override public Request request() {
            return proxiedCall.request();
        }
    }
}