-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# Gson rules: Prevent Proguard from stripping/renaming @SerializedName fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep the Gemini network data models intact so Gson can deserialize them at runtime
-keep class com.delwin.expnx.data.network.** { *; }

# Keep TypeToken and its subclasses to prevent R8 from stripping generic signatures
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken