package me.digi.examples.ca;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import me.digi.sdk.core.CAContract;
import me.digi.sdk.core.CASession;
import me.digi.sdk.core.DigiMeAuthorizationManager;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.SDKResponse;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DemoActivity";
    private SDKCallback<CASession> cb;
    private DigiMeAuthorizationManager authManager;

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

        DigiMeClient.getInstance().createSession(cb);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (authManager != null) {
            authManager.onActivityResult(requestCode, resultCode, new DigiMeAuthorizationManager.Listener() {
                @Override
                public void permissionGranted() {
                    requestFileList();
                    Log.d(TAG, "Permission granted");
                }

                @Override
                public void permissionDeclined() {
                    Log.d(TAG, "Permission declined");
                }

                @Override
                public void incorrectRequestCode() {
                    Log.d(TAG, "Incorrect code");
                }
            });
        }
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
                Log.d(TAG, "Permission declined");

            }
        });
    }

    public void getFileContent(List<String> fileIds) {
        for (final String fileId :
                fileIds) {
            DigiMeClient.getInstance().getFileContent(fileId, new SDKCallback<CAFileResponse>() {
                @Override
                public void succeeded(SDKResponse<CAFileResponse> result) {
                    Log.d(TAG, "Content for file " + fileId + ": " + result.body.fileContent);
                }

                @Override
                public void failed(SDKException exception) {

                }
            });
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
}
