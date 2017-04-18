package me.digi.examples.ca;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import me.digi.sdk.core.CAContract;
import me.digi.sdk.core.CASession;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.SDKResponse;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DemoActivity";
    private SDKCallback<CASession> cb;

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

    public void onSessionReceived() {
        if (DigiMeClient.getInstance().flow.next())
            DigiMeClient.getInstance().createSession(cb);
    }
}
