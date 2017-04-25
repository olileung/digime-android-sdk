/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.service;

import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface ConsentAccessService {

    @Headers({
            "Content-type: application/json",
            "Cache-Control: no-cache"
    })
    @GET("/v1/permission-access/query/{sessionKey}")
    Call<CAFiles> list(@Path("sessionKey") String sessionKey);

    @Headers({
            "Content-type: application/json",
            "Cache-Control: no-cache"
    })
    @GET("/v1/permission-access/query/{sessionKey}/{fileName}")
    Call<CAFileResponse> data(@Path("sessionKey") String sessionKey,
                              @Path("fileName") String fileName);
}
