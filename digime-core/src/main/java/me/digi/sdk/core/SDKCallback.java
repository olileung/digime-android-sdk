package me.digi.sdk.core;

import retrofit2.Call;
import retrofit2.Response;

public abstract class SDKCallback<T> implements retrofit2.Callback<T> {
    @Override
    public final void onResponse(Call<T> call, Response<T> response){
        if (response.isSuccessful()) {
            succeeded(new SDKResponse<>(response.body(), response));
        } else {
            failed(new DigiMeApiException(response));
        }
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        failed(new SDKException("Request Failure", t));
    }

    public abstract void succeeded(SDKResponse<T> result);
    public abstract void failed(SDKException exception);
}
