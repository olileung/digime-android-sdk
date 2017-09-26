# Digi.me SDK for Android

The Digi.me SDK for Android is a multi-module library that allows seamless authentication with Consent Access service, making content requests and core decryption services. For details on the API and general CA architecture, visit [Dev Support Docs](http://devsupport.digi.me/start.html).


## Preamble

Digi.me SDK depends on digi.me app being installed to enable user initiated authorization of requests.
For detailed explanation of the Consent Access architecture please visit [Dev Support Docs](http://devsupport.digi.me/start.html).


## Manual Installation

### Using gradle

**NOTE:** For testing and initial integration,**DEVELOPMENT** builds should be used, since production usage might be subject to usage billing. For instructions how to set up use of _Dev_ version see the [section below](#development-builds-and-snapshots).

1. Add the repository path to your root build.gradle

```gradle
    allprojects {
        repositories {
            maven { url "https://repository.sysdigi.net/artifactory/libs-release"}
        }
    }
```
2. If creating a new project set Minimum SDK to 21.

3. For existing projects set minSdkVersion to 21 in build.gradle 

4. In your project build.gradle (for example app.build.gradle) add the digime-core dependency

```gradle

   dependencies {
        compile 'me.digi.sdk:digime-core:1.1.0'
   }
```

5. You should be able to import `me.digi.sdk.core.DigiMeClient` now.

### Development builds and snapshots

For testing purposes or initial integartion _dev_ version of the library should always be used. To use development version, following dependency should be imported.

```gradle

   dependencies {
        compile '...:digime-core:1.1.0-dev'
   }
```

Snapshot builds can be retrieved from the **SNAPSHOTS** repository. Snapshots can be used to try out new in-development features. It is discouraged to use snapshots in production releases.
To use snapshot builds use the following dependency:

```gradle

   dependencies {
        compile '...:digime-core:1.1.0-SNAPSHOT'
   }
```

### Directly from source code (downloaded or git submodule)

1. Download source code

2. If creating a new project set Minimum SDK to 21.

3. For existing projects set minSdkVersion to 21 in build.gradle 

4. In Android Studio, go to File > New > New Module, select "Import Existing Project as Module"

5. Specify location of the downloaded code

6. Go to File > Project Structure, add the SDK module as a dependency for your project

7. You should be able to import `me.digi.sdk.core.DigiMeClient` now.


## Proguard setup

If proguard is enabled in the project you might need to add following parameters to proguard configuration:

```proguard
-dontwarn retrofit2.**
-dontwarn javax.naming.**
-keep class retrofit2.** { *; }
-keepattributes Signature

-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn`` okhttp3.**
-dontwarn okio.**

-dontwarn com.google.gson.**
-dontwarn org.spongycastle.**
```


## Configuring SDK usage

### Obtaining your Contract ID and App ID

Before accessing the public APIs, a valid Contract ID needs to be registered for an App ID.
The Contract ID uniquely identifies a contract with the user that spells out what type of data you want, what you will and won't do with it, how long you will retain it and if you will implement the right to be forgotten.
It also specifies how the data is encrypted in transit.

To register a Consent Access contract check out [Digi.me Dev Support](http://devsupport.digi.me/). There you can request a Contract ID and App ID to which it is bound.

### DigiMeClient and it's configuration

**DigiMeClient** is the main hub for all the interaction with the API. You access it through it's singleton accessor:
 
```java
DigiMeClient.getInstance()
```

DigiMeClient is automatically bootstrapped so there is no need to initialize it onCreate.
However before you start interacting with it in your app, you will need to configure it with your **contractId** and **appId**: 

Add your `contractId` and `appId` to a project resource file (for example strings.xml) and reference them in your Android manifest:

1. Add a string to your strings.xml with the value of your `contractId` (example name digime_contract_id)

2. In AndroidManifest.xml add a `meta-data` element specifying your `contractId`:

```xml
<application  ...>
    ...
    <meta-data android:name="me.digi.sdk.Contracts" android:resource="@string/digime_contract_id" />
    ...
</application>
```

3. Include you `appId` by adding a `meta-data` element:

```xml
<application  ...>
    ...
    <meta-data android:name="me.digi.sdk.AppId" android:value="@string/DIGIME_APP_ID" />   
    ...
</application>
```

4. SDK needs internet permission so you need to add it with uses-permission element:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```


5. Since DigiMeClient calls out to Digi.me app to let the user authorize request for data, you need to add the following `intent-filter`:
 
 ```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
 ```

6. **Optionally** you can include a custom App Name by specifying `meta-data` element:

```xml
<application  ...>
    ...
    <meta-data android:name="me.digi.sdk.AppName" android:value="@string/app_name" />   
    ...
</application>
```


## Providing contract private key

All content retrieved by the SDK is encrypted in transit using the public key bound to the certificate that was created when the Consent Access contract was created. 
For SDK to be able to decrypt content transparently matching private key must be provided (ie. from the keypair created for contract).

Digi.me SDK accepts PKCS #12 encoded files as the default key storage format.

API exposes multiple input vectors for p12 files.

### p12 file from custom sources

Digi.me SDK provides multiple helper methods to read and extract keys from p12 files. 
`KeyLoaderProvider` is the object that manages all the keys. Invoking `DigiMeClient.getDefaultKeyLoader()` returns the default provider.
From there we have an option to add a p12 file content via `InputStream`:

```java
DigiMeClient.getDefaultKeyLoader().addKeyFromPKCS12Stream(stream, keystore_passphrase);
```
 
or manually from assets/resources:

```java
//From assets
DigiMeClient.getDefaultKeyLoader().getStore().addPKCS12KeyFromAssets(context, assetPath, null, store_passphrase, null);

//or from resources
DigiMeClient.getDefaultKeyLoader().getStore().addPKCS12KeyFromResources(context, resourceID, null, store_passphrase, null);
```
 
Utility class `PKCS12Utils` also provides additional input vectors (it is used internally):
- `getPKCS12KeysFromByteArray`
- `getPKCS12KeysFromBase64`

Utility methods return a `List<PrivateKey>` which in turn can be sent to the default loader:
```java
DigiMeClient.getDefaultKeyLoader().getStore().addFromList(List_of_private_keys);
``` 

### p12 file in manfiest meta-data

If the p12 file is located in application assets, SDK can extract it with a valid asset path:

```xml
<application>
    <meta-data android:name="me.digi.sdk.Keys" android:value="path_to_file_in_assets" />   
</application>
```

In case the p12 file is part of app resources providing the resource ID will be enough to extract the file:

```xml
<application>
    <meta-data android:name="me.digi.sdk.Keys" android:value="integer_id_of_the_resource" />   
</application>
```

Since it is recommended for p12 files to be locked with a passphrase, provide the passphrase through meta-data:

```xml
<application>
    <meta-data android:name="me.digi.sdk.KeysPassphrase" android:value="passphrase" />   
</application>
```

### Raw PEM or PKCS#8 encoded keys

In rare cases (usage not recommended) app might need to provide raw private keys either encoded with PEM or raw PKCS#8 format:

```java
/* 
 * If key is hexadecimal string, extract it
 */
byte[] key = ByteUtils.hexToBytes(hexCodedKey);

/* 
 * Extract PrivateKey from byte array
 */
PrivateKey privateKey = CryptoUtils.getPrivateKey(key);

/* 
 * Add the key to the SDK key provider
 */
DigiMeClient.getDefaultKeyLoader().getStore().addKey(privateKey);
``` 

## Callbacks and responses
 
Digi.me SDK is built to be asynchronous and thread-safe and as such it provides a couple of mechanisms of redirecting results back to the application.
For that purpose the SDK provides **SDKCallback** interface and **SDKListener** interface. 

Both of them are interchangeable and can be used depending on preference. 
They can be used both at the same time, although such usage would result in very verbose code.
 
### SDKCallback

`SDKCallback` is provided per call and it contains a SDKResponse object which encapsulates all of the objects you can get for the request and actual raw response data.
To use a callback you pass it's reference to the request:

```java
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.SDKResponse;

DigiMeClient.getInstance().getFileList(new SDKCallback<CAFiles>() {
    @Override
    public void succeeded(SDKResponse<CAFiles> result) {
                CAFiles files = result.body;
    }
            
    @Override
    public void failed(SDKException exception)  {
        //Handle exception or error response
    }
});
```

Alternatively if you prefer the `SDKListener` interface, callbacks can be omitted by passing `null`. 

```java
DigiMeClient.getInstance().getFileList(null);
```


### SDKListener

`SDKListener` provides a central listening pipe for all the relevant SDK events.
 
To start listening you must implement the `SDKListener` interface (most frequently in your Launch Activity) and register it with the DigiMeClient (for example in the onCreate method of your Launch Activity).
 
```java
public class MainActivity extends ... implements SDKListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        DigiMeClient.getInstance().addListener(this);
    }
}
```


## Authorization

To start getting data into application, you'll need to authorize a session.
Authorization flow is separated into two phases:

1. Initialize a session with digi.me API (returns a **CASession** object)

2. Authorize session with the digi.me app and prepare data if user accepts.

SDK starts and handles these steps automatically by calling the `authorize(Activity, SDKCallback)` method.
This method expects a reference to the calling activity and optionally a callback.

```java
DigiMeClient.getInstance().authorize(this, new SDKCallback<CASession>() {
    @Override
    public void succeeded(SDKResponse<CASession> result) {
                
    }

    @Override
    public void failed(SDKException exception) {

    }
});
```

On success it returns a `CASession` in your callback, which encapsulates session data required for further calls.

Since `authorize()` automatically calls into digi.me app, you'll need some way of handling the switch back to your app.
You will accomplish this by overriding `onActivityResult` for your Activity.

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    DigiMeClient.getInstance().getAuthManager().onActivityResult(requestCode, resultCode, data);
}
```

### authorize() specifics

Method authorize() also returns an instance of `DigiMeAuthorizationManager`, which in this case is the default one you can access by calling:

```java
DigiMeClient.getInstance().getAuthManager()
```

If using the SDKListener interface instead of callbacks, it will trigger these events:

```java
void sessionCreated(CASession session);
void sessionCreateFailed(SDKException reason);

/*
 * User approved in the digi.me app and data ready
 */
void authorizeSucceeded(CASession session);
/*
 * User declined 
 */
void authorizeDenied(AuthorizationException reason);
/*
 * Activity passed a wrong request code, most likely from another application
 */
void authorizeFailedWithWrongRequestCode();
```

Furthermore you don't have to keep a reference to the returned CASession object internally, since DigiMeClient already track that object. 
You can always reference it later if need arises, but such scenarios are very rare:
 
```java
DigiMeClient.getInstance().getSessionManager().getCurrentSession()
```


## Fetching data

Upon successful authorization you can request user's files. 
To fetch the list of available files for your contract:

```java
 /*  @param callback         reference to the SDKCallback or null if using SDKListener
  * 
  */
DigiMeClient.getInstance().getFileList(callback)
```

Upon success DigiMeClient returns a `CAFiles` object which contains a single field `fileIds`, a list of file IDs.

Finally you can use the returned file IDs to fetch their data:

```java
 /* @param fileId         ID of the file to retrieve
  * @param callback         reference to the SDKCallback or null if using SDKListener
  */
DigiMeClient.getInstance().getFileContent(fileId, callback)
```

Upon success DigiMeClient returns a `CAFileResponse` which contains a list of deserialized content objects (`CAContent`)

For detailed content item structure look at [Dev Docs](http://devsupport.digi.me/downloads.html).

### Fetching raw response JSON

In some cases it is beneficial to have access to the complete underlying json response.
As with regular fetch you can retrieve the data once you have the list of file IDs with:

```java
 /* @param fileId         ID of the file to retrieve
  * @param callback         reference to the SDKCallback or null if using SDKListener
  */
DigiMeClient.getInstance().getFileJSON(fileId, callback)
```

Upon success DigiMeClient returns a `JsonElement` which contains complete file content.

For detailed content item structure look at [Dev Docs](http://devsupport.digi.me/downloads.html).

### Decryption

There are no additional steps necessary to decrypt the data, the SDK handles the decryption and cryptography management behind the scenes.

In cases where you don't want to use the SDK for requests, the security module can be used independently.
Just import `me.digi.sdk.crypto` package. 

For details on such implementation check out the **examples/consent-access-no-sdk** example app.


## License

Copyright © 2017 digi.me Ltd. All rights reserved.