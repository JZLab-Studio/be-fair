# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** { *** Companion; *** $serializer(...); }
-keep,includedescriptorclasses class dev.jakubzika.befair.**$$serializer { *; }

# Room (generated *_Impl and constructors)
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# Ktor: optional JVM-only classes not present on Android
-dontwarn java.lang.management.**
-dontwarn org.slf4j.**

# Tink (via androidx.security:security-crypto / EncryptedSharedPreferences): compile-time annotations only
-dontwarn com.google.errorprone.annotations.**
