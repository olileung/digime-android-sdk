package me.digi.examples.ca;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import me.digi.sdk.core.CAContract;
import me.digi.sdk.core.CASession;
import me.digi.sdk.core.DigiMeAuthorizationManager;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.SDKListener;
import me.digi.sdk.core.SDKResponse;
import me.digi.sdk.core.entities.CAContent;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.internal.AuthorizationException;

public class MainActivity extends AppCompatActivity implements SDKListener {

    private static final String TAG = "DemoActivity";
    private SDKCallback<CASession> cb;
    private DigiMeAuthorizationManager authManager;
    private TextView statusText;
    private Button gotoCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.cb = new SDKCallback<CASession>() {
            @Override
            public void succeeded(SDKResponse<CASession> result) {
                onSessionReceived();
                Log.d(TAG, "Session created with key " + result.body.sessionKey);
            }

            @Override
            public void failed(SDKException exception) {
                onSessionReceived();
                Log.d(TAG, exception.getMessage());
            }
        };

        statusText = (TextView) findViewById(R.id.status_text);
        gotoCallback = (Button) findViewById(R.id.go_to_callback);
        gotoCallback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DigiMeClient.getInstance().removeListener(MainActivity.this);
                startActivity(new Intent(MainActivity.this, CallbackActivity.class));
            }
        });
        gotoCallback.setVisibility(View.GONE);

        DigiMeClient.getInstance().addListener(this);
        authManager = DigiMeClient.getInstance().authorize(this, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (authManager != null) {
            authManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onSessionReceived() {
       authManager = DigiMeClient.getInstance().authorize(this, new SDKCallback<CASession>() {
           @Override
           public void succeeded(SDKResponse<CASession> result) {
               Log.d(TAG, "Session created with key " + result.body.sessionKey);
           }

           @Override
           public void failed(SDKException exception) {
               Log.d(TAG, "Permission declined");
           }
       });
//        if (DigiMeClient.getInstance().flow.next())
//            DigiMeClient.getInstance().createSession(cb);
    }

    @Override
    public void sessionCreated(CASession session) {
        Log.d(TAG, "Session created with token " + session.getSessionKey());
        statusText.setText("Session created... Authorizing...");
    }

    @Override
    public void sessionCreateFailed(SDKException reason) {
        Log.d(TAG, reason.getMessage());
        gotoCallback.setVisibility(View.VISIBLE);
    }

    @Override
    public void authorizeSucceeded(CASession session) {
        Log.d(TAG, "Session created with token " + session.getSessionKey());
        statusText.setText("Session authorized!");
        DigiMeClient.getInstance().getFileList(null);
    }

    @Override
    public void authorizeDenied(AuthorizationException reason) {
        Log.d(TAG, "Failed to authorize session; Reason " + reason.getThrowReason().name());
        statusText.setText("Authorization declined");
        gotoCallback.setVisibility(View.VISIBLE);
    }

    @Override
    public void authorizeFailedWithWrongRequestCode() {

    }

    @Override
    public void clientRetrievedFileList(CAFiles files) {
        for (final String fileId :
                files.fileIds) {
            DigiMeClient.getInstance().getFileContent(fileId, null);
        }
        statusText.setText("Data retrieved");
        gotoCallback.setVisibility(View.VISIBLE);
    }

    @Override
    public void clientFailedOnFileList(SDKException reason) {
        Log.d(TAG, "Failed to retrieve file list: " + reason.getMessage());
        gotoCallback.setVisibility(View.VISIBLE);
    }

    @Override
    public void contentRetrievedForFile(String fileId, CAFileResponse content) {
        Log.d(TAG, content.fileContent.toString());
    }

    @Override
    public void contentRetrieveFailed(String fileId, SDKException reason) {
        Log.d(TAG, "Failed to retrieve file content for file: " + fileId + "; Reason: " + reason);
    }
}
