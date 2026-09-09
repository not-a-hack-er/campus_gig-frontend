import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

// ─── Load keystore properties from keystore.properties (NOT committed to Git) ─
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.abpvt.campusgig_frontend"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.abpvt.campusgig_frontend"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ── Inject environment URLs as BuildConfig fields ──────────────────────
        // Override these in keystore.properties or CI environment variables.
        // For local dev on emulator: 10.0.2.2:5000 | For production: your HTTPS domain
        buildConfigField("String", "BASE_URL",
            keystoreProperties.getProperty("BASE_URL", "\"http://10.0.2.2:5000/api/\""))
        buildConfigField("String", "SOCKET_URL",
            keystoreProperties.getProperty("SOCKET_URL", "\"http://10.0.2.2:5000\""))
    }

    // ── Release Signing Configuration ─────────────────────────────────────────
    // Create a keystore.properties file in the project root (NOT committed to Git):
    //   storeFile=path/to/campusgig.keystore
    //   storePassword=your_keystore_password
    //   keyAlias=campusgig
    //   keyPassword=your_key_password
    //   BASE_URL="https://api.campusgig.com/api/"
    //   SOCKET_URL="https://api.campusgig.com"
    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                  storeFile = rootProject.file(keystoreProperties.getProperty("storeFile", ""))
                  storePassword = keystoreProperties.getProperty("storePassword", "")
                  keyAlias = keystoreProperties.getProperty("keyAlias", "")
                  // PKCS12 uses the keystore password for private keys as well.
                  keyPassword = keystoreProperties.getProperty("storePassword", "")
            }
        }
    }

    buildTypes {
        release {
            // ── R8 Minification & Resource Shrinking ──────────────────────────
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Apply release signing if keystore.properties exists
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        // ── Enable coreLibraryDesugaring to support java.time on API 24-25 ────
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true  // Needed for BuildConfig.DEBUG and custom fields
    }
}

dependencies {
    // ── Core Library Desugaring (required for java.time on API 24-25) ─────────
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // ── Security — EncryptedSharedPreferences ─────────────────────────────────
    implementation(libs.androidx.security.crypto)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Real-time chat & Messaging
    implementation(libs.socketio.client)
    implementation(libs.firebase.messaging)
    implementation(libs.coil.compose)
    implementation(libs.androidx.fragment.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
