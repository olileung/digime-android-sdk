-keepattributes *Annotation*
-dontwarn java.nio.file.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exception
-keepattributes EnclosingMethod
-keepattributes InnerClassess
-keepclasseswithmembers class * {
  @retrofit2.http.* <methods>;
}

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

-keep class me.digi.sdk.core.entities.CAContent.** { *; }
-keep class me.digi.sdk.core.entities.CAFileResponse.** { *; }

-keep class org.spongycastle.jcajce.provider.**