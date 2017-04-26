package me.digi.examples.ca;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.digi.sdk.core.session.CASession;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.SDKListener;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.internal.AuthorizationException;

public class MainActivity extends AppCompatActivity implements SDKListener {

    private static final String TAG = "DemoActivity";
    private TextView statusText;
    private Button gotoCallback;
    private DigiMeClient dgmClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dgmClient = DigiMeClient.getInstance();

        statusText = (TextView) findViewById(R.id.status_text);
        gotoCallback = (Button) findViewById(R.id.go_to_callback);
        gotoCallback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dgmClient.removeListener(MainActivity.this);
                startActivity(new Intent(MainActivity.this, CallbackActivity.class));
            }
        });
        gotoCallback.setVisibility(View.GONE);

        dgmClient.addListener(this);
        dgmClient.authorize(this, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dgmClient.getAuthManager().onActivityResult(requestCode, resultCode, data);

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
