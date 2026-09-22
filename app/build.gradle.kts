plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

import java.util.Properties
import java.io.File

val keystoreProps: Properties = Properties().apply {
    val localFile = rootProject.file("keystore.properties")
    val userHomeFile = File(System.getProperty("user.home"), "keystore.properties")
    when {
        localFile.exists() -> load(localFile.inputStream())
        userHomeFile.exists() -> load(userHomeFile.inputStream())
    }
}

val localProps: Properties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}
val geminiApiKey = (localProps["GEMINI_API_KEY"] as? String)
    ?: System.getenv("GEMINI_API_KEY") ?: ""

android {
    namespace = "com.forma.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.forma.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        ndk {
            abiFilters.clear()
            abiFilters.add("armeabi-v7a")
        }
    }

    val releaseStoreFile = (keystoreProps["KEYSTORE_PATH"] as? String)?.let { file(it) }
        ?: System.getenv("KEYSTORE_PATH")?.let { file(it) }
        ?: rootProject.file("habitflow-release.jks").takeIf { it.exists() }
    val isReleaseSigningAvailable = releaseStoreFile != null && releaseStoreFile.exists()

    signingConfigs {
        create("release") {
            if (isReleaseSigningAvailable) {
                storeFile = releaseStoreFile
                storePassword = (keystoreProps["KEYSTORE_PASSWORD"] as? String)
                    ?: System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = (keystoreProps["KEY_ALIAS"] as? String)
                    ?: System.getenv("KEY_ALIAS") ?: ""
                keyPassword = (keystoreProps["KEY_PASSWORD"] as? String)
                    ?: System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            ndk {
                abiFilters.clear()
                abiFilters.add("armeabi-v7a")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (isReleaseSigningAvailable) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            ndk {
                abiFilters.clear()
                abiFilters.add("armeabi-v7a")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.6")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Google Play Billing
    implementation(libs.play.billing)

    // Firebase (Auth & Firestore)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)

    // Google Sign-In & Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Debug Tooling
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}
