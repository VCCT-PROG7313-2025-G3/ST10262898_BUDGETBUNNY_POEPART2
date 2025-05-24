pluginManagement {
    repositories {
        google() // Google's repository for Android-related plugins
        mavenCentral() // Maven Central repository
        gradlePluginPortal() // For plugins from Gradle Plugin Portal
        maven { url = uri("https://jitpack.io") } // For dependencies from JitPack
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  // Ensure no project-level repos are used
    repositories {
        google() // Google's repository for dependencies
        mavenCentral() // Maven Central repository
        maven { url = uri("https://jitpack.io") } // Ensure JitPack is included for MPAndroidChart
    }
}

rootProject.name = "ST10262898_BUDGETBUNNY_POEPART2"
include(":app")

