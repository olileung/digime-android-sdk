/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core.service;

import me.digi.sdk.core.CAContract;

import me.digi.sdk.core.session.CASession;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ConsentAccessSessionService {
    @Headers({
            "content-type: application/json",
            "Cache-Control: no-cache"
    })
//    @POST("permissionaccess/v1/session/create")
    @POST("v1/permission-access/session")
    Call<CASession> getSessionToken(@Body CAContract contract);
}
