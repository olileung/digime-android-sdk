/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;


import me.digi.sdk.core.config.ApiConfig;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.internal.Util;
import me.digi.sdk.core.session.CASessionManager;
import me.digi.sdk.core.session.SessionListener;
import me.digi.sdk.core.session.SessionManager;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;


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

    private static CASession defaultSession;
    private final List<SDKListener> listeners = new CopyOnWriteArrayList<>();

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
        defaultSession = new CASession("default", 0, "default", null);

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

    protected void onStart(){
        consentAccessSessionManager = new CASessionManager();
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

    public CertificatePinner getSSLSocketFactory() {
        checkClientInitialized();
        if (sslSocketFactory == null) {
            createSSLSocketFactory();
        }
        return sslSocketFactory;
    }

    public SessionManager<CASession> getSessionManager() {
        checkClientInitialized();
        return consentAccessSessionManager;
    }

    public void addSessionListener(final SDKListener listener) {

        synchronized (DigiMeClient.class) {
            this.listeners.add(listener);
        }
    }

    public boolean removeSessionListener(final SDKListener listener) {

        boolean removed;
        synchronized (DigiMeClient.class) {
            removed = this.listeners.remove(listener);
        }
        return removed;
    }

    /**
     *  Public methods
     */

    public DigiMeAuthorizationManager authorize(Activity activity, SDKCallback<CASession> callback) {
        return authorizeInitializedSession(activity, callback);
    }

    public DigiMeAuthorizationManager authorizeInitializedSession(Activity activity, SDKCallback<CASession> callback) {
        checkClientInitialized();
        DigiMeAuthorizationManager mgr = new DigiMeAuthorizationManager();
        mgr.beginAutorization(activity, callback);
        return mgr;
    }

    public DigiMeAuthorizationManager authorizeInitializedSession(CASession session, Activity activity, SDKCallback<CASession> callback) {
        checkClientInitialized();
        DigiMeAuthorizationManager mgr = new DigiMeAuthorizationManager(DigiMeClient.getApplicationId(), session);
        mgr.beginAutorization(activity, callback);
        return mgr;
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

    public void startSessionWithContract(CAContract contract, SDKCallback<CASession> callback) {
        DigiMeAPIClient client = getDefaultApi();
        client.sessionService().getSessionToken(contract).enqueue(new SessionForwardCallback(callback));
    }

    public void getFileList(SDKCallback<CAFiles> callback) {
        checkClientInitialized();
        if (getSessionManager().getCurrentSession() == null) {
            callback.failed(new SDKException("Current session is null"));
            return;
        }
        getApi().consentAccessService().list(getSessionManager().getCurrentSession().sessionKey).enqueue(callback);
    }

    public void getFileListWithSession(CASession session, SDKCallback<CAFiles> callback) {
        checkClientInitialized();
        if (session == null) {
            throw new IllegalArgumentException("Session can not be null.");
        }
        getApi().consentAccessService().list(session.sessionKey).enqueue(callback);
    }

    public void getFileContent(String fileId, SDKCallback<CAFileResponse> callback) {
        checkClientInitialized();
        if (getSessionManager().getCurrentSession() == null) {
            callback.failed(new SDKException("Current session is null"));
            return;
        }
        if (fileId == null) {
            throw new IllegalArgumentException("File ID can not be null.");
        }
        getApi().consentAccessService().data(getSessionManager().getCurrentSession().sessionKey, fileId).enqueue(callback);
    }

    public void getFileContentWithSession(String fileId, CASession session, SDKCallback<CAFileResponse> callback) {
        checkClientInitialized();
        if (session == null) {
            throw new IllegalArgumentException("Session can not be null.");
        }
        if (fileId == null) {
            throw new IllegalArgumentException("File ID can not be null.");
        }
        getApi().consentAccessService().data(session.sessionKey, fileId).enqueue(callback);
    }

    public DigiMeAPIClient getDefaultApi() {
        return getApi(defaultSession);
    }

    public DigiMeAPIClient getApi() {
        checkClientInitialized();
        final CASession session = consentAccessSessionManager.getCurrentSession();
        if (session == null) {
            return null;
        }
        return getApi(session);
    }

    public DigiMeAPIClient getApi(CASession session) {
        checkClientInitialized();
        if (!networkClients.containsKey(session)) {
            networkClients.putIfAbsent(session, new DigiMeAPIClient(session));
        }
        return networkClients.get(session);
    }

    public DigiMeAPIClient addCustomClient(OkHttpClient client) {
        checkClientInitialized();
        final CASession session = consentAccessSessionManager.getCurrentSession();
        if (session == null) {
            return null;
        }
        return addCustomClient(session, client);
    }

    public DigiMeAPIClient addCustomClient(CASession session, OkHttpClient client) {
        checkClientInitialized();
        DigiMeAPIClient apiClient;
        if (client == null) {
            apiClient = new DigiMeAPIClient(session);
        } else {
            apiClient = new DigiMeAPIClient(client, session);
        }
        return networkClients.put(session, apiClient);
    }

    /**
     *  Private helpers
     */

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

    /**
     *  Iterator for pre-registered CAContract flow
     *
     */

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

    /**
     *  Callback wrappers
     */


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
            sm.setCurrentSession(session);
            getInstance().getApi(session);
            if (callback != null) {
                callback.succeeded(new SDKResponse<>(session, result.response));
            }
            synchronized (DigiMeClient.class) {
                Iterator<SDKListener> iter = listeners.iterator();
                while (iter.hasNext()) { iter.next().sessionCreated(session); }
            }
        }

        @Override
        public void failed(SDKException exception) {
            if (callback != null) {
                callback.failed(exception);
            }
            synchronized (DigiMeClient.class) {
                Iterator<SDKListener> iter = listeners.iterator();
                while (iter.hasNext()) { iter.next().sessionCreateFailed(exception); }
            }
        }
    }

    class AuthorizationForwardCallback extends SDKCallback<CASession> {
        final SDKCallback<CASession> callback;

        AuthorizationForwardCallback(SDKCallback<CASession> callback) {
            this.callback = callback;
        }

        @Override
        public void succeeded(SDKResponse<CASession> result) {
            if (result.body == null) {
                callback.failed(new SDKException("Session create returned an empty session!"));
                return;
            }
            if (callback != null) {
                callback.succeeded(result);
            }
            synchronized (DigiMeClient.class) {
                Iterator<SDKListener> iter = listeners.iterator();
                while (iter.hasNext()) { iter.next().authorizeSucceeded(result.body); }
            }
        }

        @Override
        public void failed(SDKException exception) {
            if (callback != null) {
                callback.failed(exception);
            }
            synchronized (DigiMeClient.class) {
                Iterator<SDKListener> iter = listeners.iterator();
                while (iter.hasNext()) { iter.next().authorizeDenied(null, exception); }
            }
        }
    }
}
