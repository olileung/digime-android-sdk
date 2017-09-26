/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.app;

import android.content.res.AssetManager;
import android.net.Uri;

import me.digi.examples.ca_no_sdk.BuildConfig;
import me.digi.examples.ca_no_sdk.service.PermissionService;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Application extends android.app.Application {
    private static final String PERMISSION_SERVICE_BASE_URL = BuildConfig.BASE_HOST;
    private static final String CERTIFICATE_ASSETS_PATH = "certificates";

    private PermissionService permissionService;

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

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES).readTimeout(50, TimeUnit.SECONDS)
            .connectionSpecs(Collections.singletonList(connectionSpec))
            .addInterceptor(logging);

        CertificatePinner.Builder pinBuilder = new CertificatePinner.Builder();
        String host = Uri.parse(PERMISSION_SERVICE_BASE_URL).getHost();
        for (X509Certificate certificate : pinningCertificates()) {
            pinBuilder.add(host, CertificatePinner.pin(certificate));
        }

        return builder.certificatePinner(pinBuilder.build()).build();
    }

    public Collection<X509Certificate> pinningCertificates() {
        Collection<X509Certificate> certificates = new ArrayList<>();
        try {
            AssetManager assetManager = getAssets();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            for (String file : assetManager.list(CERTIFICATE_ASSETS_PATH)) {
                InputStream in = assetManager.open(CERTIFICATE_ASSETS_PATH + "/" + file);
                for (X509Certificate certificate : (Collection<X509Certificate>)certificateFactory.generateCertificates(in)) {
                    certificates.add(certificate);
                }
            }
        } catch (IOException | CertificateException ex) {
            throw new IllegalStateException("Failed to load pinning certificates", ex);
        }

        return certificates;
    }
}
