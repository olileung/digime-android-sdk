/*
 * Copyright © 2017 digi.me. All rights reserved.
 */


package me.digi.sdk.core.service;

import me.digi.sdk.core.CAContract;

import me.digi.sdk.core.CASession;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface ConsentAccessSessionService {

    @Headers({
            "content-type: application/json",
            "Cache-Control: no-cache"
    })
    @POST("permission-access/session")
    //TODO replace ResponseBody with serialized CASession
    Call<CASession> getSessionToken(@Body CAContract contract);
}
