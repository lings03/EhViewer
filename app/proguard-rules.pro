-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Ktor logger
-dontwarn org.slf4j.impl.StaticLoggerBinder

-dontwarn org.conscrypt.Conscrypt

# https://issuetracker.google.com/222232895
-dontwarn androidx.window.extensions.**
-dontwarn androidx.window.sidecar.Sidecar*

-keepattributes LineNumberTable

-allowaccessmodification
-repackageclasses

# okhttp3.dnsoverhttps
-keeppackagenames okhttp3.internal.publicsuffix.*
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Keep all classes that inherit from JavaScriptInterface from being obfuscated
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# tech.relaycorp.doh

-dontwarn lombok.Generated
-dontwarn org.xbill.DNS.spi.DnsjavaInetAddressResolverProvider
-dontwarn sun.net.spi.nameservice.NameServiceDescriptor