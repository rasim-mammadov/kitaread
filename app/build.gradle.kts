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
        versionCode = 2
        versionName = "1.1"
    }
}
