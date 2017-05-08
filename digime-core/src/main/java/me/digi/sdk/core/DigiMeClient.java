/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CacheRequest;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class DigiMeClient {
    public static final String TAG = "DigiMeCore";

    private static volatile DigiMeClient singleton;
    private static volatile Executor coreExecutor;
    private static volatile String applicationId;
    private static volatile String applicationName;

    private static volatile boolean debugEnabled = BuildConfig.DEBUG;
    private static Context appContext;
    private static final Object SYNC = new Object();

    public static final String APPLICATION_ID_PATH = "me.digi.sdk.AppId";
    public static final String APPLICATION_NAME_PATH = "me.digi.sdk.AppName";

    private final List<CAContract> appContracts;
    private final ConcurrentHashMap<CASession, DigiMeAPIClient> networkClients;
    private volatile SSLSocketFactory sslSocketFactory;

    private DigiMeClient() {
        this.appContracts = new ArrayList<CAContract>(2);
        this.networkClients = new ConcurrentHashMap<CASession, DigiMeAPIClient>();
    }

    private static Boolean clientInitialized = false;

    public static synchronized void init(
            final Context appContext) {
        if (clientInitialized) {
            return;
        }

        if (appContext == null) {
            throw new NullPointerException("appContext can not be null.");
        }

        DigiMeClient.appContext = appContext.getApplicationContext();
        DigiMeClient.fetchMetadata(DigiMeClient.getApplicationContext());
        if ((applicationId == null) || (applicationId.length() == 0)) {
            throw new DigiMeException("Valid application ID must be set in manifest or by calling setApplicationId.");
        }

        clientInitialized = true;
        getInstance();

        //Check if core app available

        FutureTask<Void> backgroundStartup =
                new FutureTask<>(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        getInstance().getSSLSocketFactory();

                        return null;
                    }
                });
        getCoreExecutor().execute(backgroundStartup);
    }

    public static Executor getCoreExecutor() {
        synchronized (SYNC) {
            if (DigiMeClient.coreExecutor == null) {
                DigiMeClient.coreExecutor = AsyncTask.THREAD_POOL_EXECUTOR;
            }
        }
        return DigiMeClient.coreExecutor;
    }


    public static void setCoreExecutor(Executor executor) {
        if (executor == null) {
            throw new NullPointerException("Executor can not be null.");
        }
        synchronized (SYNC) {
            DigiMeClient.coreExecutor = executor;
        }
    }

    public static void checkClientInitialized() {
        if (!DigiMeClient.isClientInitialized()) {
            throw new DigiMeClientException(" DigiMe Core Client has not been properly initialized. You need to call DigiMeClient.init().");
        }
    }

    public static synchronized boolean isClientInitialized() {
        return clientInitialized;
    }

    public static Context getApplicationContext() {
        checkClientInitialized();
        return appContext;
    }

    public static String digiMeSDKVersion() {
        return DigiMeVersion.VERSION;
    }

    public static String getApplicationId() {
        checkClientInitialized();
        return applicationId;
    }

    public static String getApplicationName() {
        checkClientInitialized();
        return applicationName;
    }

    public static void setApplicationName(String applicationName) {
        DigiMeClient.applicationName = applicationName;
    }


    public static DigiMeClient getInstance() {
        checkClientInitialized();
        if (singleton == null) {
            synchronized (DigiMeClient.class) {
                if (singleton == null) {
                    singleton = new DigiMeClient();
                }
            }
        }

        return singleton;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        checkClientInitialized();
        if (sslSocketFactory == null) {
            createSSLSocketFactory();
        }
        return sslSocketFactory;
    }

    private synchronized void createSSLSocketFactory() {
        if (sslSocketFactory == null) {
            X509TrustManager trustManager;
            SSLSocketFactory sslSocketFactory;

            try {
                trustManager = trustManagerForCertificates(getApplicationContext().getResources().openRawResource(R.raw.api_stagingdigi_me));
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustManager}, null);
                sslSocketFactory = sslContext.getSocketFactory();
            } catch (Exception e) {
                throw new DigiMeException(e);
            }
            this.sslSocketFactory = sslSocketFactory;
        }
    }

    public void startSession(CAContract contract, SDKCallback<CASession>callback) {
        DigiMeAPIClient client = new DigiMeAPIClient();
        client.sessionService().getSessionToken(contract).enqueue(new SessionForwardCallback(callback));
    }

    public DigiMeAPIClient getApiClient(CASession session) {
        checkClientInitialized();
        if (!networkClients.containsKey(session)) {
            networkClients.putIfAbsent(session, new DigiMeAPIClient(session));
        }
        return networkClients.get(session);
    }

    public void addCustomApiClient(CASession session, DigiMeAPIClient client) {
        checkClientInitialized();
        if (!networkClients.containsKey(session)) {
            networkClients.putIfAbsent(session, client);
        }
    }

    private static void fetchMetadata(Context context) {
        if (context == null) {
            return;
        }
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        if (ai == null || ai.metaData == null) {
            return;
        }

        if (applicationId == null) {
            Object appId = ai.metaData.get(APPLICATION_ID_PATH);
            if (appId instanceof String) {
                String appIdString = (String) appId;
                applicationId = appIdString;

            } else if (appId instanceof Integer) {
                throw new DigiMeException(
                        "App ID must be placed in the strings manifest file");
            }
        }

        if (applicationName == null) {
            applicationName = ai.metaData.getString(APPLICATION_NAME_PATH);
        }
    }


    //TODO: used the existing variant to make it build, there is a separate provider
    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    class SessionForwardCallback extends SDKCallback<CASession> {
        final SDKCallback<CASession> callback;

        SessionForwardCallback(SDKCallback<CASession> callback) {
            this.callback = callback;
        }

        @Override
        public void succeeded(SDKResponse<CASession> result) {
            final CASession session = result.body;
            if (callback != null) {
                callback.succeeded(new SDKResponse<CASession>(session, result.response));
            }
        }

        @Override
        public void failed(SDKException exception) {
            callback.failed(exception);
        }
    }
}
