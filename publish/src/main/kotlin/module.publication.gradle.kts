import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}

mavenPublishing {
    coordinates("io.github.arcadefire", "lettuce", version.toString())

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