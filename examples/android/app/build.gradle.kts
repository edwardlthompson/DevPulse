plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val pulseVersion = rootProject.projectDir.resolve("../../.template-version")
    .takeIf { it.isFile }
    ?.readText()
    ?.trim()
    .orEmpty()
    .ifEmpty { "0.1.0" }
val pulseVersionCode = run {
    val parts = pulseVersion.substringBefore('-').substringBefore('+').split('.')
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
    major * 10000 + minor * 100 + patch
}
val pulseStore = System.getenv("DEVPULSE_STORE_FILE")
    ?.takeIf { it.isNotBlank() }
    ?.let { file(it) }
    ?.takeIf { it.isFile }

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

android {
    namespace = "dev.foss.goldenpath"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.devpulse"
        minSdk = 26
        targetSdk = 37
        versionCode = pulseVersionCode
        versionName = pulseVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (pulseStore != null) {
        signingConfigs {
            create("release") {
                storeFile = pulseStore
                storePassword = System.getenv("DEVPULSE_STORE_PASSWORD").orEmpty()
                keyAlias = System.getenv("DEVPULSE_KEY_ALIAS")?.ifBlank { null } ?: "devpulse"
                keyPassword = System.getenv("DEVPULSE_KEY_PASSWORD")
                    ?: System.getenv("DEVPULSE_STORE_PASSWORD").orEmpty()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (pulseStore != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.work:work-runtime-ktx:2.10.5")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("com.auroraoss:gplayapi:3.6.4")
    implementation("com.google.code.gson:gson:2.14.0")

    testImplementation("androidx.test:core:1.7.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // FOSS ONLY: No proprietary Play Services or closed telemetry SDKs
}
