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
import java.lang.reflect.Array;
import java.net.CacheRequest;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

import me.digi.sdk.core.config.ApiConfig;
import me.digi.sdk.core.internal.Util;
import me.digi.sdk.core.session.CASessionManager;
import me.digi.sdk.core.session.SessionManager;
import okhttp3.CertificatePinner;
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
    private static volatile String[] contractIds;

    private static volatile boolean debugEnabled = BuildConfig.DEBUG;
    private static Context appContext;
    private static final Object SYNC = new Object();

    //Predefined <meta-data> paths where the sdk looks for necessary items
    public static final String APPLICATION_ID_PATH = "me.digi.sdk.AppId";
    public static final String APPLICATION_NAME_PATH = "me.digi.sdk.AppName";
    public static final String CONSENT_ACCESS_CONTRACTS_PATH = "me.digi.sdk.Contracts";

    private static final CASession defaultSession = new CASession("default");

    private final ConcurrentHashMap<CASession, DigiMeAPIClient> networkClients;
    private volatile CertificatePinner sslSocketFactory;

    SessionManager<CASession> consentAccessSessionManager;

    public final Flow<CAContract> flow;

    private DigiMeClient() {
        this.networkClients = new ConcurrentHashMap<CASession, DigiMeAPIClient>();

        this.flow = new Flow<CAContract>(new FlowLookupInitializer<CAContract>() {
            @Override
            public CAContract create(String identifier) {
                return new CAContract(identifier, DigiMeClient.getApplicationId());
            }
        });
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
        DigiMeClient.updatePropertiesFromMetadata(DigiMeClient.appContext);
        if ((applicationId == null) || (applicationId.length() == 0)) {
            throw new DigiMeException("Valid application ID must be set in manifest or by calling setApplicationId.");
        }

        clientInitialized = true;
        getInstance().onStart();

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

    /*
     *  DigiMeClient instance methods
     */

    public static DigiMeClient getInstance() {
        checkClientInitialized();
        if (singleton == null) {
            synchronized (DigiMeClient.class) {
                singleton = new DigiMeClient();
            }
        }
        return singleton;
    }

    protected void onStart(){
        consentAccessSessionManager = new CASessionManager();
    }

    public CertificatePinner getSSLSocketFactory() {
        checkClientInitialized();
        if (sslSocketFactory == null) {
            createSSLSocketFactory();
        }
        return sslSocketFactory;
    }

    private synchronized void createSSLSocketFactory() {
        if (sslSocketFactory == null) {
            CertificatePinner pinner = new CertificatePinner.Builder()
                    .add(new ApiConfig().getHost(), "sha256/dJtgu1DIYCnEB2vznevQ8hj9ADPRHzIN4pVG/xqP1DI=")
                    .add(new ApiConfig().getHost(), "sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=")
                    .add(new ApiConfig().getHost(), "sha256/Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=")
                    .build();
            this.sslSocketFactory = pinner;
        }
    }

    public SessionManager<CASession> getSessionManager() {
        checkClientInitialized();
        return consentAccessSessionManager;
    }

    public void createSession(SDKCallback<CASession>callback) throws DigiMeException {
        if (!flow.isInitialized()) {
            throw new DigiMeException("No contracts registered! You must have forgotten to add contract Id to the meta-data path \"%s\" or pass the CAContract object to createSession.", CONSENT_ACCESS_CONTRACTS_PATH);
        }
        createSession(flow.currentId, callback);
    }

    public void createSession(String contractId, SDKCallback<CASession>callback) throws DigiMeException {
        if (!flow.isInitialized()) {
            throw new DigiMeException("No contracts registered! You must have forgotten to add contract Id to the meta-data path \"%s\" or pass the CAContract object to createSession.", CONSENT_ACCESS_CONTRACTS_PATH);
        }
        CAContract contract = null;
        if (flow.stepTo(contractId)) {
            contract = flow.get();
        } else {
            if (Util.validateContractId(contractId) && DigiMeClient.debugEnabled) {
                throw new DigiMeException("Provided contractId has invalid format.");
            }
            contract = new CAContract(contractId, DigiMeClient.getApplicationId());
        }
        startSessionWithContract(contract, callback);
    }

    public void startSessionWithContract(CAContract contract, SDKCallback<CASession>callback) {
        DigiMeAPIClient client = getDefaultApiClient();
        client.sessionService().getSessionToken(contract).enqueue(new SessionForwardCallback(callback));
    }

    public DigiMeAPIClient getDefaultApiClient() {
        return getApiClient(defaultSession);
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

    private static void updatePropertiesFromMetadata(Context context) {
        if (context == null) {
            return;
        }
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
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
                applicationId = Util.digestStringWithLimit(appIdString, 7);

            } else if (appId instanceof Integer) {
                throw new DigiMeException(
                        "App ID must be placed in the strings manifest file");
            }
        }

        if (applicationName == null) {
            applicationName = ai.metaData.getString(APPLICATION_NAME_PATH);
        }

        if (contractIds == null) {
            Object contract = ai.metaData.get(CONSENT_ACCESS_CONTRACTS_PATH);
            if (contract instanceof String) {
                String cont = (String) contract;
                contractIds = new String[]{cont};
            } else if (contract instanceof Integer) {
                String type = context.getResources().getResourceTypeName((int) contract);
                if (type.equalsIgnoreCase("array")) {
                    String[] contracts = context.getResources().getStringArray((int)contract);
                    contractIds = contracts;
                } else if (type.equalsIgnoreCase("string")) {
                    String cnt = context.getResources().getString((int)contract);
                    contractIds = new String[]{cnt};
                } else {
                    throw new DigiMeException(
                            "Allowed types for contract ID are only string-array or string. Check that you have set the correct meta-data type.");
                }
            }
        }
    }

    public abstract class FlowLookupInitializer<T> {

        public abstract T create(String identifier);
    }

    public static final class Flow<T> {
        private String currentId;
        private int currentStep;
        private final ArrayList<String> identifiers;
        private final ConcurrentHashMap<String, T> lookup;

        private Flow() {
            this.lookup = new ConcurrentHashMap<>();
            this.identifiers = new ArrayList<>(Arrays.asList(DigiMeClient.contractIds));
            tryInit();
        }

        private Flow(FlowLookupInitializer<T> initializer) {
            this();
            if (this.isInitialized()) {
                for (String id :
                        identifiers) {
                    this.lookup.putIfAbsent(id, initializer.create(id));
                }
            }
        }

        private void tryInit() {
            if (identifiers == null) {
                currentStep = -1;
                currentId = null;
            } else if (identifiers.size() == 0) {
                currentStep = -1;
                currentId = null;
            } else {
                currentStep = 0;
                currentId = identifiers.get(0);
            }
        }

        public int getCurrentStep() {
            return currentStep;
        }

        public String getCurrentId() {
            return currentId;
        }

        public boolean isInitialized() {
            if (currentStep < 0 || currentId == null) {
                return false;
            }
            return true;
        }

        public boolean next() {
            if (identifiers == null) {
                return false;
            }
            if (currentStep + 1 >= identifiers.size()) {
                return false;
            }
            currentStep++;
            currentId = identifiers.get(currentStep);

            return true;
        }

        public T get() {
            if (!isInitialized()) { return null; };
            return (T)lookup.get(currentId);
        }

        public boolean stepTo(String identfier) {
            if (identfier == null) { return false; }
            if (identfier.equals(currentId)) { return true; }
            if (lookup.containsKey(identfier)) {
                int index = identifiers.indexOf(identfier);
                if (index >= 0) {
                    currentId = identfier;
                    currentStep = index;
                }
                return true;
            }
            return false;
        }

        public Flow rewind() {
            tryInit();
            return this;
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
            if (session == null) {
                callback.failed(new SDKException("Session create returned an empty session!"));
                return;
            }
            CASessionManager sm = (CASessionManager)consentAccessSessionManager;
            CASession oldSession = sm.getCurrentSession();
            sm.setCurrentSession(session);
            if (oldSession != null) {
                sm.dispatch.currentSessionChanged(sm.getCurrentSession(), session);
            }
            DigiMeClient.getInstance().getApiClient(session);
            if (callback != null) {
                callback.succeeded(new SDKResponse<>(session, result.response));
            }
        }

        @Override
        public void failed(SDKException exception) {

            callback.failed(exception);
        }
    }
}
