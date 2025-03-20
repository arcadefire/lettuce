import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.arcadefire"
version = "0.3.2"

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        all {
            with(languageSettings) {
                enableLanguageFeature("ContextReceivers")
                optIn("kotlin.RequiresOptIn")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.atomicfu)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.lifecycle.viewmodel.ktx)
                implementation(libs.core.ktx)
            }
        }
    }
}

android {
    namespace = "io.github.arcadefire.lettuce"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "lettuce", version.toString())

    pom {
        name.set("Lettuce multiplatform library")
        description.set("Simple Redux implementation written in Kotlin")
        inceptionYear.set("2024")
        url.set("https://github.com/arcadefire/lettuce.git")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("arcadefire")
                name.set("Angelo Marchesin")
                url.set("https://github.com/arcadefire/")
            }
        }
        scm {
            url.set("https://github.com/arcadefire/lettuce.git")
            connection.set("scm:git:git:github.com/arcadefire/lettuce.git")
            developerConnection.set("scm:git:ssh://git@github.com/arcadefire/lettuce.git")
        }
    }
}