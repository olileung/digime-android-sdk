/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import me.digi.sdk.core.session.CASession;
import me.digi.sdk.crypto.CAKeyStore;
import me.digi.sdk.core.testentities.TestApiConfig;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class HTTPErrorTest {
    private static final String APP_ID_ERROR_MESSAGE = "This app is no longer valid for Consent Access";

    private static MockWebServer server;
    private static DigiMeAPIClient client;

    @BeforeClass
    public static void startUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new DigiMeAPIClient(false, new CAKeyStore(""), new TestApiConfig(server.url("/")));
    }

    @Test
    public void v2ErrorTest() throws IOException, InterruptedException {
        final String response = "{\"error\":{\"code\":\"InvalidConsentAccessApplication\",\"message\":\"Application is not valid for Consent Access\",\"reference\":\"GUID-GUID-GUID\"}}";
        final ResultWrapper wrapper = new ResultWrapper();

        MockResponse mockResponse = new MockResponse().setBody(response)
                                                .setResponseCode(403);
        server.enqueue(mockResponse);

        assertTrue(executeAsyncCallback(wrapper).await(2500, TimeUnit.MILLISECONDS));
        assertFalse(wrapper.succeedIndicator);
        assertEquals(APP_ID_ERROR_MESSAGE, wrapper.result);
    }

    @Test
    public void headerErrorTest() throws IOException, InterruptedException {
        final ResultWrapper wrapper = new ResultWrapper();

        MockResponse mockResponse = new MockResponse()
                .setBody("")
                .addHeader("X-Error-Code", "InvalidConsentAccessApplication")
                .addHeader("X-Error-Message", "Application is not valid for Consent Access")
                .setResponseCode(403);
        server.enqueue(mockResponse);

        assertTrue(executeAsyncCallback(wrapper).await(2500, TimeUnit.MILLISECONDS));
        assertFalse(wrapper.succeedIndicator);
        assertEquals(APP_ID_ERROR_MESSAGE, wrapper.result);
    }

    @Test
    public void v2ErrorFallthroughTest() throws IOException, InterruptedException {
        final String response = "{\"error\":{\"code\":\"SomeOtherExceptionCode\",\"message\":\"Application is not valid for Consent Access\",\"reference\":\"GUID-GUID-GUID\"}}";
        final ResultWrapper wrapper = new ResultWrapper();

        MockResponse mockResponse = new MockResponse().setBody(response)
                .setResponseCode(403);
        server.enqueue(mockResponse);

        assertTrue(executeAsyncCallback(wrapper).await(2500, TimeUnit.MILLISECONDS));
        assertFalse(wrapper.succeedIndicator);
        assertEquals("/v1/permission-access/session unsuccessful - Application is not valid for Consent Access (403).", wrapper.result);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.shutdown();
    }

    private CountDownLatch executeAsyncCallback(final ResultWrapper wrapper) {
        CAContract mockContract = new CAContract("dummyId", "me.digi.sdk.test");
        final CountDownLatch latch = new CountDownLatch(1);
        client.sessionService().getSessionToken(mockContract).enqueue(new SDKCallback<CASession>() {
            @Override
            public void succeeded(SDKResponse<CASession> result) {
                wrapper.succeedIndicator = true;
                latch.countDown();
            }

            @Override
            public void failed(SDKException exception) {
                wrapper.succeedIndicator = false;
                wrapper.result = exception.getMessage();
                latch.countDown();
            }
        });
        return latch;
    }

    private class ResultWrapper {
        boolean succeedIndicator;
        String result;
    }

}
