/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import me.digi.sdk.core.entities.HTTPError;

import retrofit2.Response;

public class DigiMeApiException extends SDKException {

    private final HTTPError concreteError;
    private final int responseCode;
    private final Response response;

    public DigiMeApiException(Response response) {
        this(response, readResponseBody(response), response.code());
    }

    DigiMeApiException(Response response, HTTPError concreteError,
                       int responseCode) {
        super(messageForCode(response, concreteError, responseCode));
        this.concreteError = concreteError;
        this.response = response;
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getErrorString() {
        return concreteError == null ? null : concreteError.error;
    }

    public Response getResponse() {
        return response;
    }

    public static HTTPError readResponseBody(Response response) {
        try {
            final String body = response.errorBody().source().buffer().clone().readUtf8();
            if (!TextUtils.isEmpty(body)) {
                return parseResponse(body);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //TODO log this somehow
        }

        return null;
    }

    static HTTPError parseResponse(String body) {
        final Gson gson = new Gson();
        try {
            final HTTPError error = gson.fromJson(body, HTTPError.class);
            return error;
        } catch (JsonSyntaxException e) {
            //TODO log this somehow
        }
        return null;
    }

    static String messageForCode(Response resp, HTTPError error, int code) {
        return String.format(resp.raw().request().url().encodedPath() + " unsuccessful - %s (%s).", error != null ? error.error : "General error", String.valueOf(code));
    }
}
