enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        // Google-hosted mirror of Maven Central. Used instead of repo1 because
        // some networks (incl. CI-less VMs) get HTTP 429 from central.sonatype.org.
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        // Marker artifacts only exist on the plugin portal; fetch their .pom
        // and skip .module metadata — the portal proxies .module lookups to
        // repo1, which fails under rate limiting.
        maven {
            url = uri("https://plugins.gradle.org/m2/")
            metadataSources {
                mavenPom()
                ignoreGradleMetadataRedirection()
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
    }
}

rootProject.name = "twitter-clone"

include(":app")

include(":core:model")
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:data")
include(":core:ui")

include(":feature:auth")
include(":feature:feed")
include(":feature:compose-tweet")
include(":feature:profile")
