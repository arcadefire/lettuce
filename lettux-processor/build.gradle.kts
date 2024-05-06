plugins {
    id("org.jetbrains.kotlin.jvm")
    `maven-publish`
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.23-1.0.20")
    implementation("com.squareup:kotlinpoet:1.16.0")
    implementation("com.squareup:kotlinpoet-ksp:1.16.0")
}

publishing {
    publications {
        create<MavenPublication>("processor") {
            from(components["java"])
            version = project.version.toString()
            groupId = project.group.toString()
        }
    }
}