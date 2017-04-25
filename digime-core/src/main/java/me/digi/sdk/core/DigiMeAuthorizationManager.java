/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import me.digi.sdk.core.session.SessionManager;

public class DigiMeAuthorizationManager {
    private static final String KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN";
    private static final String KEY_APP_ID = "KEY_APP_ID";
    private static final String PERMISSION_ACCESS_INTENT_ACTION = "android.intent.action.DIGI_PERMISSION_REQUEST";
    private static final String PERMISSION_ACCESS_INTENT_TYPE = "text/plain";
    private static final String DIGI_ME_PACKAGE_ID = "me.digi.app";
    private static final int REQUEST_CODE = 762;

    static final AtomicReference<Boolean> authInProgress = new AtomicReference<>(null);
    private SDKCallback<CASession> callback;

    private final String appId;
    private CASession session;
    private final SessionManager<CASession> sManager;

    public void onActivityResult(int requestCode, int resultCode, Listener listener) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //callback.succeeded(new SDKResponse<CASession>(this.session, null));
                listener.permissionGranted();
            } else {
                listener.permissionDeclined();
            }
            authInProgress.set(null);
            return;
        }
        listener.incorrectRequestCode();
    }

    public DigiMeAuthorizationManager() {
        this(DigiMeClient.getApplicationId(), DigiMeClient.getInstance().getSessionManager());
    }

    public DigiMeAuthorizationManager(String applicationId, SessionManager<CASession> manager) {
        this.appId = applicationId;
        this.sManager = manager;
    }

    public DigiMeAuthorizationManager(String appId, CASession session) {
        this.appId = appId;
        this.session = session;
        this.sManager = null;

    }

    public void beginAutorization(Activity activity, SDKCallback<CASession> callback) {
        if (activity == null) {
            throw new IllegalArgumentException("Must set the activity to start the flow.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Must set the callback.");
        }
        if (!activity.isFinishing()) {
            prepareRequest(activity, callback);
        }
    }

    private boolean prepareRequest(Activity activity, SDKCallback<CASession> callback) {
        if (session == null) {
            CASession session = sManager.getCurrentSession();
            if (session == null || session.getSessionKey() == null) {
                throw new NullPointerException("Session is null.");
            }
            this.session = session;
        }
        if (!sendRequest(activity, callback)) {
            callback.failed(new DigiMeAuthorizationException("Failed to authorize session"));
        }

        return true;
    }

    private boolean sendRequest(Activity activity, SDKCallback<CASession> callback) {
        if (!markInProgress()) {
            return false;
        }
        this.callback = callback;
        Intent sendIntent = new Intent();
        sendIntent.setAction(PERMISSION_ACCESS_INTENT_ACTION);
        sendIntent.putExtra(KEY_SESSION_TOKEN, session.getSessionKey());
        sendIntent.putExtra(KEY_APP_ID, appId);
        sendIntent.setType(PERMISSION_ACCESS_INTENT_TYPE);

        if (verifyIntentCanBeHandled(sendIntent, activity.getPackageManager())) {
            activity.startActivityForResult(sendIntent, REQUEST_CODE);
        } else {
            startInstallDigiMeFlow(activity);
        }
        return true;
    }

    public int getRequestCode() {
        return REQUEST_CODE;
    }

    private boolean verifyIntentCanBeHandled(Intent intent, PackageManager packageManager) {
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }

    private void startInstallDigiMeFlow(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + DIGI_ME_PACKAGE_ID)));
        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + DIGI_ME_PACKAGE_ID)));
        }
        Toast.makeText(activity, "digi.me not found.", Toast.LENGTH_LONG).show();
    }

    private boolean markInProgress() {
        boolean result = false;
        if (isInProgress()) {
            Log.d(DigiMeClient.TAG, "Consent Access authorization is already in progress.");
        } else {
            result = authInProgress.compareAndSet(null, Boolean.TRUE);
            if (!result) {
                Log.d(DigiMeClient.TAG, "Consent Access authorization is already in progress.");
            }
        }
        return result;
    }

    public boolean isInProgress() {
        return authInProgress.get() != null;
    }

    public class DigiMeAuthorizationException extends SDKException {

        public DigiMeAuthorizationException(String message) {
            super(message);
        }

        public DigiMeAuthorizationException(String message, Throwable throwable) { super(message, throwable); }

    }

    public interface Listener {
        void permissionGranted();
        void permissionDeclined();
        void incorrectRequestCode();
    }
}
