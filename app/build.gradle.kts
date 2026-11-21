@file:Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")

import com.cloudx.ios17.buildsrc.Versions
import java.util.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.refine)
}

// Manifest version information
val versionMajor = 1
val versionMinor = 15
val versionPatch = 0

val localProps = Properties()
val localPropsFile = project.rootProject.file("local.properties")

if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}

val appendDebugSuffix = (localProps.getProperty("appendDebugSuffix") ?: "true").toBoolean()
val keyStorePath = localProps.getProperty("keyStorePath") ?: "/keystore/platform.keystore"
val keyStorePassword = localProps.getProperty("keyStorePassword") ?: "android"
val signingKeyAlias = localProps.getProperty("keyAlias") ?: "platform"
val signingKeyPassword = localProps.getProperty("keyPassword") ?: "android"

android {
    namespace = "com.cloudx.ios17"
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        applicationId = "com.cloudx.ios17"
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        versionCode = versionMajor * 100_00_00 + versionMinor * 10_00 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "BlissLauncher-$versionName")

        // Optional: batasi bahasa yang dikemas, mis. resConfigs=en,id di local.properties.
        // Tanpa ini, semua terjemahan tetap dibawa (belum terkena resource shrinking).
        val localeList = (localProps.getProperty("resConfigs") ?: "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (localeList.isNotEmpty()) {
            resourceConfigurations += localeList
        }
    }

    buildFeatures { buildConfig = true }

    buildTypes {
        release {
            // R8 + resource shrinking: APK jauh lebih kecil & startup lebih ringan.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            if (appendDebugSuffix) {
                applicationIdSuffix = ".debug"
            }
            signingConfig = signingConfigs.getByName("debug")
        }

        create("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }

    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(rootDir.path + keyStorePath)
            storePassword = keyStorePassword
            keyAlias = signingKeyAlias
            keyPassword = signingKeyPassword
        }
    }

    flavorDimensions.add("api")
    productFlavors {
        // Semua flavor mewarisi minSdk/targetSdk dari defaultConfig (minSdk 23).
        create("apiQ") { dimension = "api" }
        create("apiR") { dimension = "api" }
        create("apiS") { dimension = "api" }
    }

    // Always show the result of every unit test, even if it passes.
    testOptions.unitTests.all { test ->
        test.testLogging { events("passed", "skipped", "failed", "standardOut", "standardError") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = "1.8" }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        warningsAsErrors = true
        disable.add("PluralsCandidate")
        disable.add("MissingTranslation")
        disable.add("UnusedResources")
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    "apiQImplementation"(files("libs/lineage-sdk-q.jar"))
    "apiRImplementation"(files("libs/lineage-sdk-r.jar"))
    "apiSImplementation"(files("libs/e-ui-sdk-s.jar"))

    // Support Libs
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.localbroadcast)

    // Rx Java and Android
    implementation(libs.rx.android)
    implementation(libs.rx.binding)
    implementation(libs.rx.java)
    implementation(libs.rx.relay)

    // Room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.retrofit.rxjava.adapter)

    // Okhttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Misc. (dependency tak terpakai & berat sudah dibuang:
    // apache-commons, eventbus, restriction-bypass, profileinstaller, desugaring, telemetry)
    implementation(libs.hoko.blur)
    // elib menyediakan resource e_* (warna/drawable) yang dipakai layout & theme.
    implementation(libs.elib)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.circleindicator)
    implementation(libs.tools.timber)
    debugImplementation(libs.debug.db)
    debugImplementation(libs.tools.leakcanary)

    // Testing dependencies
    testImplementation(libs.bundles.testing.unit)
    testImplementation(libs.bundles.testing.android)
}
