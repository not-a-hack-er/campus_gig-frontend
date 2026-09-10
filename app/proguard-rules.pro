# ─── CampusGig — ProGuard / R8 Rules ─────────────────────────────────────────
# These rules prevent R8 from removing or renaming classes that are used
# by third-party libraries which depend on reflection or serialization.

# ─── Retrofit ────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# ─── Gson Serialization ───────────────────────────────────────────────────────
# Keep all data model classes (used by Gson to deserialize API JSON)
-keep class com.abpvt.campusgig_frontend.data.model.** { *; }
-keep class com.abpvt.campusgig_frontend.data.model.request.** { *; }
-keep class com.abpvt.campusgig_frontend.data.model.response.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson instantiates field-level @JsonAdapter classes reflectively.
-keep class com.abpvt.campusgig_frontend.core.network.*Deserializer { public <init>(); *; }

# ─── OkHttp ──────────────────────────────────────────────────────────────────
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ─── Socket.IO Client ────────────────────────────────────────────────────────
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# Firebase discovers these registrars by class name from manifest metadata.
-keep class * implements com.google.firebase.components.ComponentRegistrar { public <init>(); *; }
-keep class com.google.firebase.**Registrar { public <init>(); *; }

# ─── Kotlin Coroutines ───────────────────────────────────────────────────────
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ─── Jetpack Compose ─────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ─── Lifecycle / ViewModel ───────────────────────────────────────────────────
-keep class androidx.lifecycle.** { *; }

# ─── Kotlin reflection ───────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.jvm.JvmStatic <methods>;
}

# ─── Remove debug logs in production ─────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}
