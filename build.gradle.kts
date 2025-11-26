plugins {
    kotlin("jvm") version "2.2.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
tasks {
    shadowJar {
        archiveBaseName.set("my-app")
        archiveVersion.set("1.0.0")
        archiveClassifier.set("")
        manifest {
            attributes["Main-Class"] = "MainKt"
        }
    }
}
