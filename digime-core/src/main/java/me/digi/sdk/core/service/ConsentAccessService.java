/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.service;

import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.internal.network.CallConfig;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface ConsentAccessService {

    @CallConfig(shouldRetry = true, retryCount = 3)
    @Headers({
            "Content-type: application/json",
            "Cache-Control: no-cache"
    })
    @GET("/v1/permission-access/query/{sessionKey}")
    Call<CAFiles> list(@Path("sessionKey") String sessionKey);

    @CallConfig(shouldRetry = true, retryCount = 3, retryOnResponseCode = {404})
    @Headers({
            "Content-type: application/json",
            "Cache-Control: no-cache"
    })
    @GET("/v1/permission-access/query/{sessionKey}/{fileName}")
    Call<CAFileResponse> data(@Path("sessionKey") String sessionKey,
                              @Path("fileName") String fileName);
}
