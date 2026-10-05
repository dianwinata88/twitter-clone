plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.twitterclone.core.network"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
}

dependencies {
    api(projects.core.model)
    api(libs.retrofit)
    api(libs.okhttp)
    api(libs.kotlinx.serialization.json)

    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.logging.interceptor)
}
