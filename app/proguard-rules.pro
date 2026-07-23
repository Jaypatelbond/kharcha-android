-keepattributes *Annotation*
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# ── Google API Client & Drive API ──
# Keep all Google API model classes (they use reflection for JSON parsing)
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.services.drive.model.** { *; }

# Keep Google API Client core (reflection-heavy)
-keep class com.google.api.client.** { *; }
-keep class com.google.api.client.googleapis.** { *; }
-keep class com.google.api.client.json.** { *; }
-keep class com.google.api.client.http.** { *; }
-keep class com.google.api.client.util.** { *; }

# Keep Google HTTP Client
-keep class com.google.http.** { *; }

# Keep Google Auth
-keep class com.google.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# Keep GSON (used for JSON serialization/deserialization)
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes AnnotationDefault

# Suppress warnings
-dontwarn javax.naming.**
-dontwarn javax.naming.directory.**
-dontwarn javax.naming.ldap.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.http.**
-dontwarn com.google.appengine.**
-dontwarn com.google.apphosting.**

