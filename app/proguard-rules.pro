# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class io.github.winzysss.fuckdpi.core.DesyncProxy { *; }

-keep,allowoptimization class io.github.winzysss.fuckdpi.core.TProxyService { *; }
-keep,allowoptimization class io.github.winzysss.fuckdpi.activities.** { *; }
-keep,allowoptimization class io.github.winzysss.fuckdpi.services.** { *; }
-keep,allowoptimization class io.github.winzysss.fuckdpi.receiver.** { *; }

-keep class io.github.winzysss.fuckdpi.fragments.** {
    <init>();
}

-keep,allowoptimization class io.github.winzysss.fuckdpi.data.** {
    <fields>;
}

-keep class io.github.winzysss.fuckdpi.utility.UpdateChecker$Release** {
    <fields>;
}

-keepattributes Signature
-keepattributes *Annotation*

-repackageclasses 'fuckdpi'
-renamesourcefileattribute ''
-keepattributes SourceFile,InnerClasses,EnclosingMethod,Signature,RuntimeVisibleAnnotations,*Annotation*,*Parcelable*
-allowaccessmodification
-overloadaggressively
-optimizationpasses 5
-verbose
-dontusemixedcaseclassnames
-adaptclassstrings
-adaptresourcefilecontents **.xml,**.json
-adaptresourcefilenames **.xml,**.json