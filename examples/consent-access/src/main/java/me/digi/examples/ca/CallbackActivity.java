/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package me.digi.examples.ca;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import me.digi.sdk.core.CASession;
import me.digi.sdk.core.DigiMeAuthorizationManager;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.SDKResponse;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.internal.AuthorizationException;

public class CallbackActivity extends AppCompatActivity {

    private static final String TAG = "DemoCallbackActivity";
    private SDKCallback<CASession> cb;
    private DigiMeAuthorizationManager authManager;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.cb = new SDKCallback<CASession>() {
            @Override
            public void succeeded(SDKResponse<CASession> result) {
                onSessionReceived();
                writeStatus("Session created!");
            }

            @Override
            public void failed(SDKException exception) {
                writeStatus("Session create failed!");
                Log.d(TAG, exception.getMessage());
            }
        };

        final Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.GONE);
                DigiMeClient.getInstance().createSession(cb);
            }
        });

        statusText = (TextView) findViewById(R.id.callback_status);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (authManager != null) {
            authManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onSessionReceived() {
        authManager = DigiMeClient.getInstance().authorizeInitializedSession(this, new SDKCallback<CASession>() {
            @Override
            public void succeeded(SDKResponse<CASession> result) {
                writeStatus("Session authorized!");
                requestFileList();
            }

            @Override
            public void failed(SDKException exception) {
                writeStatus("Authorization failed!");
            }
        });
    }

    public void requestFileList() {
        DigiMeClient.getInstance().getFileList(new SDKCallback<CAFiles>() {
            @Override
            public void succeeded(SDKResponse<CAFiles> result) {
                CAFiles files = result.body;
                getFileContent(files.fileIds);
            }

            @Override
            public void failed(SDKException exception)  {
                writeStatus("Failed to fetch list" + exception.getMessage());

            }
        });
    }

    public void getFileContent(List<String> fileIds) {
        for (final String fileId :
                fileIds) {
            DigiMeClient.getInstance().getFileContent(fileId, new SDKCallback<CAFileResponse>() {
                @Override
                public void succeeded(SDKResponse<CAFileResponse> result) {
                    writeStatus("Content retrieved");
                    Log.d(TAG, "Content for file " + fileId + ": " + result.body.fileContent);
                }

                @Override
                public void failed(SDKException exception) {
                    Log.d(TAG, "Failed to retrieve file content for file: " + fileId + "; Reason: " + exception);
                }
            });
        }
    }

    private void writeStatus(String status) {
        statusText.setText(status);
        Log.d(TAG, status);
    }

}
