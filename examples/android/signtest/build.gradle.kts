plugins {
    id("com.android.application")
}

android {
    namespace = "app.devpulse.signtest"
    compileSdk = 37
    defaultConfig {
        applicationId = "app.devpulse.signtest"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "2.0"
    }
    val alphaStore = file("alpha.jks")
    val betaStore = file("beta.jks")
    if (alphaStore.isFile && betaStore.isFile) {
        signingConfigs {
            create("alpha") {
                storeFile = alphaStore
                storePassword = "signtest"
                keyAlias = "a"
                keyPassword = "signtest"
            }
            create("beta") {
                storeFile = betaStore
                storePassword = "signtest"
                keyAlias = "b"
                keyPassword = "signtest"
            }
        }
        flavorDimensions += "cert"
        productFlavors {
            create("alpha") {
                signingConfig = signingConfigs.getByName("alpha")
            }
            create("beta") {
                signingConfig = signingConfigs.getByName("beta")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
