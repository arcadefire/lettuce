import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    publications.withType<MavenPublication> {
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        pom {
            name.set("Lettux multiplatform library")
            description.set("Simple Redux implementation written in Kotlin.")
            url.set("https://github.com/Kotlin/multiplatform-library-template")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("arcadefire")
                    name.set("Angelo Marchesin")
                }
            }
            scm {
                url.set("https://github.com/Kotlin/multiplatform-library-template")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
