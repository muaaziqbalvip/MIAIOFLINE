pluginManagement {
    repositories {
        google()               // Android AGP plugins must resolve from google() first
        gradlePluginPortal()   // Then Gradle core and Kotlin plugins
        mavenCentral()         // Finally Maven Central as fallback
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MI AI Offline"
include(":app")
