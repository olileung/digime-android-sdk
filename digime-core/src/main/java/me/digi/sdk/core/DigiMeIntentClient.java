/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.sdk.core;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import java.util.List;

public class DigiMeIntentClient {
    private static final String KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN";
    private static final String KEY_APP_ID = "KEY_APP_ID";
    private static final String PERMISSION_ACCESS_INTENT_ACTION = "android.intent.action.DIGI_PERMISSION_REQUEST";
    private static final String PERMISSION_ACCESS_INTENT_TYPE = "text/plain";
    private static final String DIGI_ME_PACKAGE_ID = "me.digi.app";
    private static final int REQUEST_CODE = 762;

    private final String appId;
    private final String contractId;
    private final String sessionToken;

    public void onActivityResult(int requestCode, int resultCode, Listener listener) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                listener.permissionGranted();
            } else {
                listener.permissionDeclined();
            }
        }
        listener.incorrectRequestCode();
    }

    public DigiMeIntentClient(String appId, String contractId, String sessionToken) {
//        if (TextUtils.isEmpty(appId)) throw new RuntimeException("appId cannot be empty");
//        if (TextUtils.isEmpty(contractId)) throw new RuntimeException("contractId cannot be empty"); TODO these along with other exceptions will be moved into the DigiMeClient object.
//        if (TextUtils.isEmpty(sessionToken)) throw new RuntimeException("sessionToken cannot be empty");

        this.appId = appId;
        this.contractId = contractId;
        this.sessionToken = sessionToken;
    }

    public void sendRequest(Activity activity) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(PERMISSION_ACCESS_INTENT_ACTION);
        sendIntent.putExtra(KEY_SESSION_TOKEN, sessionToken);
        sendIntent.putExtra(KEY_APP_ID, appId);
        sendIntent.setType(PERMISSION_ACCESS_INTENT_TYPE);

        if (verifyIntentCanBeHandled(sendIntent, activity.getPackageManager())) {
            activity.startActivityForResult(sendIntent, REQUEST_CODE);
        } else {
            startInstallDigiMeFlow(activity);
        }
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

    public interface Listener {
        void permissionGranted();
        void permissionDeclined();
        void incorrectRequestCode();
    }
}
