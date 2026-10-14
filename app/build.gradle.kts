import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val abis = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun signingProp(key: String, env: String) =
    keystoreProps.getProperty(key) ?: System.getenv(env)
val releaseStoreFile = signingProp("storeFile", "KEYSTORE_FILE")?.let { rootProject.file(it) }

android {
    namespace = "io.github.winzysss.fuckdpi"
    //noinspection GradleDependency
    compileSdk = 36
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "io.github.winzysss.fuckdpi"
        minSdk = 21
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = providers.gradleProperty("releaseVersionCode")
            .map(String::toInt).getOrElse(1)
        versionName = providers.gradleProperty("releaseVersionName")
            .getOrElse("1.0.0")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(abis)
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    signingConfigs {
        create("release") {
            storeFile = releaseStoreFile
            storePassword = signingProp("storePassword", "KEYSTORE_PASSWORD")
            keyAlias = signingProp("keyAlias", "KEY_ALIAS")
            keyPassword = signingProp("keyPassword", "KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            buildConfigField("String", "VERSION_NAME",  "\"${defaultConfig.versionName}\"")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isMinifyEnabled = true
            isShrinkResources = true
            if (signingConfigs.getByName("release").storeFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            buildConfigField("String", "VERSION_NAME",  "\"${defaultConfig.versionName}-debug\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // https://android.izzysoft.de/articles/named/iod-scan-apkchecks?lang=en#blobs
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    splits {
        abi {
            isEnable = true
            reset()
            include(*abis.toTypedArray())
            isUniversalApk = true
        }
    }
}

dependencies {
    //noinspection GradleDependency
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.code.gson:gson:2.14.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

tasks.register<Exec>("runNdkBuild") {
    group = "build"

    val ndkDir = android.ndkDirectory
    executable = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) {
        "$ndkDir\\ndk-build.cmd"
    } else {
        "$ndkDir/ndk-build"
    }
    setArgs(listOf(
        "NDK_PROJECT_PATH=build/intermediates/ndkBuild",
        "NDK_LIBS_OUT=src/main/jniLibs",
        "APP_BUILD_SCRIPT=src/main/jni/Android.mk",
        "NDK_APPLICATION_MK=src/main/jni/Application.mk"
    ))

    println("Command: $commandLine")

    // ndk-build emits the tunnel submodule's shared library into each jniLibs
    // ABI dir. Its file name is fixed by the submodule's own Android.mk (an
    // upstream file we must not modify), so rename whatever .so was produced
    // to libtproxy.so — the name dlopen()ed in src/main/cpp/tunnel-jni.c —
    // keeping upstream names out of the packaged APK.
    doLast {
        val jniLibsDir = file("src/main/jniLibs")
        abis.forEach { abi ->
            val abiDir = jniLibsDir.resolve(abi)
            val produced = abiDir.listFiles { f -> f.name.endsWith(".so") }
                .orEmpty()
                .filter { it.name != "libtproxy.so" }
            if (produced.size > 1) {
                throw GradleException("runNdkBuild: ${produced.size} tunnel libraries produced for $abi: ${produced.map { it.name }}")
            }
            produced.forEach { lib ->
                Files.move(lib.toPath(), lib.toPath().resolveSibling("libtproxy.so"),
                    StandardCopyOption.REPLACE_EXISTING)
            }
            if (!abiDir.resolve("libtproxy.so").exists()) {
                throw GradleException("runNdkBuild: no tunnel library found for $abi")
            }
        }
    }
}

tasks.preBuild {
    dependsOn("runNdkBuild")
}
