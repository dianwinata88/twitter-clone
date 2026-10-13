plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(libs.androidx.paging.common)
    api(libs.kotlinx.datetime)

    testImplementation(libs.junit)
}
