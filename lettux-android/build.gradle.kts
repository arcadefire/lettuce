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
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.core:core-ktx:1.12.0")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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