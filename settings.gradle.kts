pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.convention(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}


rootProject.name = "lettux"
include("lettux-android")
include("lettux-core")
include(":lettux-processor")
