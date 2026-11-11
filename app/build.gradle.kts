plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "az.leafreader"
    compileSdk = 35
    defaultConfig {
        applicationId = "az.leafreader"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "2.1-preview"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin {
    jvmToolchain(17)
}
