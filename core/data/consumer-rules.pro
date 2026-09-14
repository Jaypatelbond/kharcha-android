-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,AnnotationDefault
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# ── Google API Client & Drive API ──
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.services.drive.model.** { *; }
-keep class * extends com.google.api.client.json.GenericJson { *; }

# Preserve fields annotated with @Key for Drive models JSON deserialization
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}

# Keep Google API Client core (reflection-heavy)
-keep class com.google.api.client.** { *; }
-keep class com.google.api.client.googleapis.** { *; }
-keep class com.google.api.client.json.** { *; }
-keep class com.google.api.client.json.gson.** { *; }
-keep class com.google.api.client.http.** { *; }
-keep class com.google.api.client.util.** { *; }
-keep class com.google.api.client.extensions.android.** { *; }

# Keep Google HTTP Client
-keep class com.google.http.** { *; }

# Keep Google Auth & Play Services Sign-in
-keep class com.google.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# Keep GSON (used for JSON serialization/deserialization)
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Suppress warnings for optional/runtime dependencies
-dontwarn sun.misc.**
-dontwarn javax.naming.**
-dontwarn javax.naming.directory.**
-dontwarn javax.naming.ldap.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.http.**
-dontwarn com.google.appengine.**
-dontwarn com.google.apphosting.**
