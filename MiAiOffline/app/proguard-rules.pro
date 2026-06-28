# Keep llama.cpp JNI bridge classes - native methods must not be obfuscated
-keep class com.miai.offline.inference.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# Gson model classes
-keep class com.miai.offline.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
