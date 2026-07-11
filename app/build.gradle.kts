import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Load optional local signing properties (never committed).
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

/**
 * Resolve a signing value from an environment variable first, then a local
 * keystore.properties file. Returns null when the value is not configured.
 */
fun signingValue(envKey: String, propKey: String): String? {
    val fromEnv = System.getenv(envKey)
    if (!fromEnv.isNullOrBlank()) return fromEnv
    val fromProps = keystoreProperties.getProperty(propKey)
    if (!fromProps.isNullOrBlank()) return fromProps
    return null
}

android {
    namespace = "com.coldstock.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.coldstock.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("ANDROID_KEYSTORE_PATH")
                ?: keystoreProperties.getProperty("storeFile")
            val storePass = signingValue("ANDROID_KEYSTORE_PASSWORD", "storePassword")
            val alias = signingValue("ANDROID_KEY_ALIAS", "keyAlias")
            val keyPass = signingValue("ANDROID_KEY_PASSWORD", "keyPassword")

            if (storeFilePath != null && storePass != null && alias != null && keyPass != null) {
                storeFile = file(storeFilePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            // Staged release hardening: keep false for the first verified
            // release build, then flip both to true and re-test (see README).
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val releaseSigning = signingConfigs.getByName("release")
            val hasCredentials = releaseSigning.storeFile != null &&
                releaseSigning.storePassword != null &&
                releaseSigning.keyAlias != null &&
                releaseSigning.keyPassword != null

            if (hasCredentials) {
                signingConfig = releaseSigning
            } else {
                // Never silently fall back to the debug key for release
                // artifacts. Fail clearly when a signed release is requested.
                gradle.taskGraph.whenReady {
                    val buildingReleaseArtifact = allTasks.any {
                        val n = it.name
                        n.contains("Release") && (n.startsWith("assemble") || n.startsWith("bundle") || n.startsWith("package"))
                    }
                    if (buildingReleaseArtifact) {
                        throw GradleException(
                            "Release signing credentials are missing. Provide " +
                            "ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, " +
                            "ANDROID_KEY_ALIAS and ANDROID_KEY_PASSWORD (or a " +
                            "keystore.properties file). Refusing to sign a " +
                            "release build with the debug key."
                        )
                    }
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")
    implementation("androidx.activity:activity-compose:1.9.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
