# Coldstock ProGuard / R8 rules

# --- Kotlinx Serialization -------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the generated serializers and companions used by @Serializable classes.
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all Coldstock model classes and their synthetic serializer members.
-keep,includedescriptorclasses class com.coldstock.app.model.**$$serializer { *; }
-keepclassmembers class com.coldstock.app.model.** {
    *** Companion;
    <fields>;
}
-keep class com.coldstock.app.model.** { *; }

# Enum values() / valueOf() used by serialization.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Compose ---------------------------------------------------------------
-dontwarn kotlinx.serialization.**
