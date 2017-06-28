# Digi.me SDK for Android

The Digi.me SDK for Android is a multi-module library that allows seamless authentication with Consent Access service, making content requests and core decryption services. For details on the API and general CA architecture, visit [Dev Support Docs](http://devsupport.digi.me/start.html).

## Preamble

Digi.me SDK depends on digi.me app being installed to enable user initiated authorization of requests.
For detailed explanation of the Consent Access architecture please visit [Dev Support Docs](http://devsupport.digi.me/start.html).

## Manual Installation

### Using pre-built binaries

1. Download .AAR file from [repository](http://download.digi.me/android/sandboxca/sandboxca-digime-android-sdk.aar)

2. If creating a new project set Minimum SDK to 21.

3. For existing projects set minSdkVersion to 21 in build.gradle 

4. In Android Studio, go to File > New > New Module, select "Import .JAR or >AAR Package"

5. Specify location of the downloaded .AAR file 

6. Go to File > Project Structure, add the SDK module as a dependency for your project

7. You should be able to import `me.digi.sdk.core.DigiMeClient` now.


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

```java
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


## Getting started examples
