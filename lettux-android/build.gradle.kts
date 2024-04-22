buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.2")
    }
}

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

android {
    compileSdk = 34
    namespace = "org.lettux.android"

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        aarMetadata {
            minCompileSdk = 21
        }
    }

    val javaVersion = JavaVersion.VERSION_17

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("debug") {
            withSourcesJar()
            withJavadocJar()
        }
        version = project.version.toString()
        group = project.group.toString()
    }
}

dependencies {
    implementation(project(":lettux-core"))

    implementation("androidx.compose.ui:ui:1.6.6")
    implementation("androidx.compose.runtime:runtime:1.6.6")
    implementation("androidx.compose.foundation:foundation:1.6.6")
    implementation("androidx.compose.animation:animation:1.6.6")
    implementation("androidx.compose.material:material:1.6.6")
    implementation("androidx.compose.material3:material3:1.2.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.kotest:kotest-assertions-core:5.8.1")
    testImplementation("org.robolectric:robolectric:4.12.1")
    testImplementation("androidx.compose.ui:ui-test-junit4:1.6.6")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.6")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("debug") {
                from(components["debug"])
                version = project.version.toString()
                groupId = project.group.toString()
            }
        }
    }
}