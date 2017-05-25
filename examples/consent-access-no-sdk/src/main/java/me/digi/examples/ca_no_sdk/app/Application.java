/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.app;

import com.digi.me.permission.BuildConfig;
import com.digi.me.permission.R;
import me.digi.examples.ca_no_sdk.service.PermissionService;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Application extends android.app.Application {
    private static final String PERMISSION_SERVICE_BASE_URL = "https://api.demo.sandboxdigi.me/v1/";

    private PermissionService permissionService;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public synchronized PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = new Retrofit.Builder()
                .baseUrl(PERMISSION_SERVICE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build()
                .create(PermissionService.class);
        }
        return permissionService;
    }

    private OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .build();

        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;

        try {
            trustManager = trustManagerForCertificates(getResources().openRawResource(R.raw.api_stagingdigi_me));
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES).readTimeout(50, TimeUnit.SECONDS)
            .connectionSpecs(Collections.singletonList(connectionSpec))
            .addInterceptor(logging);

        if (!BuildConfig.DEBUG) {
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        }

        return builder.build();
    }

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
}
