/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import android.text.TextUtils;

import com.google.gson.Gson;
import me.digi.sdk.core.entities.HTTPError;

import okhttp3.Headers;
import retrofit2.Response;

public class DigiMeApiException extends SDKException {

    private final HTTPError concreteError;
    private final Response response;

    private static final String APP_NOT_VALID_MESSAGE = "This app is no longer valid for Consent Access";
    private static final String HEADER_ERROR_CODE = "X-Error-Code";
    private static final String HEADER_ERROR_MESSAGE = "X-Error-Message";
    private static final String HEADER_CODE_APP_NOT_VALID = "InvalidConsentAccessApplication";

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
        HTTPError finalError = null;
        try {
            @SuppressWarnings("ConstantConditions") final String body = response.errorBody().source().buffer().clone().readUtf8();
            if (!TextUtils.isEmpty(body)) {
                finalError = parseResponse(body, response.code());
            }
            if (finalError != null) {
                return finalError;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response.headers().get(HEADER_ERROR_CODE) != null) {
            return parseHeaders(response.headers(), response.code());
        }

        return new HTTPError("Request failed", response.code(), null);
    }

    private static HTTPError parseResponse(String body, int code) {
        HTTPError parsedError;
        final Gson gson = new Gson();
        try {
            parsedError = gson.fromJson(body, HTTPError.class);
            parsedError.code = code;
        } catch (Exception e) {
            parsedError = null;
        }
        if (parsedError == null) {
            try {
                HTTPError.HTTPErrorV2 otherError = gson.fromJson(body, HTTPError.HTTPErrorV2.class);
                parsedError = new HTTPError(otherError, code);
            } catch (Exception e) {
                parsedError = null;
            }
        }
        return parsedError;
    }

    private static HTTPError parseHeaders(Headers headers, int code) {
        return new HTTPError(headers.get(HEADER_ERROR_CODE), code, headers.get(HEADER_ERROR_MESSAGE));
    }

    private static String messageForCode(Response resp, HTTPError error, int code) {
        String reason = "error code";
        if (error != null) {
            if (error.error.equalsIgnoreCase(HEADER_CODE_APP_NOT_VALID)) {
                return APP_NOT_VALID_MESSAGE;
            }
            if (error.message != null) {
                reason = error.message;
            } else if (error.error != null) {
                reason = error.error;
            }
        }
        return String.format(resp.raw().request().url().encodedPath() + " unsuccessful - %s (%s).", reason, String.valueOf(code));
    }
}
