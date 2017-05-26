/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import android.support.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Response;

public abstract class SDKCallback<T> implements retrofit2.Callback<T> {

    @Override
    public final void onResponse(@NonNull Call<T> call, @NonNull Response<T> response){
        if (response.isSuccessful()) {
            succeeded(new SDKResponse<>(response.body(), response));
        } else {
            failed(new DigiMeApiException(response));
        }
    }

    @Override
    public final void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        failed(new SDKException("Request Failure", t));
    }

    public abstract void succeeded(SDKResponse<T> result);
    public abstract void failed(SDKException exception);
}
