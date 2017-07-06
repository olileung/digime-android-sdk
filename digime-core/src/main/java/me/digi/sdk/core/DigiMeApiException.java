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
    private final Response response;

    public DigiMeApiException(Response response) {
        this(response, readResponseBody(response), response.code());
    }

    private DigiMeApiException(Response response, HTTPError concreteError,
                               int responseCode) {
        super(messageForCode(response, concreteError, responseCode));
        this.concreteError = concreteError;
        this.response = response;
        this.code = responseCode;
    }

    public String getErrorString() {
        return concreteError == null ? null : concreteError.error;
    }

    public Response getResponse() {
        return response;
    }

    private static HTTPError readResponseBody(Response response) {
        try {
            @SuppressWarnings("ConstantConditions") final String body = response.errorBody().source().buffer().clone().readUtf8();
            if (!TextUtils.isEmpty(body)) {
                return parseResponse(body);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HTTPError("Request failed", 501, null);
    }

    private static HTTPError parseResponse(String body) {
        final Gson gson = new Gson();
        try {
            return gson.fromJson(body, HTTPError.class);
        } catch (JsonSyntaxException e) {
            return new HTTPError("Request failed", 501, null);
        }
    }

    private static String messageForCode(Response resp, HTTPError error, int code) {
        String reason = "error code";
        if (error != null) {
            if (error.error != null) {
                reason = error.error;
            } else if (error.message != null) {
                reason = error.message;
            }
        }
        return String.format(resp.raw().request().url().encodedPath() + " unsuccessful - %s (%s).", reason, String.valueOf(code));
    }
}
