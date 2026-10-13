plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.twitterclone.core.datastore"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
}

dependencies {
    api(libs.androidx.datastore.preferences)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
